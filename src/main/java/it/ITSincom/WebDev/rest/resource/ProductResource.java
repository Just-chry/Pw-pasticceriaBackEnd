package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.rest.model.ProductResponse;
import it.ITSincom.WebDev.service.ProductService;
import it.ITSincom.WebDev.service.AuthenticationService;
import it.ITSincom.WebDev.service.exception.UserSessionNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ProductResource {

    private final ProductService productService;
    private final AuthenticationService authenticationService;

    @Inject
    public ProductResource(ProductService productService, AuthenticationService authenticationService) {
        this.productService = productService;
        this.authenticationService = authenticationService;
    }

    @GET
    @Path("/available")
    public Response getVisibleProducts(@CookieParam("sessionId") String sessionId) {
        try {
            validateSession(sessionId);
            List<ProductResponse> productResponses = productService.getVisibleProducts();
            return Response.ok(productResponses).build();
        } catch (UserSessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }

    private void validateSession(String sessionId) throws UserSessionNotFoundException {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new UserSessionNotFoundException("Sessione non trovata o non valida. Effettua il login.");
        }

        if (authenticationService.findUserSessionBySessionId(sessionId) == null) {
            throw new UserSessionNotFoundException("Sessione non valida. Effettua il login.");
        }
    }
}
