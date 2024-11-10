package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.persistence.model.Order;
import it.ITSincom.WebDev.rest.model.OrderRequest;
import it.ITSincom.WebDev.service.AuthenticationService;
import it.ITSincom.WebDev.service.NotificationService;
import it.ITSincom.WebDev.service.OrderService;
import it.ITSincom.WebDev.util.ValidationUtils;
import it.ITSincom.WebDev.service.exception.UserSessionNotFoundException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    private final OrderService orderService;
    private final NotificationService notificationService;
    private final AuthenticationService authenticationService;

    @Inject
    public OrderResource(OrderService orderService, NotificationService notificationService, AuthenticationService authenticationService) {
        this.orderService = orderService;
        this.notificationService = notificationService;
        this.authenticationService = authenticationService;
    }

    @POST
    @Path("/create")
    public Response createOrder(@CookieParam("sessionId") String sessionId, OrderRequest orderRequest) {
        try {
            ValidationUtils.validateSessionId(sessionId);
            Order newOrder = orderService.createOrder(sessionId, orderRequest);
            notificationService.sendNewOrderNotificationToAdmin(newOrder);
            return Response.ok(newOrder).build();
        } catch (UserSessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/cancel/{orderId}")
    public Response deleteOrder(@CookieParam("sessionId") String sessionId, @PathParam("orderId") String orderId) {
        try {
            ValidationUtils.validateSessionId(sessionId);
            orderService.deleteOrder(sessionId, orderId);
            return Response.ok("Ordine cancellato con successo.").build();
        } catch (UserSessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    public Response getOrdersByUser(@CookieParam("sessionId") String sessionId) {
        try {
            ValidationUtils.validateSessionId(sessionId);
            List<Order> userOrders = orderService.getUserOrders(sessionId);
            return Response.ok(userOrders).build();
        } catch (UserSessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/all")
    public Response getAllOrders(@CookieParam("sessionId") String sessionId) {
        try {
            ValidationUtils.validateSessionId(sessionId);
            // Assuming only admin users can access this endpoint, you could add an additional check for admin role here.
            if (!authenticationService.isAdmin(sessionId)) {
                return Response.status(Response.Status.FORBIDDEN).entity("Accesso negato. Funzionalità riservata agli amministratori.").build();
            }
            List<Order> allOrders = orderService.getAllOrders();
            return Response.ok(allOrders).build();
        } catch (UserSessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

}
