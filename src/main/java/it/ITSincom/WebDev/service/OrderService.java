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
import jakarta.transaction.Transactional;

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

    @Transactional
    public Order createOrder(String sessionId, OrderRequest orderRequest) {
        LocalDate pickupDate = orderRequest.getPickupDate();
        LocalTime pickupTime = orderRequest.getPickupTime();

        Optional<UserSession> optionalUserSession = userSessionRepository.findBySessionId(sessionId);
        if (optionalUserSession.isEmpty()) {
            throw new IllegalArgumentException("Sessione non valida. Effettua il login.");
        }
        // Controllo se il giorno è lunedì (lunedì è chiuso)
        if (pickupDate.getDayOfWeek().getValue() == 1) {
            throw new IllegalArgumentException("Non è possibile effettuare un ordine di lunedì, siamo chiusi.");
        }

        // Controllo se l'orario è valido
        if (!isPickupTimeValid(pickupTime)) {
            throw new IllegalArgumentException("L'orario selezionato non è valido. Gli orari disponibili sono dalle 9:00 alle 13:00 e dalle 15:00 alle 19:00.");
        }

        // Controllo se l'orario di quel giorno è già stato prenotato
        if (isPickupSlotTaken(LocalDateTime.of(pickupDate, pickupTime))) {
            throw new IllegalArgumentException("L'orario selezionato non è disponibile. Scegli un altro orario.");
        }

        String userId = optionalUserSession.get().getUser().getId();
        // Crea e salva il nuovo ordine
        Order order = new Order();
        order.setUserId(userId);
        order.setPickupDateTime(LocalDateTime.of(pickupDate, pickupTime));
        order.setComments(orderRequest.getComments());

        // Convert OrderItemRequest to OrderItem
        List<OrderItem> orderItems = orderRequest.getProducts().stream().map(orderItemRequest -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(orderItemRequest.getProductId());
            orderItem.setQuantity(orderItemRequest.getQuantity());

            // Recupera i dettagli del prodotto dal database
            Optional<Product> productOptional = productRepository.findByIdOptional(orderItemRequest.getProductId());
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                // Controlla se la quantità richiesta è disponibile
                if (product.getQuantity() < orderItemRequest.getQuantity()) {
                    throw new IllegalArgumentException("Quantità insufficiente per il prodotto: " + product.getName());
                }

                // Aggiorna la quantità disponibile del prodotto
                product.setQuantity(product.getQuantity() - orderItemRequest.getQuantity());
                productRepository.persist(product);

                orderItem.setProductName(product.getName());
                orderItem.setPrice(product.getPrice());
            } else {
                throw new IllegalArgumentException("Prodotto non trovato per l'ID: " + orderItemRequest.getProductId());
            }

            return orderItem;
        }).collect(Collectors.toList());

        order.setProducts(orderItems);
        order.setStatus("pending");

        // Salva l'ordine nel database
        order.persist();

        return order;
    }




}
