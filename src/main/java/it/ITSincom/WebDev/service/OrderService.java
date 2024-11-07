package it.ITSincom.WebDev.service;

import it.ITSincom.WebDev.persistence.OrderRepository;
import it.ITSincom.WebDev.persistence.PickUpSlotRepository;
import it.ITSincom.WebDev.persistence.ProductRepository;
import it.ITSincom.WebDev.persistence.UserSessionRepository;
import it.ITSincom.WebDev.persistence.model.Order;
import it.ITSincom.WebDev.persistence.model.OrderItem;
import it.ITSincom.WebDev.persistence.model.Product;
import it.ITSincom.WebDev.persistence.model.UserSession;
import it.ITSincom.WebDev.rest.model.OrderRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserSessionRepository userSessionRepository;
    private final ProductRepository productRepository;
    private final PickUpSlotRepository pickUpSlotRepository;

    @Inject
    public OrderService(OrderRepository orderRepository, UserSessionRepository userSessionRepository, ProductRepository productRepository, PickUpSlotRepository pickupSlotRepository) {
        this.orderRepository = orderRepository;
        this.userSessionRepository = userSessionRepository;
        this.productRepository = productRepository;
        this.pickUpSlotRepository = pickupSlotRepository;
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


    public Order createOrder(String sessionId, OrderRequest orderRequest) throws Exception {
        // Controlla la disponibilità dello slot
        String dayOfWeek = getDayOfWeek(orderRequest.getPickupDate());
        boolean isSlotAvailable = pickUpSlotRepository.isSlotAvailable(orderRequest.getPickupDate(), orderRequest.getPickupTime());

        if (!isSlotAvailable) {
            throw new Exception("Lo slot selezionato non è disponibile. Scegli un altro orario.");
        }

        // Aggiorna la disponibilità dello slot
        pickUpSlotRepository.bookSlot(orderRequest.getPickupDate(), orderRequest.getPickupTime());

        // Crea un nuovo ordine
        Order newOrder = new Order();
        newOrder.setUserId(getUserIdFromSession(sessionId));

        // Ottieni la data e l'orario e combinali
        LocalDate pickupDate = LocalDate.parse(orderRequest.getPickupDate());
        LocalTime pickupTime = LocalTime.parse(orderRequest.getPickupTime());
        LocalDateTime pickupDateTime = LocalDateTime.of(pickupDate, pickupTime);

        newOrder.setPickupDate(pickupDateTime);
        newOrder.setComments(orderRequest.getComments());
        newOrder.setStatus("pending");

        List<OrderItem> orderItems = orderRequest.getProducts().stream()
                .map(item -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProductId(item.getProductId());
                    orderItem.setQuantity(item.getQuantity());

                    Optional<Product> productOpt = productRepository.findByIdOptional(item.getProductId());
                    if (productOpt.isPresent()) {
                        Product product = productOpt.get();
                        orderItem.setProductName(product.getName());
                        orderItem.setPrice(product.getPrice());
                    }

                    return orderItem;
                })
                .collect(Collectors.toList());
        newOrder.setProducts(orderItems);

        orderRepository.persist(newOrder);

        return newOrder;
    }


    private String getDayOfWeek(String pickupDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(pickupDate, formatter).getDayOfWeek().toString();
    }

    private String getUserIdFromSession(String sessionId) {
        Optional<UserSession> session = userSessionRepository.findBySessionId(sessionId);

        if (session.isEmpty()) {
            throw new RuntimeException("Sessione non trovata o non valida.");
        }

        return session.get().getUser().getId().toString(); // Ottieni l'ID dell'utente dalla sessione
    }
}
