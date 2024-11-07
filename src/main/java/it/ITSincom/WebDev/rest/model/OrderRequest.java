package it.ITSincom.WebDev.rest.model;

import java.util.List;

public class OrderRequest {
    private String pickupDate; // Data di ritiro nel formato "yyyy-MM-dd"
    private String pickupTime; // Orario di ritiro nel formato "HH:mm"
    private String comments;
    private List<OrderItemRequest> products;

    // Getters e Setters
    public String getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(String pickupDate) {
        this.pickupDate = pickupDate;
    }

    public String getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(String pickupTime) {
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

