package it.ITSincom.WebDev.service;

import it.ITSincom.WebDev.persistence.IngredientRepository;
import it.ITSincom.WebDev.persistence.ProductRepository;
import it.ITSincom.WebDev.persistence.model.Ingredient;
import it.ITSincom.WebDev.persistence.model.Product;
import it.ITSincom.WebDev.service.exception.EntityNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;

import java.util.List;

@ApplicationScoped
public class ProductService {
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;

    @Inject
    public ProductService(ProductRepository productRepository, IngredientRepository ingredientRepository) {
        this.productRepository = productRepository;
        this.ingredientRepository = ingredientRepository;
    }

    public List<Product> getAllProducts() {
        List<Product> products = productRepository.listAll();
        if (products == null || products.isEmpty()) {
            throw new BadRequestException("Nessun prodotto trovato.");
        }
        return products;
    }

    @Transactional
    public void addProduct(Product product) {
        // Salva il prodotto nella tabella 'product'
        productRepository.persist(product);

        // Dopo aver salvato il prodotto, aggiungi gli ingredienti associati
        if (product.getIngredientNames() != null && !product.getIngredientNames().isEmpty()) {
            for (String ingredientName : product.getIngredientNames()) {
                // Cerca se l'ingrediente esiste già
                Ingredient ingredient = ingredientRepository.find("name", ingredientName).firstResult();

                // Se l'ingrediente non esiste, crealo
                if (ingredient == null) {
                    ingredient = new Ingredient();
                    ingredient.setName(ingredientName);
                    ingredientRepository.persist(ingredient);
                }

                // Associa l'ingrediente al prodotto nella tabella di join 'product_ingredient'
                ingredientRepository.getEntityManager().createNativeQuery(
                                "INSERT INTO product_ingredient (product_id, ingredient_id) VALUES (?, ?)")
                        .setParameter(1, product.getId())
                        .setParameter(2, ingredient.getId())
                        .executeUpdate();
            }
        }
    }



    @Transactional
    public void decrementProductQuantity(Long productId) {
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new EntityNotFoundException("Prodotto non trovato con ID: " + productId);
        }
        if (product.getQuantity() > 0) {
            product.setQuantity(product.getQuantity() - 1);
        } else {
            throw new BadRequestException("Quantità del prodotto è già zero.");
        }
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new EntityNotFoundException("Prodotto non trovato con ID: " + productId);
        }
        productRepository.delete(product);
    }
}
