package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.persistence.model.Order;
import it.ITSincom.WebDev.rest.model.OrderRequest;
import it.ITSincom.WebDev.service.OrderService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;


@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    private final OrderService orderService;

    @Inject
    public OrderResource(OrderService orderService) {
        this.orderService = orderService;
    }

    @POST
    @Path("/create")
    public Response createOrder(@CookieParam("sessionId") Cookie sessionIdCookie, OrderRequest orderRequest) {
        if (sessionIdCookie == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Utente non autenticato. Effettua il login.").build();
        }

        String sessionId = sessionIdCookie.getValue();

        try {
            Order newOrder = orderService.createOrder(sessionId, orderRequest);
            return Response.ok(newOrder).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }


    @GET
    public Response getOrdersByUser(@CookieParam("sessionId") Cookie sessionIdCookie) {
        if (sessionIdCookie == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Utente non autenticato. Effettua il login.").build();
        }

        String sessionId = sessionIdCookie.getValue();

        try {
            List<Order> userOrders = orderService.getUserOrders(sessionId);
            return Response.ok(userOrders).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }


}
