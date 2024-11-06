package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.persistence.ProductRepository;
import it.ITSincom.WebDev.persistence.model.Product;
import it.ITSincom.WebDev.persistence.model.UserSession;
import it.ITSincom.WebDev.rest.model.ProductResponse;
import it.ITSincom.WebDev.service.AuthenticationService;
import it.ITSincom.WebDev.service.exception.UserSessionNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ProductResource {

    private final ProductRepository productRepository;
    private final AuthenticationService authenticationService;

    @Inject
    public ProductResource(ProductRepository productRepository, AuthenticationService authenticationService) {
        this.productRepository = productRepository;
        this.authenticationService = authenticationService;
    }

    @GET
    @Path("/available")
    public Response getVisibleProducts(@CookieParam("sessionId") String sessionId) {
        try {
            validateSession(sessionId);
            List<Product> products = productRepository.findVisibleProducts();

            // Converte la lista di Product in ProductResponse usando stream()
            List<ProductResponse> productResponses = products.stream()
                    .map(product -> {
                        List<String> ingredients = productRepository.findIngredientNamesByProductId(product.getId());
                        return new ProductResponse(
                                product.getName(),
                                product.getDescription(),
                                product.getImage(),
                                product.getPrice(),
                                product.getCategory().name(),
                                ingredients
                        );
                    })
                    .collect(Collectors.toList());

            return Response.ok(productResponses).build();
        } catch (UserSessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }



    private void validateSession(String sessionId) throws UserSessionNotFoundException {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new UserSessionNotFoundException("Sessione non trovata o non valida. Effettua il login.");
        }

        UserSession userSession = authenticationService.findUserSessionBySessionId(sessionId);
        if (userSession == null) {
            throw new UserSessionNotFoundException("Sessione non valida. Effettua il login.");
        }
    }
}
