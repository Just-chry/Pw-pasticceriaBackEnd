package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.persistence.model.User;
import it.ITSincom.WebDev.rest.model.LoginRequest;
import it.ITSincom.WebDev.rest.model.LoginResponse;
import it.ITSincom.WebDev.service.exception.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import it.ITSincom.WebDev.rest.model.CreateUserRequest;
import it.ITSincom.WebDev.service.AuthenticationService;

import java.util.Optional;


@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthenticationResource {

    private final AuthenticationService authenticationService;

    public AuthenticationResource(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @POST
    @Path("/register")
    public Response register(CreateUserRequest request) throws UserCreationException {
        authenticationService.register(request);
        return Response.ok("Registrazione completata con successo").build();
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest request) throws UserNotFoundException, WrongPasswordException, SessionAlreadyExistsException {
        String sessionId = authenticationService.login(request);
        Optional<User> optionalUser = authenticationService.findUserByEmailOrTelefono(request.getEmailOrPhone());
        User user = optionalUser.orElseThrow(() -> new UserNotFoundException("Utente non trovato."));

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


}
