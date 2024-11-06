package it.ITSincom.WebDev.persistence;

import it.ITSincom.WebDev.persistence.model.Order;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrderRepository implements PanacheMongoRepository<Order> {
    // Puoi aggiungere metodi personalizzati qui, se necessario
}