package it.ITSincom.WebDev.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import it.ITSincom.WebDev.persistence.model.Product;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductRepository implements PanacheRepositoryBase<Product, String> {
    public void addIngredientToProduct(String productId, String ingredientId) {
        getEntityManager().createNativeQuery(
                        "INSERT INTO product_ingredient (product_id, ingredient_id) VALUES (?, ?)")
                .setParameter(1, productId)
                .setParameter(2, ingredientId)
                .executeUpdate();
    }

    public List<Product> findVisibleProducts() {
        return list("isVisible", true);
    }

    public List findIngredientNamesByProductId(String productId) {
        return getEntityManager().createNativeQuery(
                        "SELECT i.name FROM ingredient i " +
                                "JOIN product_ingredient pi ON i.id = pi.ingredient_id " +
                                "WHERE pi.product_id = :productId")
                .setParameter("productId", productId)
                .getResultList();
    }


    public void removeIngredientFromProduct(String productId, String ingredientId) {
        getEntityManager().createNativeQuery(
                        "DELETE FROM product_ingredient WHERE product_id = :productId AND ingredient_id = :ingredientId")
                .setParameter("productId", productId)
                .setParameter("ingredientId", ingredientId)
                .executeUpdate();
    }

    public List<Product> findByCategory(String category) {
        return findAll().stream()
                .filter(product -> product.getCategory().name().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

}
