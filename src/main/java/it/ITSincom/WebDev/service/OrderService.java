package it.ITSincom.WebDev.service;

import it.ITSincom.WebDev.persistence.OrderRepository;
import it.ITSincom.WebDev.persistence.ProductRepository;
import it.ITSincom.WebDev.persistence.UserRepository;
import it.ITSincom.WebDev.persistence.UserSessionRepository;
import it.ITSincom.WebDev.persistence.model.*;
import it.ITSincom.WebDev.rest.model.OrderItemRequest;
import it.ITSincom.WebDev.rest.model.OrderRequest;
import it.ITSincom.WebDev.util.ValidationUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.bson.types.ObjectId;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserSessionRepository userSessionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Inject
    public OrderService(OrderRepository orderRepository, UserSessionRepository userSessionRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userSessionRepository = userSessionRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
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

    @Transactional
    public void addToCart(String sessionId, OrderItemRequest itemRequest) throws Exception {
        ValidationUtils.validateSessionId(sessionId);
        Optional<UserSession> optionalUserSession = userSessionRepository.findBySessionId(sessionId);

        String userId = optionalUserSession.get().getUser().getId();
        Optional<Order> optionalCart = orderRepository.find("userId = ?1 and status = 'cart'", userId).firstResultOptional();
        Order cart = optionalCart.orElseGet(() -> {
            Order newCart = new Order(userId, null, null, new ArrayList<>(), "cart");
            orderRepository.persist(newCart);
            return newCart;
        });

        // Check if the product exists
        Optional<Product> productOptional = productRepository.findByIdOptional(itemRequest.getProductId());
        if (productOptional.isPresent() && !productOptional.get().getIsVisible()) {
            throw new Exception("Il prodotto non è disponibile per l'acquisto: " + itemRequest.getProductId());
        }
        if (productOptional.isEmpty()) {
            throw new Exception("Prodotto non trovato per l'ID: " + itemRequest.getProductId());
        }

        Product product = productOptional.get();
        if (product.getQuantity() < itemRequest.getQuantity()) {
            throw new Exception("Quantità richiesta non disponibile per il prodotto: " + product.getName());
        }

        OrderItem newItem = new OrderItem(itemRequest.getProductId(), product.getName(), itemRequest.getQuantity(), product.getPrice());

        // Add or update item in cart
        List<OrderItem> items = cart.getProducts();
        boolean itemUpdated = false;
        for (OrderItem item : items) {
            if (item.getProductId().equals(newItem.getProductId())) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                itemUpdated = true;
                break;
            }
        }

        if (!itemUpdated) {
            items.add(newItem);
        }

        // Update product quantity in inventory
        product.setQuantity(product.getQuantity() - newItem.getQuantity());
        productRepository.persist(product);

        // Update the cart
        cart.setProducts(items);
        orderRepository.update(cart);
    }



    @Transactional
    public Order createOrderFromCart(String sessionId, OrderRequest orderRequest) throws Exception {
        ValidationUtils.validateSessionId(sessionId);
        Optional<UserSession> optionalUserSession = userSessionRepository.findBySessionId(sessionId);

        String userId = optionalUserSession.get().getUser().getId();
        Optional<Order> optionalCart = orderRepository.find("userId = ?1 and status = 'cart'", userId).firstResultOptional();

        if (optionalCart.isEmpty() || optionalCart.get().getProducts().isEmpty()) {
            throw new Exception("Il carrello è vuoto. Aggiungi prodotti prima di creare un ordine.");
        }

        LocalDateTime pickupDateTime = LocalDateTime.of(orderRequest.getPickupDate(), orderRequest.getPickupTime());
        if (!isPickupTimeValid(orderRequest.getPickupTime()) || isPickupSlotTaken(pickupDateTime)) {
            throw new Exception("La fascia oraria selezionata non è disponibile. Gli ordini possono essere fatti solo ogni 10 minuti dalle 9:00 alle 13:00 e dalle 15:00 alle 19:00.");
        }

        Order cart = optionalCart.get();
        Order order = new Order(userId, pickupDateTime, orderRequest.getComments(), cart.getProducts(), "pending");
        orderRepository.persist(order);

        // Clear the cart after order is created by deleting it
        orderRepository.delete(cart);
        return order;
    }

    @Transactional
    public void acceptOrder(String orderId) throws Exception {
        // Retrieve the order by its ID
        Order order = getOrder(orderId);
        if (!"pending".equals(order.getStatus())) {
            throw new Exception("L'ordine non è in stato 'pending' e non può essere accettato.");
        }

        order.setStatus("accepted");
        orderRepository.update(order);
    }

    @Transactional
    public void deleteOrder(String sessionId, String orderId) throws Exception {
        // Controlla che la sessione sia valida e ottieni l'userId
        Optional<UserSession> optionalUserSession = userSessionRepository.findBySessionId(sessionId);
        if (optionalUserSession.isEmpty()) {
            throw new Exception("Sessione non valida. Effettua il login.");
        }
        String userId = optionalUserSession.get().getUser().getId();

        // Converti l'orderId in ObjectId
        Order order = getOrder(orderId);

        // Controlla che l'ordine appartenga all'utente che sta tentando di cancellarlo
        if (!order.getUserId().equals(userId)) {
            throw new Exception("Non sei autorizzato a cancellare questo ordine.");
        }

        // Ripristina le quantità dei prodotti
        for (OrderItem item : order.getProducts()) {
            Optional<Product> productOptional = productRepository.findByIdOptional(item.getProductId());
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                product.setQuantity(product.getQuantity() + item.getQuantity()); // Ripristina la quantità
                productRepository.persist(product);
            } else {
                throw new Exception("Prodotto non trovato per l'ID: " + item.getProductId());
            }
        }

        // Cancella l'ordine
        orderRepository.delete(order);
    }

    public User getUserByOrderId(String orderId) throws Exception {
        // Convert the orderId from String to ObjectId
        Order order = getOrder(orderId);
        String userId = order.getUserId();
        Optional<User> optionalUser = userRepository.findByIdOptional(userId);
        if (optionalUser.isEmpty()) {
            throw new Exception("Utente non trovato per l'ordine con ID: " + orderId);
        }

        return optionalUser.get();
    }

    public Order getOrder(String orderId) throws Exception {
        ObjectId objectId;
        try {
            objectId = new ObjectId(orderId);
        } catch (IllegalArgumentException e) {
            throw new Exception("ID ordine non valido: " + orderId);
        }

        // Recupera l'ordine dal database
        Optional<Order> optionalOrder = orderRepository.findByIdOptional(objectId);
        if (optionalOrder.isEmpty()) {
            throw new Exception("Ordine non trovato con ID: " + orderId);
        }

        return optionalOrder.get();
    }

    public List<Order> getAllOrders() {
        return orderRepository.listAll();
    }

    public boolean isPickupSlotTaken(LocalDateTime pickupDateTime) {
        // Query the database to check if there is an existing order with the same pickup date and time
        List<Order> existingOrders = orderRepository.find("pickupDateTime = ?1", pickupDateTime).list();
        return !existingOrders.isEmpty();
    }


    public boolean isPickupTimeValid(LocalTime pickupTime) {
        boolean isValidTimeRange = (pickupTime.isAfter(LocalTime.of(8, 59)) && pickupTime.isBefore(LocalTime.of(13, 1)))
                || (pickupTime.isAfter(LocalTime.of(14, 59)) && pickupTime.isBefore(LocalTime.of(19, 1)));

        boolean isValidSlot = pickupTime.getMinute() % 10 == 0;

        return isValidTimeRange && isValidSlot;
    }



}
