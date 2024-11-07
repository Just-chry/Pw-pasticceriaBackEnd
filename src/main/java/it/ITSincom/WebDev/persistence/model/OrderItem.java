package it.ITSincom.WebDev.persistence.model;

public class OrderItem {
    private String productId;
    private int quantity;
    private String productName; // Added field for product name
    private double price; // Added field for price

    // No-argument constructor
    public OrderItem() {
    }

    // Full constructor
    public OrderItem(String productId, int quantity, String productName, double price) {
        this.productId = productId;
        this.quantity = quantity;
        this.productName = productName;
        this.price = price;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
