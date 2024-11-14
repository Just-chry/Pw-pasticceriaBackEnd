package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.persistence.model.Order;
import it.ITSincom.WebDev.persistence.model.User;
import it.ITSincom.WebDev.persistence.model.UserSession;
import it.ITSincom.WebDev.rest.model.OrderItemRequest;
import it.ITSincom.WebDev.rest.model.OrderRequest;
import it.ITSincom.WebDev.service.AuthenticationService;
import it.ITSincom.WebDev.service.NotificationService;
import it.ITSincom.WebDev.service.OrderService;
import it.ITSincom.WebDev.service.exception.UserSessionNotFoundException;
import it.ITSincom.WebDev.util.Validation;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


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
            validateSessionAndGetUser(sessionId);
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
            validateSessionAndGetUser(sessionId);
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
            validateSessionAndGetUser(sessionId);
            Order order = orderService.createOrderFromCart(sessionId, orderRequest);
            notificationService.sendNewOrderNotificationToAdmin(order);
            return Response.ok("Ordine creato con successo.").build();
        } catch (UserSessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/cart")
    public Response getCartByUser(@CookieParam("sessionId") String sessionId) {
        try {
            Order cart = orderService.getCartByUserSession(sessionId);
            return Response.ok(cart).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }


    @DELETE
    @Path("/cancel/{orderId}")
    public Response deleteOrder(@CookieParam("sessionId") String sessionId, @PathParam("orderId") String orderId) {
        try {
            validateSessionAndGetUser(sessionId);

            // Recupera l'ordine PRIMA di cancellarlo per inviare la notifica
            Order order = orderService.getOrder(orderId);

            // Invia la notifica di cancellazione dell'ordine
            notificationService.sendOrderCancelledNotificationToAdmin(order);

            // Cancella l'ordine
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

    @GET
    @Path("/day/{date}")
    public Response getOrdersByDay(@CookieParam("sessionId") String sessionId, @PathParam("date") String date) {
        try {
            // Validate if the user has admin privileges
            authenticationService.isAdmin(sessionId);

            // Trim any extra spaces from the date string
            String trimmedDate = date.trim();

            // Parse the date from the path parameter
            LocalDate parsedDate = LocalDate.parse(trimmedDate, DateTimeFormatter.ISO_LOCAL_DATE);

            // Retrieve orders by the given date from the service
            List<Order> orders = orderService.getOrdersByDay(parsedDate);

            return Response.ok(orders).build();
        } catch (UserSessionNotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }


    private User validateSessionAndGetUser(String sessionId) throws UserSessionNotFoundException {
        // Recupera la UserSession e l'utente
        UserSession userSession = authenticationService.findUserSessionBySessionId(sessionId);
        User user = (userSession != null) ? userSession.getUser() : null;

        // Esegui la validazione della sessione e dell'utente
        Validation.validateSessionAndUser(sessionId, userSession, user);

        return user;
    }


    @PUT
    @Path("/accept/{orderId}")
    public Response acceptOrder(@CookieParam("sessionId") String sessionId, @PathParam("orderId") String orderId) {
        try {
            authenticationService.isAdmin(sessionId);
            orderService.acceptOrder(orderId);
            User user = orderService.getUserByOrderId(orderId);

            notificationService.sendOrderAcceptedNotification(user, orderId);

            return Response.ok("Ordine accettato con successo e notifica inviata.").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/reject/{orderId}")
    public Response rejectOrder(@CookieParam("sessionId") String sessionId, @PathParam("orderId") String orderId) {
        try {
            // Verifica se l'utente è un amministratore
            authenticationService.isAdmin(sessionId);

            // Rifiuta l'ordine usando il servizio OrderService
            orderService.rejectOrder(orderId);

            // Recupera l'utente associato all'ordine
            User user = orderService.getUserByOrderId(orderId);

            // Invia una notifica all'utente che l'ordine è stato rifiutato
            notificationService.sendOrderRejectedNotification(user, orderId);

            // Restituisci una risposta di successo
            return Response.ok("Ordine rifiutato con successo e notifica inviata.").build();
        } catch (Exception e) {
            // Gestione dell'eccezione
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/notify/{orderId}")
    public Response sendOrderNotification(@CookieParam("sessionId") String sessionId, @PathParam("orderId") String orderId) {
        try {
            authenticationService.isAdmin(sessionId);

            Order order = orderService.getOrder(orderId);

            User user = orderService.getUserByOrderId(orderId);

            notificationService.sendOrderInCartNotification(user, order);

            return Response.ok("Notifica inviata con successo").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/available-slots")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAvailableSlots(@QueryParam("date") String date) {
        try {
            LocalDate selectedDate = LocalDate.parse(date);
            List<LocalTime> availableSlots = orderService.getAvailableSlots(selectedDate);

            return Response.ok(availableSlots).build();
        } catch (DateTimeParseException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Formato della data non valido. Utilizza il formato YYYY-MM-DD.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore durante il recupero degli slot disponibili. Riprova più tardi.")
                    .build();
        }
    }

}







