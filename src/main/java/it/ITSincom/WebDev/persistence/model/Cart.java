package it.ITSincom.WebDev.persistence.model;

import java.util.List;

public class Cart {
    private String userId;
    private List<OrderItem> items;

    public Cart() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
