    package it.ITSincom.WebDev.rest.resource;

    import it.ITSincom.WebDev.persistence.model.User;
    import it.ITSincom.WebDev.rest.model.LoginRequest;
    import it.ITSincom.WebDev.rest.model.LoginResponse;
    import it.ITSincom.WebDev.service.SmsService;
    import it.ITSincom.WebDev.service.exception.*;
    import it.ITSincom.WebDev.util.ValidationUtils;
    import jakarta.inject.Inject;
    import jakarta.ws.rs.*;
    import jakarta.ws.rs.core.MediaType;
    import jakarta.ws.rs.core.NewCookie;
    import jakarta.ws.rs.core.Response;

    import it.ITSincom.WebDev.rest.model.CreateUserRequest;
    import it.ITSincom.WebDev.service.AuthenticationService;

    import java.util.Optional;
    import java.util.UUID;
    import io.quarkus.mailer.Mailer;
    import io.quarkus.mailer.Mail;


    @Path("/auth")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public class AuthenticationResource {

        private final AuthenticationService authenticationService;
        private final Mailer mailer;
        private final SmsService smsService;

        @Inject
        public AuthenticationResource(AuthenticationService authenticationService, Mailer mailer, SmsService smsService) {
            this.authenticationService = authenticationService;
            this.mailer = mailer;
            this.smsService = smsService;
        }

        @POST
        @Path("/register")
        public Response register(CreateUserRequest request) throws UserCreationException {
            User user = authenticationService.register(request);

            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                String verificationLink = "http://localhost:8080/auth/verify?token=" + user.getVerificationTokenEmail() + "&contact=" + user.getEmail();

                mailer.send(Mail.withHtml(user.getEmail(),
                        "Conferma la tua registrazione",
                        "<h1>Benvenuto " + user.getName() + " " + user.getSurname() + "!</h1>" +
                                "<p>Per favore, clicca sul link seguente per verificare il tuo indirizzo email:</p>" +
                                "<a href=\"" + verificationLink + "\">Verifica la tua email</a>"));
            } else if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                String otp = user.getVerificationTokenPhone();
                try {
                    smsService.sendSms(user.getPhone(), "Il tuo codice OTP è: " + otp);
                } catch (SmsSendingException e) {
                    throw new UserCreationException("Errore durante l'invio dell'OTP: " + e.getMessage());
                }
            }

            return Response.ok("Registrazione completata con successo, controlla il tuo contatto per confermare.").build();
        }

    @GET
    @Path("/verify")
    public Response verify(@QueryParam("token") String token, @QueryParam("contact") String contact) {
        return authenticationService.verifyContact(token, contact);
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest request) throws UserNotFoundException, WrongPasswordException, SessionAlreadyExistsException {
        // 1. Cerca l'utente nel database tramite email o telefono
        Optional<User> optionalUser = authenticationService.findUserByEmailOrPhone(request.getEmailOrPhone());
        User user = optionalUser.orElseThrow(() -> new UserNotFoundException("Utente non trovato."));

        if (!user.getEmailVerified() && !user.getPhoneVerified()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Contatto non verificato. Per favore, verifica il tuo indirizzo email o il tuo numero di telefono.").build();
        }

        String sessionId = authenticationService.login(request);
        LoginResponse response = new LoginResponse("Login avvenuto con successo", user.getName());
        NewCookie sessionCookie = new NewCookie("sessionId", sessionId, "/", null, "Session Cookie", 3600, false);

        return Response.ok(response).cookie(sessionCookie).build();
    }

    @DELETE
    @Path("/logout")
    public Response logout(@CookieParam("sessionId") String sessionId) throws UserSessionNotFoundException {
        // Utilizza ValidationUtils per validare l'ID della sessione
        ValidationUtils.validateSessionId(sessionId);
        authenticationService.logout(sessionId);
        NewCookie expiredCookie = new NewCookie("sessionId", "", "/", null, "Session Cookie", -1, false);
        return Response.ok("Logout avvenuto con successo").cookie(expiredCookie).build();
    }

    private void sendVerificationEmail(User user, String verificationLink) {
        mailer.send(Mail.withHtml(user.getEmail(),
                "Conferma la tua registrazione",
                "<h1>Benvenuto " + user.getName() + " " + user.getSurname() + "!</h1>" +
                        "<p>Per favore, clicca sul link seguente per verificare il tuo indirizzo email:</p>" +
                        "<a href=\"" + verificationLink + "\">Verifica la tua email</a>"));
    }

    private void sendVerificationSms(User user) throws UserCreationException {
        String otp = user.getVerificationTokenPhone();
        try {
            smsService.sendSms(user.getPhone(), "Il tuo codice OTP è: " + otp);
        } catch (SmsSendingException e) {
            throw new UserCreationException("Errore durante l'invio dell'OTP: " + e.getMessage());
        }
    }
}
