package it.ITSincom.WebDev.service;

import it.ITSincom.WebDev.persistence.OrderRepository;
import it.ITSincom.WebDev.persistence.ProductRepository;
import it.ITSincom.WebDev.persistence.UserSessionRepository;
import it.ITSincom.WebDev.persistence.model.Order;
import it.ITSincom.WebDev.persistence.model.OrderItem;
import it.ITSincom.WebDev.persistence.model.Product;
import it.ITSincom.WebDev.persistence.model.UserSession;
import it.ITSincom.WebDev.rest.model.OrderRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserSessionRepository userSessionRepository;
    private final ProductRepository productRepository;

    @Inject
    public OrderService(OrderRepository orderRepository, UserSessionRepository userSessionRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userSessionRepository = userSessionRepository;
        this.productRepository = productRepository;
    }

    public List<Order> getUserOrders(String sessionId) throws Exception {
        Optional<UserSession> optionalUserSession = userSessionRepository.findBySessionId(sessionId);
        if (optionalUserSession.isEmpty()) {
            throw new Exception("Sessione non valida. Effettua il login.");
        }

        String userId = optionalUserSession.get().getUser().getId();
        List<Order> userOrders = orderRepository.findByUserId(userId);

        if (userOrders.isEmpty()) {
            throw new Exception("Nessun ordine trovato per l'utente.");
        }

        // Assicurati che ogni prodotto abbia valori completi
        for (Order order : userOrders) {
            if (order.getProducts() != null) {
                for (OrderItem item : order.getProducts()) {
                    if (item.getProductId() != null) {
                        Optional<Product> product = productRepository.findByIdOptional(item.getProductId());
                        if (product.isPresent()) {
                            Product productDetails = product.get();
                            item.setProductName(productDetails.getName());
                            item.setPrice(productDetails.getPrice());
                        } else {
                            System.out.println("Product not found for ID: " + item.getProductId());
                        }
                    }
                }
            } else {
                order.setProducts(new ArrayList<>()); // Imposta una lista vuota se products è null
            }
        }

        return userOrders;
    }


    public boolean isPickupSlotTaken(LocalDateTime pickupDateTime) {
        // Query the database to check if there is an existing order with the same pickup date and time
        List<Order> existingOrders = orderRepository.find("pickupDateTime = ?1", pickupDateTime).list();
        return !existingOrders.isEmpty();
    }


    public boolean isPickupTimeValid(LocalTime pickupTime) {
        return (pickupTime.isAfter(LocalTime.of(8, 59)) && pickupTime.isBefore(LocalTime.of(13, 1))) ||
                (pickupTime.isAfter(LocalTime.of(14, 59)) && pickupTime.isBefore(LocalTime.of(19, 1)));
    }


    public Order createOrder(String sessionId, OrderRequest orderRequest) {
        LocalDate pickupDate = orderRequest.getPickupDate();
        LocalTime pickupTime = orderRequest.getPickupTime();

        if (pickupDate.getDayOfWeek().getValue() == 1) {
            throw new IllegalArgumentException("Non è possibile effettuare un ordine di lunedì, siamo chiusi.");
        }
        if (!isPickupTimeValid(pickupTime)) {
            throw new IllegalArgumentException("L'orario selezionato non è valido. Gli orari disponibili sono dalle 9:00 alle 13:00 e dalle 15:00 alle 19:00.");
        }
        if (isPickupSlotTaken(LocalDateTime.of(pickupDate, pickupTime))) {
            throw new IllegalArgumentException("L'orario selezionato non è disponibile. Scegli un altro orario.");
        }

        // Create and save the new order
        Order order = new Order();
        // Set order details based on orderRequest and sessionId
        order.setUserId(sessionId);
        order.setPickupDateTime(LocalDateTime.of(pickupDate, pickupTime));
        order.setComments(orderRequest.getComments());
        List<OrderItem> orderItems = orderRequest.getProducts().stream().map(orderItemRequest ->
                new OrderItem(orderItemRequest.getProductId(), null, orderItemRequest.getQuantity(), 0.0)
        ).collect(Collectors.toList());
        order.setProducts(orderItems);
        order.setStatus("pending");
        order.persist();

        return order;
    }


}
