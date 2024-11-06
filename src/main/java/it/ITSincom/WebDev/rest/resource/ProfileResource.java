package it.ITSincom.WebDev.rest.resource;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import it.ITSincom.WebDev.persistence.model.User;
import it.ITSincom.WebDev.persistence.model.UserSession;
import it.ITSincom.WebDev.rest.model.ModifyRequest;
import it.ITSincom.WebDev.rest.model.PasswordModifyRequest;
import it.ITSincom.WebDev.rest.model.UserResponse;
import it.ITSincom.WebDev.service.AuthenticationService;
import it.ITSincom.WebDev.service.HashCalculator;
import it.ITSincom.WebDev.service.ProfileService;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/profile")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProfileResource {

    private final AuthenticationService authenticationService;
    private final ProfileService profileService;
    private final Mailer mailer;

    @Inject
    public ProfileResource(AuthenticationService authenticationService, ProfileService profileService, Mailer mailer) {
        this.authenticationService = authenticationService;
        this.profileService = profileService;
        this.mailer = mailer;
    }

    private User validateUserSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new EntityNotFoundException("Sessione non trovata.");
        }
        UserSession userSession = authenticationService.findUserSessionBySessionId(sessionId);
        if (userSession == null) {
            throw new EntityNotFoundException("Sessione non valida.");
        }
        User user = authenticationService.findUserById(userSession.getUserId());
        if (user == null) {
            throw new EntityNotFoundException("Utente non trovato.");
        }
        return user;
    }

    @PUT
    @Path("/modify/email")
    public Response modifyEmail(ModifyRequest request, @CookieParam("sessionId") String sessionId) {
        try {
            User user = validateUserSession(sessionId);
            if (request.getEmail() != null) {
                user.setEmail(request.getEmail());
                user.setEmailVerified(false);

                String verificationTokenEmail = UUID.randomUUID().toString();
                user.setVerificationTokenEmail(verificationTokenEmail);

                String verificationLink = "http://localhost:8080/auth/verify?token=" + verificationTokenEmail + "&contact=" + user.getEmail();
                mailer.send(Mail.withHtml(user.getEmail(),
                        "Conferma la tua email",
                        "<h1>Ciao " + user.getName() + "!</h1>" +
                                "<p>Per favore, clicca sul link seguente per verificare il tuo nuovo indirizzo email:</p>" +
                                "<a href=\"" + verificationLink + "\">Verifica la tua email</a>"));
            }

            User updatedUser = profileService.updateUser(user);
            UserResponse response = new UserResponse(updatedUser.getName(), updatedUser.getSurname(), updatedUser.getEmail(), updatedUser.getPhone());
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }


    @PUT
    @Path("/modify/telefono")
    public Response modifyTelefono(ModifyRequest request, @CookieParam("sessionId") String sessionId) {
        try {
            User user = validateUserSession(sessionId);
            if (request.getPhone() != null) {
                user.setPhone(request.getPhone());
            }
            User updatedUser = profileService.updateUser(user);
            UserResponse response = new UserResponse(updatedUser.getName(), updatedUser.getSurname(), updatedUser.getEmail(), updatedUser.getPhone());
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/modify/password")
    public Response modifyPassword(PasswordModifyRequest request, @CookieParam("sessionId") String sessionId) {
        try {
            validateUserSession(sessionId);
            UserSession userSession = authenticationService.findUserSessionBySessionId(sessionId);
            if (userSession == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Sessione non valida").build();
            }

            User user = authenticationService.findUserById(userSession.getUserId());
            String hashedOldPassword = authenticationService.hashPassword(request.getOldPassword());
            if (!authenticationService.verifyPassword(user.getPassword(), hashedOldPassword)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("La vecchia password non corrisponde").build();
            }

            if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("La nuova password non può essere vuota").build();
            }

            String hashedNewPassword = authenticationService.hashPassword(request.getNewPassword());
            user.setPassword(hashedNewPassword);
            User updatedUser = profileService.updateUser(user);
            return Response.ok("Password aggiornata con successo").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
}
