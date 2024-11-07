package it.ITSincom.WebDev.rest.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class OrderRequest {
    private LocalDate pickupDate;  // Data di ritiro come LocalDate
    private LocalTime pickupTime;  // Orario di ritiro come LocalTime
    private String comments;
    private List<OrderItemRequest> products;

    // Getters e Setters
    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(LocalDate pickupDate) {
        this.pickupDate = pickupDate;
    }

    public LocalTime getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(LocalTime pickupTime) {
        this.pickupTime = pickupTime;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<OrderItemRequest> getProducts() {
        return products;
    }

    public void setProducts(List<OrderItemRequest> products) {
        this.products = products;
    }
}
