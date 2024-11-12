package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.persistence.model.Product;
import it.ITSincom.WebDev.persistence.model.User;
import it.ITSincom.WebDev.persistence.model.UserSession;
import it.ITSincom.WebDev.rest.model.ProductAdminResponse;
import it.ITSincom.WebDev.rest.model.ProductResponse;
import it.ITSincom.WebDev.service.ProductService;
import it.ITSincom.WebDev.service.AuthenticationService;
import it.ITSincom.WebDev.service.exception.UnauthorizedAccessException;
import it.ITSincom.WebDev.service.exception.UserSessionNotFoundException;
import it.ITSincom.WebDev.util.Validation;
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
    public Response getAllProducts(@CookieParam("sessionId") String sessionId) throws UserSessionNotFoundException {
        try {
            authenticationService.isAdmin(sessionId);
            List<ProductAdminResponse> productResponses = productService.getAllProductsForAdmin();
            return Response.ok(productResponses).build();

        } catch (UnauthorizedAccessException e) {
            List<ProductResponse> productResponses = productService.getAllProductsForUser();
            return Response.ok(productResponses).build();
        }
    }

    @GET
    @Path("/{category}")
    public Response getProductsByCategory(@CookieParam("sessionId") String sessionId, @PathParam("category") String category) throws UserSessionNotFoundException {
        try {
            authenticationService.isAdmin(sessionId);
            List<ProductAdminResponse> productResponses = productService.getProductsByCategoryForAdmin(category);
            return Response.ok(productResponses).build();

        } catch (UnauthorizedAccessException e) {
            List<ProductResponse> productResponses = productService.getProductsByCategoryForUser(category);
            return Response.ok(productResponses).build();
        }
    }


    @POST
    @Path("/add")
    public Response addProduct(@CookieParam("sessionId") String sessionId, Product product) throws UserSessionNotFoundException {
        authenticationService.isAdmin(sessionId);
        productService.addProduct(product);
        return Response.status(Response.Status.CREATED).entity("Prodotto aggiunto con successo").build();
    }

    @POST
    @Path("/add-multiple")
    public Response addProducts(@CookieParam("sessionId") String sessionId, List<Product> products) throws UserSessionNotFoundException {
        authenticationService.isAdmin(sessionId);
        productService.addProducts(products);
        return Response.status(Response.Status.CREATED).entity("Prodotti aggiunti con successo").build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteProduct(@CookieParam("sessionId") String sessionId, @PathParam("id") String productId) throws UserSessionNotFoundException {
        authenticationService.isAdmin(sessionId);
        productService.deleteProduct(productId);
        return Response.ok("Prodotto con ID " + productId + " rimosso con successo").build();
    }

    @DELETE
    @Path("/{id}/decrement")
    public Response decrementProductQuantity(@CookieParam("sessionId") String sessionId, @PathParam("id") String productId) throws UserSessionNotFoundException {
        authenticationService.isAdmin(sessionId);
        productService.decrementProductQuantity(productId);
        return Response.ok("Quantità del Prodotto: " + productId + " decrementata di 1").build();
    }

    @PUT
    @Path("/{id}/increment")
    public Response incrementProductQuantity(@CookieParam("sessionId") String sessionId, @PathParam("id") String productId) throws UserSessionNotFoundException {
        authenticationService.isAdmin(sessionId);
        productService.incrementProductQuantity(productId);
        return Response.ok("Quantità del Prodotto: " + productId + " incrementata di 1").build();
    }



    @PUT
    @Path("/{id}")
    public Response modifyProduct(@CookieParam("sessionId") String sessionId, @PathParam("id") String productId, Product updatedProduct) throws UserSessionNotFoundException {
        authenticationService.isAdmin(sessionId);
        productService.modifyProduct(productId, updatedProduct);
        return Response.ok("Prodotto con ID " + productId + " modificato con successo").build();
    }


}
