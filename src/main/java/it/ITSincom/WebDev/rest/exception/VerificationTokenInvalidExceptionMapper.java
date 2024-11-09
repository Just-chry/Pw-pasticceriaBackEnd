package it.ITSincom.WebDev.rest.exception;

import it.ITSincom.WebDev.service.exception.VerificationTokenInvalidException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class VerificationTokenInvalidExceptionMapper implements ExceptionMapper<VerificationTokenInvalidException> {

    @Override
    public Response toResponse(VerificationTokenInvalidException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(exception.getMessage())
                .build();
    }
}
