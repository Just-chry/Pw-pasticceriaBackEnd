package it.ITSincom.WebDev.rest.exception;

import it.ITSincom.WebDev.service.exception.InvalidProductException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class InvalidProductExceptionMapper implements ExceptionMapper<InvalidProductException> {

    @Override
    public Response toResponse(InvalidProductException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(exception.getMessage())
                .build();
    }
}
