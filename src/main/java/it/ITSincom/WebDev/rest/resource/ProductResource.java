package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.persistence.model.Product;
import it.ITSincom.WebDev.rest.model.ProductResponse;
import it.ITSincom.WebDev.service.ProductService;
import it.ITSincom.WebDev.service.AuthenticationService;
import it.ITSincom.WebDev.service.exception.ProductNotFoundException;
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
    public Response getVisibleProducts(@CookieParam("sessionId") String sessionId) throws UserSessionNotFoundException {
        validateSession(sessionId);
        List<ProductResponse> productResponses = productService.getVisibleProducts();
        return Response.ok(productResponses).build();
    }

    @GET
    public Response getAllProducts(@CookieParam("sessionId") String sessionId) throws UserSessionNotFoundException {
        validateAdminSession(sessionId);
        List<Product> productResponses = productService.getAllProducts();
        return Response.ok(productResponses).build();
    }

    @POST
    @Path("/add")
    public Response addProduct(@CookieParam("sessionId") String sessionId, Product product) throws UserSessionNotFoundException {
        validateAdminSession(sessionId);
        productService.addProduct(product);
        return Response.status(Response.Status.CREATED).entity("Prodotto aggiunto con successo").build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteProduct(@CookieParam("sessionId") String sessionId, @PathParam("id") String productId) throws UserSessionNotFoundException {
        validateAdminSession(sessionId);
        productService.deleteProduct(productId);
        return Response.ok("Prodotto con ID " + productId + " rimosso con successo").build();
    }

    @DELETE
    @Path("/{id}/decrement")
    public Response decrementProductQuantity(@CookieParam("sessionId") String sessionId, @PathParam("id") String productId) throws UserSessionNotFoundException {
        validateAdminSession(sessionId);
        productService.decrementProductQuantity(productId);
        return Response.ok("Quantità del Prodotto: " + productId +  " decrementata di 1").build();
    }

    @PUT
    @Path("/{id}/increment")
    public Response incrementProductQuantity(@CookieParam("sessionId") String sessionId, @PathParam("id") String productId) throws UserSessionNotFoundException {
        validateAdminSession(sessionId);
        productService.incrementProductQuantity(productId);
        return Response.ok("Quantità del Prodotto: " + productId +  " incrementata di 1").build();
    }

    @PUT
    @Path("/{id}")
    public Response modifyProduct(@CookieParam("sessionId") String sessionId, @PathParam("id") String productId, Product updatedProduct) throws UserSessionNotFoundException{
        validateAdminSession(sessionId);
        productService.modifyProduct(productId, updatedProduct);
        return Response.ok("Prodotto con ID " + productId + " modificato con successo").build();
    }

    private void validateSession(String sessionId) throws UserSessionNotFoundException {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new UserSessionNotFoundException("Sessione non trovata o non valida. Effettua il login.");
        }

        if (authenticationService.findUserSessionBySessionId(sessionId) == null) {
            throw new UserSessionNotFoundException("Sessione non valida. Effettua il login.");
        }
    }

    private void validateAdminSession(String sessionId) throws UserSessionNotFoundException {
        validateSession(sessionId);
        authenticationService.isAdmin(sessionId);
    }

}
