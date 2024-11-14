package it.ITSincom.WebDev.service;

import it.ITSincom.WebDev.persistence.OrderRepository;
import it.ITSincom.WebDev.persistence.ProductRepository;
import it.ITSincom.WebDev.persistence.UserRepository;
import it.ITSincom.WebDev.persistence.UserSessionRepository;
import it.ITSincom.WebDev.persistence.model.*;
import it.ITSincom.WebDev.rest.model.OrderItemRequest;
import it.ITSincom.WebDev.rest.model.OrderRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.bson.types.ObjectId;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserSessionRepository userSessionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Inject
    public OrderService(OrderRepository orderRepository, UserSessionRepository userSessionRepository, ProductRepository productRepository, UserRepository userRepository, EntityManager entityManager) {
        this.orderRepository = orderRepository;
        this.userSessionRepository = userSessionRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    public List<Order> getUserOrders(String sessionId) throws Exception {
        Optional<UserSession> optionalUserSession = userSessionRepository.findBySessionId(sessionId);
        if (optionalUserSession.isEmpty()) {
            throw new Exception("Sessione non valida. Effettua il login.");
        }

        String userId = optionalUserSession.get().getUser().getId();
        List<Order> userOrders = orderRepository.findByUserId(userId);
        System.out.println("Recuperando gli ordini per UserID: " + userId);

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
                            throw new Exception("Nessun ordine trovato per l'utentess.");
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

        // Update the cart
        cart.setProducts(items);
        orderRepository.update(cart);
    }

    @Transactional
    public Order createOrderFromCart(String sessionId, OrderRequest orderRequest) throws Exception {
        Optional<UserSession> optionalUserSession = userSessionRepository.findBySessionId(sessionId);

        String userId = optionalUserSession.get().getUser().getId();
        Optional<Order> optionalCart = orderRepository.find("userId = ?1 and status = 'cart'", userId).firstResultOptional();

        if (optionalCart.isEmpty() || optionalCart.get().getProducts().isEmpty()) {
            throw new Exception("Il carrello è vuoto. Aggiungi prodotti prima di creare un ordine.");
        }

        Order cart = optionalCart.get();

        // Check if all product quantities are available
        for (OrderItem item : cart.getProducts()) {
            Optional<Product> productOptional = productRepository.findByIdOptional(item.getProductId());
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                if (product.getQuantity() < item.getQuantity()) {
                    throw new Exception("Prodotti insufficienti per il prodotto: " + product.getName() + " Quantità di prodotti rimanenti: " + product.getQuantity());
                }
            } else {
                throw new Exception("Prodotto non trovato per l'ID: " + item.getProductId());
            }
        }

        LocalDateTime pickupDateTime = LocalDateTime.of(orderRequest.getPickupDate(), orderRequest.getPickupTime());
        if (pickupDateTime.getDayOfWeek() == DayOfWeek.MONDAY) {
            throw new Exception("Gli ordini non possono essere effettuati il lunedì poiché siamo chiusi.");
        }
        if (!isPickupTimeValid(orderRequest.getPickupTime()) || isPickupSlotTaken(pickupDateTime)) {
            throw new Exception("La fascia oraria selezionata non è disponibile. Gli ordini possono essere fatti solo ogni 10 minuti dalle 9:00 alle 13:00 e dalle 15:00 alle 19:00.");
        }

        Order order = new Order(userId, pickupDateTime, orderRequest.getComments(), cart.getProducts(), "pending");
        orderRepository.persist(order);
        System.out.println("Ordine creato: ID = " + order.getId() + ", UserID = " + order.getUserId());

        // Decrease product quantities after the order is confirmed
        for (OrderItem item : cart.getProducts()) {
            Optional<Product> productOptional = productRepository.findByIdOptional(item.getProductId());
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                product.setQuantity(product.getQuantity() - item.getQuantity());
                productRepository.persist(product);
            } else {
                throw new Exception("Prodotto non trovato per l'ID: " + item.getProductId());
            }
        }

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
        // Recupera l'ordine
        Order order = getOrder(orderId);
        if (order == null) {
            throw new Exception("Ordine non trovato con ID: " + orderId);
        }

        // Controlla se l'ID dell'utente è nullo o vuoto
        String userId = order.getUserId();
        if (userId == null || userId.isEmpty()) {
            throw new Exception("ID dell'utente associato all'ordine è nullo o vuoto per l'ordine con ID: " + orderId);
        }

        // Log per debug
        System.out.println("Order ID: " + orderId);
        System.out.println("User ID: " + userId);

        // Cerca l'utente nel database
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


    @Transactional
    public void deleteProductFromCart(String sessionId, String productId) throws Exception {
        Optional<UserSession> optionalUserSession = userSessionRepository.findBySessionId(sessionId);

        String userId = optionalUserSession.get().getUser().getId();
        Optional<Order> optionalCart = orderRepository.find("userId = ?1 and status = 'cart'", userId).firstResultOptional();

        if (optionalCart.isEmpty()) {
            throw new Exception("Carrello non trovato.");
        }

        Order cart = optionalCart.get();
        List<OrderItem> items = cart.getProducts();
        boolean productFound = false;

        for (OrderItem item : items) {
            if (item.getProductId().equals(productId)) {
                // Restore the product quantity in the inventory
                Optional<Product> productOptional = productRepository.findByIdOptional(productId);
                if (productOptional.isPresent()) {
                    Product product = productOptional.get();
                    product.setQuantity(product.getQuantity() + item.getQuantity());
                    productRepository.persist(product);
                } else {
                    throw new Exception("Prodotto non trovato per l'ID: " + productId);
                }
                items.remove(item);
                productFound = true;
                break;
            }
        }

        if (!productFound) {
            throw new Exception("Prodotto non trovato nel carrello.");
        }

        // Update the cart
        cart.setProducts(items);
        orderRepository.update(cart);
    }

    @Transactional
    public void rejectOrder(String orderId) throws Exception {
        // Recupera l'ordine tramite il suo ID
        Order order = getOrder(orderId);

        // Verifica se l'ordine è nello stato 'pending'
        if (!"pending".equals(order.getStatus())) {
            throw new Exception("L'ordine non è in stato 'pending' e non può essere rifiutato.");
        }

        // Imposta lo stato dell'ordine su 'rejected'
        order.setStatus("rifiutato");

        // Aggiorna l'ordine nel repository
        orderRepository.update(order);
    }

    public Order getCartByUserSession(String sessionId) throws Exception {
        Optional<UserSession> optionalUserSession = userSessionRepository.findBySessionId(sessionId);
        if (optionalUserSession.isEmpty()) {
            throw new Exception("Sessione non valida. Effettua il login.");
        }

        String userId = optionalUserSession.get().getUser().getId();
        Optional<Order> optionalCart = orderRepository.find("userId = ?1 and status = 'cart'", userId).firstResultOptional();

        if (optionalCart.isEmpty()) {
            throw new Exception("Carrello vuoto o non trovato.");
        }

        Order cart = optionalCart.get();
        // Assicurati che ogni prodotto abbia valori completi
        for (OrderItem item : cart.getProducts()) {
            if (item.getProductId() != null) {
                Optional<Product> product = productRepository.findByIdOptional(item.getProductId());
                if (product.isPresent()) {
                    Product productDetails = product.get();
                    item.setProductName(productDetails.getName());
                    item.setPrice(productDetails.getPrice());
                } else {
                    throw new Exception("Prodotto non trovato: " + item.getProductId());
                }
            }
        }

        return cart;
    }

    public List<Order> getOrdersByDay(LocalDate parsedDate) {
        // Retrieve all orders and filter them by the given date
        return orderRepository.listAll().stream()
                .filter(order -> order.getPickupDateTime() != null && order.getPickupDateTime().toLocalDate().equals(parsedDate))
                .collect(Collectors.toList());
    }

    public List<Order> getOrdersByDate(LocalDate pickupDate) {
        return orderRepository.findByPickupDate(pickupDate);
    }

    // Metodo per ottenere tutti gli slot orari disponibili (e.g., ogni 10 minuti dalle 9 alle 13 e dalle 15 alle 19)
    public List<LocalTime> getAllPossibleSlots() {
        List<LocalTime> allSlots = new ArrayList<>();

        // Slot mattutini: dalle 9:00 alle 13:00
        LocalTime current = LocalTime.of(9, 0);
        LocalTime morningEnd = LocalTime.of(13, 0);
        while (current.isBefore(morningEnd)) {
            allSlots.add(current);
            current = current.plusMinutes(10);
        }

        // Slot pomeridiani: dalle 15:00 alle 19:00
        current = LocalTime.of(15, 0);
        LocalTime eveningEnd = LocalTime.of(19, 0);
        while (current.isBefore(eveningEnd)) {
            allSlots.add(current);
            current = current.plusMinutes(10);
        }

        return allSlots;
    }

    public List<LocalTime> getAvailableSlots(LocalDate pickupDate) {
        List<Order> existingOrders = getOrdersByDate(pickupDate);
        List<LocalTime> allSlots = getAllPossibleSlots();

        // Rimuovi gli orari già prenotati dalla lista degli orari disponibili
        List<LocalTime> availableSlots = allSlots.stream()
                .filter(slot -> existingOrders.stream().noneMatch(order ->
                        order.getPickupDateTime().toLocalDate().equals(pickupDate) && // Filtra sulla data
                                order.getPickupDateTime().toLocalTime().equals(slot))) // Filtra sull'orario
                .collect(Collectors.toList());

        return availableSlots;
    }
}
