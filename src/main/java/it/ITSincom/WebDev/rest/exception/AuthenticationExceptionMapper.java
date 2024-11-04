package it.ITSincom.WebDev.rest.exception;

import it.ITSincom.WebDev.service.exception.AuthenticationException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AuthenticationExceptionMapper implements ExceptionMapper<AuthenticationException> {

    @Override
    public Response toResponse(AuthenticationException exception) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(exception.getMessage())
                .build();
    }
}
