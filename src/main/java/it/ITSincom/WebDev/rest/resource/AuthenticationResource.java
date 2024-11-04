package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.persistence.model.User;
import it.ITSincom.WebDev.rest.model.LoginRequest;
import it.ITSincom.WebDev.service.exception.AuthenticationException;
import it.ITSincom.WebDev.service.exception.UserCreationException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import it.ITSincom.WebDev.rest.model.CreateUserRequest;
import it.ITSincom.WebDev.service.AuthenticationService;

import java.time.LocalDateTime;
import java.util.UUID;


@Path("/auth")
public class AuthenticationResource {

    private final AuthenticationService authenticationService;

    public AuthenticationResource(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(CreateUserRequest request) throws UserCreationException {
        authenticationService.register(request);
        return Response.ok("Registrazione completata con successo").build();
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest request) throws AuthenticationException {
        String sessionId = authenticationService.login(request);
        NewCookie sessionCookie = new NewCookie("sessionId", sessionId, "/", null, "Session Cookie", 3600, false);
        return Response.ok("Login avvenuto con successo").cookie(sessionCookie).build();
    }

    @DELETE
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@CookieParam("sessionId") String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Sessione non trovata o non valida.").build();
        }

        try {
            authenticationService.logout(sessionId);
            NewCookie expiredCookie = new NewCookie("sessionId", "", "/", null, "Session Cookie", 0, false);
            return Response.ok("Logout avvenuto con successo").cookie(expiredCookie).build();
        } catch (AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }


}
