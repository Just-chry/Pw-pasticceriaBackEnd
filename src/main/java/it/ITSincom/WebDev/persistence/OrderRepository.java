package it.ITSincom.WebDev.persistence;

import it.ITSincom.WebDev.persistence.model.Order;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class OrderRepository implements PanacheMongoRepository<Order> {
    public List<Order> findByUserId(String userId) {
        return find("user_id", userId).list();
    }

}