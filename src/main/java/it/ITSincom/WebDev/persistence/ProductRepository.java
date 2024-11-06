package it.ITSincom.WebDev.persistence;

import it.ITSincom.WebDev.persistence.model.Product;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {
    public void addIngredientToProduct(Long productId, Long ingredientId) {
        getEntityManager().createNativeQuery(
                        "INSERT INTO product_ingredient (product_id, ingredient_id) VALUES (?, ?)")
                .setParameter(1, productId)
                .setParameter(2, ingredientId)
                .executeUpdate();
    }
}
