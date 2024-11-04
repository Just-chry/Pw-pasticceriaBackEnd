package it.ITSincom.WebDev.rest.exception;

import it.ITSincom.WebDev.service.exception.UserCreationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class UserCreationExceptionMapper implements ExceptionMapper<UserCreationException> {

    @Override
    public Response toResponse(UserCreationException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(exception.getMessage())
                .build();
    }
}
