package it.ITSincom.WebDev.rest.model;

public class OrderItemRequest {
    private String productId;
    private int quantity;

    // Constructors
    public OrderItemRequest() {
    }

    public OrderItemRequest(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}