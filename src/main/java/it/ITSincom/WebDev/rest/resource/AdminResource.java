package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.persistence.model.Product;
import it.ITSincom.WebDev.persistence.model.User;
import it.ITSincom.WebDev.rest.model.UserResponse;
import it.ITSincom.WebDev.service.AuthenticationService;
import it.ITSincom.WebDev.service.ProductService;
import it.ITSincom.WebDev.service.exception.EntityNotFoundException;
import it.ITSincom.WebDev.service.exception.UserSessionNotFoundException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

@Path("/admin")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AdminResource {
    private final AuthenticationService authenticationService;
    private final ProductService productService;

    @Inject
    public AdminResource(AuthenticationService authenticationService, ProductService productService) {
        this.authenticationService = authenticationService;
        this.productService = productService;
    }

    @GET
    @Path("/users")
    public Response getAllUsers(@CookieParam("sessionId") String sessionId) {
        try {
            if (sessionId == null || sessionId.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Sessione non valida").build();
            }
            authenticationService.isAdmin(sessionId);

            List<User> users = authenticationService.getAllUsers();
            List<UserResponse> userResponses = new ArrayList<>();

            for (User user : users) {
                UserResponse response = new UserResponse(user.getName(), user.getSurname(), user.getEmail(), user.getPhone());
                userResponses.add(response);
            }

            return Response.ok(userResponses).build();
        } catch (UserSessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/products")
    public Response getAllProducts(@CookieParam("sessionId") String sessionId) {
        try {
            if (sessionId == null || sessionId.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Sessione non valida").build();
            }
            authenticationService.isAdmin(sessionId);
            List<Product> products = productService.getAllProducts();
            return Response.ok(products).build();
        } catch (UserSessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/product/add")
    public Response addProduct(@CookieParam("sessionId") String sessionId, Product product) {
        try {
            if (sessionId == null || sessionId.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Sessione non valida").build();
            }
            authenticationService.isAdmin(sessionId);
            productService.addProduct(product);
            return Response.status(Response.Status.CREATED).entity("Prodotto aggiunto con successo").build();
        } catch (UserSessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }


    @DELETE
    @Path("/product/{id}/decrement")
    public Response decrementProductQuantity(@CookieParam("sessionId") String sessionId, @PathParam("id") String productId) {
        try {
            if (sessionId == null || sessionId.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Sessione non valida").build();
            }
            authenticationService.isAdmin(sessionId);
            productService.decrementProductQuantity(productId);
            return Response.ok("Quantità del prodotto decrementata di 1").build();
        } catch (UserSessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/product/{id}")
    public Response deleteProduct(@CookieParam("sessionId") String sessionId, @PathParam("id") String productId) {
        try {
            if (sessionId == null || sessionId.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Sessione non valida").build();
            }
            authenticationService.isAdmin(sessionId);
            productService.deleteProduct(productId);
            return Response.ok("Prodotto rimosso con successo").build();
        } catch (UserSessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/product/{id}")
    public Response modifyProduct(@CookieParam("sessionId") String sessionId, @PathParam("id") String  productId, Product updatedProduct) {
        try {
            if (sessionId == null || sessionId.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Sessione non valida").build();
            }
            authenticationService.isAdmin(sessionId);
            productService.modifyProduct(productId, updatedProduct);
            return Response.ok("Prodotto modificato con successo").build();
        } catch (UserSessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

}

