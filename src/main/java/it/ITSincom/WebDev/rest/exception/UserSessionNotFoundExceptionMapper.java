package it.ITSincom.WebDev.rest.exception;

import it.ITSincom.WebDev.service.exception.UserSessionNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class UserSessionNotFoundExceptionMapper implements ExceptionMapper<UserSessionNotFoundException> {

    @Override
    public Response toResponse(UserSessionNotFoundException exception) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(exception.getMessage())
                .build();
    }
}
