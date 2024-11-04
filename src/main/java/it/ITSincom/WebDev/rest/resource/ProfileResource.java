package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.persistence.model.User;
import it.ITSincom.WebDev.persistence.model.UserSession;
import it.ITSincom.WebDev.rest.model.ModifyRequest;
import it.ITSincom.WebDev.rest.model.UserResponse;
import it.ITSincom.WebDev.service.AuthenticationService;
import it.ITSincom.WebDev.service.ProfileService;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/profile")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProfileResource {

    private final AuthenticationService authenticationService;
    private final ProfileService profileService;

    @Inject
    public ProfileResource(AuthenticationService authenticationService, ProfileService profileService) {
        this.authenticationService = authenticationService;
        this.profileService = profileService;
    }


    private void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new EntityNotFoundException("Sessione non trovata.");
        }
    }

    @PUT
    @Path("/modify/email")
    public Response modifyEmail(ModifyRequest request, @CookieParam("sessionId") String sessionId) {
        try {
            validateSessionId(sessionId);
            UserSession userSession = authenticationService.findUserSessionBySessionId(sessionId);
            if (userSession == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Sessione non valida").build();
            }
            User user = authenticationService.findUserById(userSession.getUserId());
            if (request.getEmail() != null) {
                user.setEmail(request.getEmail());
            }
            User updatedUser = profileService.updateUser(user);
            UserResponse response = new UserResponse(updatedUser.getNome(), updatedUser.getCognome(), updatedUser.getEmail(), updatedUser.getTelefono());
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/modify/telefono")
    public Response modifyTelefono(ModifyRequest request, @CookieParam("sessionId") String sessionId) {
        try {
            validateSessionId(sessionId);
            UserSession userSession = authenticationService.findUserSessionBySessionId(sessionId);
            if (userSession == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Sessione non valida").build();
            }
            User user = authenticationService.findUserById(userSession.getUserId());
            if (request.getTelefono() != null) {
                user.setTelefono(request.getTelefono());
            }
            User updatedUser = profileService.updateUser(user);
            UserResponse response = new UserResponse(updatedUser.getNome(), updatedUser.getCognome(), updatedUser.getEmail(), updatedUser.getTelefono());
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
}
