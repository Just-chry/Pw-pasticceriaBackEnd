package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.persistence.model.User;
import it.ITSincom.WebDev.service.AuthenticationService;
import it.ITSincom.WebDev.service.exception.UserSessionNotFoundException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/admin")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AdminResource {
    private final AuthenticationService authenticationService;

    @Inject
    public AdminResource(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GET
    @Path("/users")
    public Response getAllUsers(@CookieParam("sessionId") String sessionId) {
        try {
            if (sessionId == null || sessionId.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Sessione non valida").build();
            }
            authenticationService.isAdmin(sessionId);
            List<User> users = authenticationService.getAllUsers();
            return Response.ok(users).build();
        } catch (UserSessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }
}
