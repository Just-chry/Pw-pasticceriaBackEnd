package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.persistence.model.Order;
import it.ITSincom.WebDev.rest.model.OrderItemRequest;
import it.ITSincom.WebDev.rest.model.OrderRequest;
import it.ITSincom.WebDev.service.AuthenticationService;
import it.ITSincom.WebDev.service.NotificationService;
import it.ITSincom.WebDev.service.OrderService;
import it.ITSincom.WebDev.service.exception.UserSessionNotFoundException;
import it.ITSincom.WebDev.util.ValidationUtils;
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
    @Path("/add")
    public Response addToCart(@CookieParam("sessionId") String sessionId, OrderItemRequest itemRequest) {
        try {
            ValidationUtils.validateSessionId(sessionId);
            orderService.addToCart(sessionId, itemRequest);
            return Response.ok("Prodotto aggiunto al carrello con successo.").build();
        } catch (UserSessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
    @DELETE
    @Path("/delete/{productId}")
    public Response deleteProductFromCart(@CookieParam("sessionId") String sessionId, @PathParam("productId") String productId) {
        try {
            ValidationUtils.validateSessionId(sessionId);
            orderService.deleteProductFromCart(sessionId, productId);
            return Response.ok("Prodotto rimosso dal carrello con successo.").build();
        } catch (UserSessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }


    @POST
    @Path("/create")
    public Response createOrderFromCart(@CookieParam("sessionId") String sessionId, OrderRequest orderRequest) {
        try {
            ValidationUtils.validateSessionId(sessionId);
            Order order = orderService.createOrderFromCart(sessionId, orderRequest);
            notificationService.sendNewOrderNotificationToAdmin(order);
            return Response.ok("Ordine creato con successo.").build();
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
            List<Order> userOrders = orderService.getUserOrders(sessionId);
            return Response.ok(userOrders).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/all")
    public Response getAllOrders(@CookieParam("sessionId") String sessionId) {
        try {
            authenticationService.isAdmin(sessionId);
            List<Order> allOrders = orderService.getAllOrders();
            return Response.ok(allOrders).build();
        } catch (UserSessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

}
