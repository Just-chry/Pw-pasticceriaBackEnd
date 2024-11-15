package it.ITSincom.WebDev.rest.model;

import java.util.List;

public class ProductAdminResponse extends ProductResponse {
    private Integer quantity;
    private Boolean is_visible;

    public ProductAdminResponse(String id, String name, String description, String image, Double price, String category, List<String> ingredients, Integer quantity, Boolean is_visible) {
        super(id,name, description, image, price, category, ingredients, quantity);
        this.quantity = quantity;
        this.is_visible = is_visible;
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
