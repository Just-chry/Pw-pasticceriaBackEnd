package it.ITSincom.WebDev.persistence.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.types.ObjectId;
import java.time.LocalDateTime;
import java.util.List;

@MongoEntity(collection = "orders")
public class Order extends PanacheMongoEntity {
    private ObjectId id;
    private String userId;
    private LocalDateTime pickupDateTime;
    private String comments;
    private List<OrderItem> products;
    private String status;

    // Constructors
    public Order() {
    }

    public Order(String userId, LocalDateTime pickupDateTime, String comments, List<OrderItem> products, String status) {
        this.userId = userId;
        this.pickupDateTime = pickupDateTime;
        this.comments = comments;
        this.products = products;
        this.status = status;
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

    public LocalDateTime getPickupDateTime() {
        return pickupDateTime;
    }

    public void setPickupDateTime(LocalDateTime pickupDateTime) {
        this.pickupDateTime = pickupDateTime;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<OrderItem> getProducts() {
        return products;
    }

    public void setProducts(List<OrderItem> products) {
        this.products = products;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

