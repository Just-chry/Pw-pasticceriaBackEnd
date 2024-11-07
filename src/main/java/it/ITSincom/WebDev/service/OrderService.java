package it.ITSincom.WebDev.service;

import it.ITSincom.WebDev.persistence.OrderRepository;
import it.ITSincom.WebDev.persistence.ProductRepository;
import it.ITSincom.WebDev.persistence.UserSessionRepository;
import it.ITSincom.WebDev.persistence.model.Order;
import it.ITSincom.WebDev.persistence.model.OrderItem;
import it.ITSincom.WebDev.persistence.model.Product;
import it.ITSincom.WebDev.persistence.model.UserSession;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

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

        // Popola i dettagli del prodotto in ogni ordine
        for (Order order : userOrders) {
            for (OrderItem item : order.getProducts()) {
                if (item.getProductId() == null) {
                    System.out.println("Product ID is null for one of the order items.");
                    continue;
                }

                Product product = productRepository.findById(item.getProductId());
                if (product != null) {
                    item.setProductName(product.getName());
                    item.setPrice(product.getPrice());
                } else {
                    System.out.println("Product not found for ID: " + item.getProductId());
                }
            }
        }

        return userOrders;
    }
}
