package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.persistence.model.User;
import it.ITSincom.WebDev.persistence.model.UserSession;
import it.ITSincom.WebDev.rest.model.ModifyRequest;
import it.ITSincom.WebDev.rest.model.PasswordModifyRequest;
import it.ITSincom.WebDev.rest.model.UserResponse;
import it.ITSincom.WebDev.service.AuthenticationService;
import it.ITSincom.WebDev.service.ProfileService;
import it.ITSincom.WebDev.service.exception.UserSessionNotFoundException;
import it.ITSincom.WebDev.util.Validation;
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

    @PUT
    @Path("/modify/email")
    public Response modifyEmail(ModifyRequest request, @CookieParam("sessionId") String sessionId) {
        try {
            User user = getUserFromSession(sessionId);
            if (request.getEmail() != null) {
                profileService.updateEmail(user, request.getEmail());
            }

            User updatedUser = profileService.updateUser(user);
            UserResponse response = new UserResponse(updatedUser.getName(), updatedUser.getSurname(), updatedUser.getEmail(), updatedUser.getPhone(), updatedUser.getRole());
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/modify/phone")
    public Response modifyPhone(ModifyRequest request, @CookieParam("sessionId") String sessionId) {
        try {
            User user = getUserFromSession(sessionId);
            User updatedUser = profileService.updatePhoneNumber(user, request.getPhone());
            UserResponse response = new UserResponse(updatedUser.getName(), updatedUser.getSurname(), updatedUser.getEmail(), updatedUser.getPhone(), updatedUser.getRole());
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }


    @PUT
    @Path("/modify/password")
    public Response modifyPassword(PasswordModifyRequest request, @CookieParam("sessionId") String sessionId) {
        try {
            User user = getUserFromSession(sessionId);

            profileService.updatePassword(user, request.getOldPassword(), request.getNewPassword(), authenticationService);
            return Response.ok("Password aggiornata con successo").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    private User getUserFromSession(String sessionId) throws UserSessionNotFoundException, EntityNotFoundException {
        UserSession userSession = authenticationService.findUserSessionBySessionId(sessionId);
        User user = (userSession != null) ? userSession.getUser() : null;
        Validation.validateSessionAndUser(sessionId, userSession, user);
        return user;
    }


}
