package it.ITSincom.WebDev.persistence;

import it.ITSincom.WebDev.persistence.model.Order;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class OrderRepository implements PanacheMongoRepository<Order> {
    public List<Order> findByUserId(String userId) {
        return find("userId", userId).list();
    }
    public List<Order> findByPickupDate(LocalDate pickupDate) {
        LocalDateTime startOfDay = pickupDate.atStartOfDay();
        LocalDateTime endOfDay = pickupDate.plusDays(1).atStartOfDay();
        return find("{'pickupDateTime': {$gte: ?1, $lt: ?2}}", startOfDay, endOfDay).list();
    }

}