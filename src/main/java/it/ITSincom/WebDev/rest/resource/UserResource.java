package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.persistence.model.User;
import it.ITSincom.WebDev.rest.model.UserResponse;
import it.ITSincom.WebDev.service.AuthenticationService;
import it.ITSincom.WebDev.service.exception.UserSessionNotFoundException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    AuthenticationService authenticationService;

    @GET
    @Path("/all")
    public Response getAllUsers(@CookieParam("sessionId") String sessionId) {
        try {
            if (sessionId == null || sessionId.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Sessione non valida").build();
            }
            authenticationService.isAdmin(sessionId);

            List<User> users = authenticationService.getAllUsers();
            List<UserResponse> userResponses = new ArrayList<>();

            for (User user : users) {
                UserResponse response = new UserResponse(user.getName(), user.getSurname(), user.getEmail(), user.getPhone());
                userResponses.add(response);
            }

            return Response.ok(userResponses).build();
        } catch (UserSessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }
}
