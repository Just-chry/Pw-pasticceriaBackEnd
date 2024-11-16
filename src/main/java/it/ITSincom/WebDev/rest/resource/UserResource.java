package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.persistence.model.User;
import it.ITSincom.WebDev.rest.model.ExtendedUserResponse;
import it.ITSincom.WebDev.rest.model.UserResponse;
import it.ITSincom.WebDev.service.AuthenticationService;
import it.ITSincom.WebDev.service.exception.UserNotFoundException;
import it.ITSincom.WebDev.service.exception.UserSessionNotFoundException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    AuthenticationService authenticationService;

    @GET
    @Path("/{userId}")
    public Response getUserById(@PathParam("userId") String userId) throws UserNotFoundException {
        User user = authenticationService.getUserById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(user).build();
    }

    @GET
    @Path("/all")
    public Response getAllUsers(@CookieParam("sessionId") String sessionId) {
        try {
            authenticationService.isAdmin(sessionId);
            List<ExtendedUserResponse> userResponses = authenticationService.getAllUserResponses();
            return Response.ok(userResponses).build();
        } catch (UserSessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }

    @GET
    public Response getUser(@CookieParam("sessionId") String sessionId) throws UserSessionNotFoundException {
        User user = authenticationService.getUserBySessionId(sessionId);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Utente non trovato").build();
        }

        UserResponse userResponse = new UserResponse(
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getPhone(),
                user.getRole()
        );

        return Response.ok(userResponse).build();
    }

    @POST
    @Path("/verify/{userId}")
    public Response verifyUserField(@PathParam("userId") String userId, @QueryParam("field") String field) {
        try {
            User user = authenticationService.getUserById(userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Utente non trovato").build();
            }

            switch (field.toLowerCase()) {
                case "email":
                    user.setEmailVerified(true);
                    break;
                case "phone":
                    user.setPhoneVerified(true);
                    break;
                default:
                    return Response.status(Response.Status.BAD_REQUEST).entity("Campo di verifica non valido").build();
            }

            // Salva le modifiche nel database
            authenticationService.updateUser(user);

            return Response.ok("Utente verificato con successo").build();
        } catch (UserNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity("Utente non trovato").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Errore interno del server").build();
        }
    }


}
