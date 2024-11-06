package it.ITSincom.WebDev.persistence.model;

import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "product")
public class Product {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;
    private String name;
    private String description;
    private String image;
    private Double price;
    private Integer quantity;

    @Column(name = "is_visible", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isVisible;

    @Enumerated(EnumType.STRING)
    private Category category;
    @Transient
    private List<String> ingredientNames;

    public Product() {this.id = UUID.randomUUID().toString();}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Boolean getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<String> getIngredientNames() {
        return ingredientNames;
    }

    public void setIngredientNames(List<String> ingredientNames) {
        this.ingredientNames = ingredientNames;
    }

    public enum Category {
        Macarons,
        Cookies,
        Jams,
        Bars,
        Cakes;

        public static Category fromString(String value) {
            for (Category category : Category.values()) {
                if (category.name().equalsIgnoreCase(value)) {
                    return category;
                }
            }
            throw new IllegalArgumentException("No enum constant for value: " + value);
        }
    }
}
