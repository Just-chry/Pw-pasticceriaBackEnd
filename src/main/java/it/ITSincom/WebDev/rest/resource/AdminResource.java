package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.service.AuthenticationService;
import it.ITSincom.WebDev.service.OrderService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/admin")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AdminResource {

    private final AuthenticationService authenticationService;
    private final OrderService orderService;

    @Inject
    public AdminResource(AuthenticationService authenticationService, OrderService orderService) {
        this.authenticationService = authenticationService;
        this.orderService = orderService;
    }

    @PUT
    @Path("/order/accept/{orderId}")
    public Response acceptOrder(@CookieParam("sessionId") String sessionId, @PathParam("orderId") String orderId) {
        try {
            if (sessionId == null || sessionId.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Sessione non valida").build();
            }
            // Check if the user is an admin
            authenticationService.isAdmin(sessionId);

            orderService.acceptOrder(orderId);
            return Response.ok("Ordine accettato con successo").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
}
