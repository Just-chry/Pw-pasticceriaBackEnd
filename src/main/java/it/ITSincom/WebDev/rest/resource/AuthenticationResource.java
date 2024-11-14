package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.persistence.model.User;
import it.ITSincom.WebDev.persistence.model.UserSession;
import it.ITSincom.WebDev.rest.model.LoginRequest;
import it.ITSincom.WebDev.rest.model.LoginResponse;
import it.ITSincom.WebDev.service.exception.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import it.ITSincom.WebDev.rest.model.CreateUserRequest;
import it.ITSincom.WebDev.service.AuthenticationService;


@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthenticationResource {

    private final AuthenticationService authenticationService;


    @Inject
    public AuthenticationResource(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @POST
    @Path("/register")
    public Response register(CreateUserRequest request) throws UserCreationException {
        User user = authenticationService.register(request);

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            String verificationLink = "http://localhost:8080/auth/verify?token=" + user.getVerificationTokenEmail() + "&contact=" + user.getEmail();
            authenticationService.sendVerificationEmail(user, verificationLink);
        } else if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            authenticationService.sendVerificationSms(user);
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
    public Response login(LoginRequest request) {
        try {
            LoginResponse response = authenticationService.handleLogin(request);

            NewCookie sessionCookie = new NewCookie("sessionId", response.getSessionId(), "/", null, "Session Cookie", -1, false);
            return Response.ok(response).cookie(sessionCookie).build();
        } catch (UserNotFoundException | WrongPasswordException | SessionAlreadyExistsException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }


    @DELETE
    @Path("/logout")
    public Response logout(@CookieParam("sessionId") String sessionId) throws UserSessionNotFoundException {
        UserSession userSession = authenticationService.findUserSessionBySessionId(sessionId);
        if (userSession == null) {
            throw new UserSessionNotFoundException("Sessione non valida");
        }
        authenticationService.logout(sessionId);

        NewCookie expiredCookie = new NewCookie("sessionId", "", "/", null, "Session Cookie", -1, false);
        return Response.ok("Logout avvenuto con successo").cookie(expiredCookie).build();
    }


}
