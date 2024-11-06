package it.ITSincom.WebDev.persistence.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "ingredient")
public class Ingredient {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @Column(nullable = false)
    private String name;

    public Ingredient() {
        this.id = UUID.randomUUID().toString();
    }

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
}
