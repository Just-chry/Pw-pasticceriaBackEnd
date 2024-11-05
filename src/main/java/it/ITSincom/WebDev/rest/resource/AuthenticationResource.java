package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.persistence.model.User;
import it.ITSincom.WebDev.rest.model.LoginRequest;
import it.ITSincom.WebDev.rest.model.LoginResponse;
import it.ITSincom.WebDev.service.ProfileService;
import it.ITSincom.WebDev.service.exception.*;
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
    private final ProfileService profileService;
    private final Mailer mailer;

    @Inject
    public AuthenticationResource(AuthenticationService authenticationService, ProfileService profileService, Mailer mailer) {
        this.authenticationService = authenticationService;
        this.profileService = profileService;
        this.mailer = mailer;
    }

    @POST
    @Path("/register")
    public Response register(CreateUserRequest request) throws UserCreationException {
        User user = authenticationService.register(request);

        String verificationLink = "http://localhost:8080/auth/verify?token=" + user.getVerificationToken() + "&email=" + user.getEmail();

        mailer.send(Mail.withHtml(user.getEmail(),
                "Conferma la tua registrazione",
                "<h1>Benvenuto " + user.getName() + " " + user.getSurname() + "!</h1>" +
                        "<p>Per favore, clicca sul link seguente per verificare il tuo indirizzo email:</p>" +
                        "<a href=\"" + verificationLink + "\">Verifica la tua email</a>"));

        return Response.ok("Registrazione completata con successo, controlla la tua email per confermare.").build();
    }



    @POST
    @Path("/login")
    public Response login(LoginRequest request) throws UserNotFoundException, WrongPasswordException, SessionAlreadyExistsException {
        Optional<User> optionalUser = authenticationService.findUserByEmailOrTelefono(request.getEmailOrPhone());
        User user = optionalUser.orElseThrow(() -> new UserNotFoundException("Utente non trovato."));

        if (!user.getEmailVerified()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Email non verificata. Per favore, verifica il tuo indirizzo email.").build();
        }

        String sessionId = authenticationService.login(request);
        LoginResponse response = new LoginResponse("Login avvenuto con successo", user.getName());
        NewCookie sessionCookie = new NewCookie("sessionId", sessionId, "/", null, "Session Cookie", 3600, false);

        return Response.ok(response).cookie(sessionCookie).build();
    }


    @DELETE
    @Path("/logout")
    public Response logout(@CookieParam("sessionId") String sessionId) throws UserSessionNotFoundException {
        if (sessionId == null || sessionId.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Sessione non trovata o non valida.").build();
        }

        authenticationService.logout(sessionId);
        NewCookie expiredCookie = new NewCookie("sessionId", "", "/", null, "Session Cookie", 0, false);
        return Response.ok("Logout avvenuto con successo").cookie(expiredCookie).build();
    }

    @GET
    @Path("/verify")
    public Response verifyEmail(@QueryParam("token") String token, @QueryParam("email") String email) {
        // Cerca l'utente nel database tramite l'email
        Optional<User> optionalUser = authenticationService.findUserByEmail(email);

        if (optionalUser.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Utente non trovato.").build();
        }

        User user = optionalUser.get();

        if (user.getVerificationToken() == null || !user.getVerificationToken().equals(token)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Token non valido.").build();
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        profileService.updateUser(user);

        return Response.ok("Email verificata con successo! Ora puoi accedere.").build();
    }


    @GET
    @Path("/send-test-email")
    public Response sendTestEmail() {
        String email = "sl.falese@gmail.com";  // Può essere qualsiasi indirizzo email valido
        mailer.send(Mail.withText(email, "Email di Prova", "Questa è una email di prova inviata tramite Quarkus Mailer."));

        return Response.ok("Email di prova inviata con successo a " + email).build();
    }

}
