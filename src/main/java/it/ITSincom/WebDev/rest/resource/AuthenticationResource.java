package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.persistence.model.User;
import it.ITSincom.WebDev.rest.model.LoginRequest;
import it.ITSincom.WebDev.service.exception.AuthenticationException;
import it.ITSincom.WebDev.service.exception.UserCreationException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
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
    public Response login(LoginRequest request) {
        try {
            String sessionId = authenticationService.login(request);

            // Creazione di un cookie di sessione con l'ID ritornato dal servizio di login
            NewCookie sessionCookie = new NewCookie("sessionId", sessionId, "/", null, "Session Cookie", 3600, false);

            return Response.ok("Login avvenuto con successo").cookie(sessionCookie).build();
        } catch (AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }

}
