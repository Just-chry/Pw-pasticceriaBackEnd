package it.ITSincom.WebDev.persistence.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.types.ObjectId;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@MongoEntity(collection = "orders")
public class Order extends PanacheMongoEntity {
    private ObjectId id;
    private String userId;
    private List<OrderItem> products = new ArrayList<>();  // Inizializza con una lista vuota per evitare valori null
    private LocalDateTime pickupDate;
    private String status;
    private String comments;

    // No-argument constructor required by Panache
    public Order() {
    }

    public Order(String userId, List<OrderItem> products, LocalDateTime pickupDate, String status, String comments) {
        this.userId = userId;
        this.products = products;
        this.pickupDate = pickupDate;
        this.status = status;
        this.comments = comments;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<OrderItem> getProducts() {
        return products;
    }

    public void setProducts(List<OrderItem> products) {
        this.products = products;
    }

    public LocalDateTime getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(LocalDateTime pickupDate) {
        this.pickupDate = pickupDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
