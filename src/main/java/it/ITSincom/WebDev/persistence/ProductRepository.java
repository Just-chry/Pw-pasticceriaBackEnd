package it.ITSincom.WebDev.persistence;

import it.ITSincom.WebDev.persistence.model.Product;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {
    public void addIngredientToProduct(Long productId, Long ingredientId) {
        getEntityManager().createNativeQuery(
                        "INSERT INTO product_ingredient (product_id, ingredient_id) VALUES (?, ?)")
                .setParameter(1, productId)
                .setParameter(2, ingredientId)
                .executeUpdate();
    }

    public List<Product> findVisibleProducts() {
        return list("isVisible", true);
    }

    public List<String> findIngredientNamesByProductId(Long productId) {
        return getEntityManager().createNativeQuery(
                        "SELECT i.name FROM ingredient i " +
                                "JOIN product_ingredient pi ON i.id = pi.ingredient_id " +
                                "WHERE pi.product_id = :productId")
                .setParameter("productId", productId)
                .getResultList();
    }


}
