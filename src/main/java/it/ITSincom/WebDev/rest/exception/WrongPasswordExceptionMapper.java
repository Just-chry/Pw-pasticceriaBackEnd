package it.ITSincom.WebDev.rest.exception;

import it.ITSincom.WebDev.service.exception.WrongPasswordException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class WrongPasswordExceptionMapper implements ExceptionMapper<WrongPasswordException> {

    @Override
    public Response toResponse(WrongPasswordException exception) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(exception.getMessage())
                .build();
    }
}
