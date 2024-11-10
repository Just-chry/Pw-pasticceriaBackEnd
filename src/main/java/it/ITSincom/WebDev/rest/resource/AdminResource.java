package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.persistence.model.User;
import it.ITSincom.WebDev.service.AuthenticationService;
import it.ITSincom.WebDev.service.NotificationService;
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
    private final NotificationService notificationService;

    @Inject
    public AdminResource(AuthenticationService authenticationService, OrderService orderService, NotificationService notificationService) {
        this.authenticationService = authenticationService;
        this.orderService = orderService;
        this.notificationService = notificationService;
    }

    @PUT
    @Path("/order/accept/{orderId}")
    public Response acceptOrder(@CookieParam("sessionId") String sessionId, @PathParam("orderId") String orderId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessione non valida").build();
        }

        try {
            // Check if the user is an admin
            authenticationService.isAdmin(sessionId);

            // Accept the order
            orderService.acceptOrder(orderId);

            User user = orderService.getUserByOrderId(orderId); // Assicurati di avere un metodo per ottenere l'utente

            notificationService.sendOrderAcceptedNotification(user, orderId);

            return Response.ok("Ordine accettato con successo e notifica inviata.").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }


}
