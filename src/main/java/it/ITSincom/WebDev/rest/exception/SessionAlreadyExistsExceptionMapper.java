package it.ITSincom.WebDev.rest.exception;

import it.ITSincom.WebDev.service.exception.SessionAlreadyExistsException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SessionAlreadyExistsExceptionMapper implements ExceptionMapper<SessionAlreadyExistsException> {

    @Override
    public Response toResponse(SessionAlreadyExistsException exception) {
        return Response.status(Response.Status.CONFLICT)
                .entity(exception.getMessage())
                .build();
    }
}
