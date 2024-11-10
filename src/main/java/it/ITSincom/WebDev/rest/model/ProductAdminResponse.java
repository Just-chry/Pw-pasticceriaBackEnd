package it.ITSincom.WebDev.rest.model;

import java.util.List;

public class ProductAdminResponse extends ProductResponse {
    private String id;        // ID visibile solo per admin
    private Integer quantity; // Quantità visibile solo per admin
    private Boolean is_visible; // Visibilità del prodotto

    // Costruttore con `id` e `quantity`
    public ProductAdminResponse(String id, String name, String description, String image, Double price, String category, List<String> ingredients, Integer quantity, Boolean is_visible) {
        super(name, description, image, price, category, ingredients);
        this.id = id;
        this.quantity = quantity;
        this.is_visible = is_visible;
    }

    // Getter e setter per `id` e `quantity`
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Boolean getIs_visible() {
        return is_visible;
    }

    public void setIs_visible(Boolean is_visible) {
        this.is_visible = is_visible;
    }
}
