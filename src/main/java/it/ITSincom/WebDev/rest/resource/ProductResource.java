package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.persistence.model.Product;
import it.ITSincom.WebDev.rest.model.ProductAdminResponse;
import it.ITSincom.WebDev.rest.model.ProductResponse;
import it.ITSincom.WebDev.service.ProductService;
import it.ITSincom.WebDev.service.AuthenticationService;
import it.ITSincom.WebDev.service.exception.UserSessionNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
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
    @Path("/admin")
    public Response getAllProductsAdmin(@CookieParam("sessionId") String sessionId) throws UserSessionNotFoundException {
        authenticationService.isAdmin(sessionId);
        List<ProductAdminResponse> productResponses = productService.getAllProductsForAdmin();
        return Response.ok(productResponses).build();
    }


    @GET
    public Response getAllProducts() {
        List<ProductResponse> productResponses = productService.getAllProductsForUser();
        return Response.ok(productResponses).build();
    }


    @GET
    @Path("/{id}")
    public Response getProductById(@PathParam("id") String productId) {
        ProductResponse productResponse = productService.getProductById(productId);
        return Response.ok(productResponse).build();
    }

    @GET
    @Path("/category")
    public Response getProductsByCategory(@QueryParam("category") String category) {
        List<ProductResponse> productResponses = productService.getProductsByCategoryForUser(category);
        return Response.ok(productResponses).build();
    }

    @GET
    @Path("/category/admin")
    public Response getProductsByCategoryAdmin(@CookieParam("sessionId") String sessionId, @QueryParam("category") String category) throws UserSessionNotFoundException {
        authenticationService.isAdmin(sessionId);
        List<ProductAdminResponse> productResponses = productService.getProductsByCategoryForAdmin(category);
        return Response.ok(productResponses).build();
    }


    @POST
    @Path("/add")
    public Response addProduct(@CookieParam("sessionId") String sessionId, Product productReq) throws UserSessionNotFoundException {
        authenticationService.isAdmin(sessionId);

        try {
            productService.addProduct(productReq);
            return Response.status(Response.Status.CREATED).entity(productReq).build();
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore durante il salvataggio del prodotto: " + e.getMessage())
                    .build();
        }
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
