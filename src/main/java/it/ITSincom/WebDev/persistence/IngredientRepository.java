package it.ITSincom.WebDev.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import it.ITSincom.WebDev.persistence.model.Ingredient;

@ApplicationScoped
public class IngredientRepository implements PanacheRepository<Ingredient> {
    // Con PanacheRepository puoi già usare metodi come find, persist, delete, ecc.
}
