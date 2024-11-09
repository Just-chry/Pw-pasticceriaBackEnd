package it.ITSincom.WebDev.service;

import it.ITSincom.WebDev.persistence.IngredientRepository;
import it.ITSincom.WebDev.persistence.ProductRepository;
import it.ITSincom.WebDev.persistence.model.Ingredient;
import it.ITSincom.WebDev.persistence.model.Product;
import it.ITSincom.WebDev.rest.model.ProductResponse;
import it.ITSincom.WebDev.service.exception.EntityNotFoundException;
import it.ITSincom.WebDev.service.exception.ProductNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;

import java.util.List;
import java.util.stream.Collectors;

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

        if (product.getIngredientNames() != null && !product.getIngredientNames().isEmpty()) {
            for (String ingredientName : product.getIngredientNames()) {
                Ingredient ingredient = ingredientRepository.find("name", ingredientName).firstResult();

                if (ingredient == null) {
                    ingredient = new Ingredient();
                    ingredient.setName(ingredientName);
                    ingredientRepository.persist(ingredient);
                }
                productRepository.addIngredientToProduct(product.getId(), ingredient.getId());
            }
        }
    }

    @Transactional
    public void deleteProduct(String productId) {
        Product product = getProductByIdOrThrow(productId);
        productRepository.delete(product);
    }

    @Transactional
    public void decrementProductQuantity(String productId) {
        Product product = getProductByIdOrThrow(productId);
        if (product.getQuantity() > 0) {
            product.setQuantity(product.getQuantity() - 1);
        } else {
            throw new BadRequestException("Quantità del prodotto è già zero.");
        }
    }

    @Transactional
    public void incrementProductQuantity(String productId) {
        Product product = getProductByIdOrThrow(productId);
        product.setQuantity(product.getQuantity() + 1);
    }


    @Transactional
    public void modifyProduct(String productId, Product updatedProduct) {
        Product product = getProductByIdOrThrow(productId);
        if (updatedProduct.getName() != null && updatedProduct.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome del prodotto non può essere vuoto.");
        }
        if (updatedProduct.getPrice() != null && updatedProduct.getPrice() < 0) {
            throw new IllegalArgumentException("Il prezzo del prodotto non può essere negativo.");
        }
        if (updatedProduct.getQuantity() != null && updatedProduct.getQuantity() < 0) {
            throw new IllegalArgumentException("La quantità del prodotto non può essere negativa.");
        }
        if (updatedProduct.getName() != null) {
            product.setName(updatedProduct.getName());
        }
        if (updatedProduct.getDescription() != null) {
            product.setDescription(updatedProduct.getDescription());
        }
        if (updatedProduct.getImage() != null) {
            product.setImage(updatedProduct.getImage());
        }
        if (updatedProduct.getPrice() != null) {
            product.setPrice(updatedProduct.getPrice());
        }
        if (updatedProduct.getQuantity() != null) {
            product.setQuantity(updatedProduct.getQuantity());
        }
        if (updatedProduct.getIsVisible() != null) {
            product.setIsVisible(updatedProduct.getIsVisible());
        }
        if (updatedProduct.getCategory() != null) {
            product.setCategory(updatedProduct.getCategory());
        }

        productRepository.persist(product);
    }

    public List<ProductResponse> getVisibleProducts() {
        List<Product> products = productRepository.findVisibleProducts();
        // Converti i prodotti in `ProductResponse` e aggiungi gli ingredienti
        return products.stream()
                .map(product -> {
                    List<String> ingredients = productRepository.findIngredientNamesByProductId(product.getId());
                    return new ProductResponse(
                            product.getName(),
                            product.getDescription(),
                            product.getImage(),
                            product.getPrice(),
                            product.getCategory().name(),
                            ingredients
                    );
                })
                .collect(Collectors.toList());
    }

    private Product getProductByIdOrThrow(String productId) {
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new ProductNotFoundException("Prodotto non trovato con ID: " + productId);
        }
        return product;
    }

}
