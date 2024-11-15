package it.ITSincom.WebDev.service;

import it.ITSincom.WebDev.persistence.IngredientRepository;
import it.ITSincom.WebDev.persistence.ProductRepository;
import it.ITSincom.WebDev.persistence.model.Ingredient;
import it.ITSincom.WebDev.persistence.model.Product;
import it.ITSincom.WebDev.rest.model.ProductAdminResponse;
import it.ITSincom.WebDev.rest.model.ProductResponse;
import it.ITSincom.WebDev.service.exception.InvalidProductException;
import it.ITSincom.WebDev.service.exception.ProductNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
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

    public List<ProductResponse> getAllProductsForUser() {
        List<Product> products = productRepository.findVisibleProducts();
        return products.stream()
                .map(product -> {
                    List<String> ingredients = productRepository.findIngredientNamesByProductId(product.getId());
                    return new ProductResponse(
                            product.getId(),
                            product.getName(),
                            product.getDescription(),
                            product.getImage(),
                            product.getPrice(),
                            product.getCategory().name(),
                            ingredients,
                            product.getQuantity()
                    );
                })
                .collect(Collectors.toList());
    }

    public List<ProductAdminResponse> getAllProductsForAdmin() {
        List<Product> products = productRepository.listAll();
        return products.stream()
                .map(product -> {
                    List<String> ingredients = productRepository.findIngredientNamesByProductId(product.getId());
                    return new ProductAdminResponse(
                            product.getId(),
                            product.getName(),
                            product.getDescription(),
                            product.getImage(),
                            product.getPrice(),
                            product.getCategory().name(),
                            ingredients,
                            product.getQuantity(),
                            product.getIsVisible()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void addProduct(Product product) throws IOException {
        String base64Image = product.getImage();
        if (base64Image != null && !base64Image.isEmpty()) {
            String fileName = product.getName().replaceAll("\\s+", "_") + ".png";

            saveImage(base64Image, fileName);

            product.setImage("/images/" + fileName);
        }
        validateProductInput(product);
        productRepository.persist(product);

        if (product.getIngredientNames() != null && !product.getIngredientNames().isEmpty()) {
            addIngredientsToProduct(product);
        }
    }

    private void saveImage(String base64Image, String fileName) throws IOException {
        String[] parts = base64Image.split(",");
        String imageData = parts.length > 1 ? parts[1] : parts[0];

        byte[] imageBytes = Base64.getDecoder().decode(imageData);

        Path path = Paths.get("C:\\Users\\JiaHaoChristianChen\\PW-Pasticceria\\front-end/public/images/" + fileName);

        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            fos.write(imageBytes);
        }
    }


    @Transactional
    public void addProducts(List<Product> products) {
        for (Product product : products) {
            if (product.getIsVisible() == null) {
                product.setIsVisible(true);
            }
            validateProductInput(product);
            productRepository.persist(product);
            addIngredientsToProduct(product);
        }
    }

    @Transactional
    public void deleteProduct(String productId) {
        Product product = getProductByIdOrThrow(productId);

        String imagePath = "C:\\Users\\JiaHaoChristianChen\\PW-Pasticceria\\front-end/public" + product.getImage();
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            if (imageFile.delete()) {
                System.out.println("Immagine eliminata con successo: " + imagePath);
            } else {
                System.err.println("Errore durante l'eliminazione dell'immagine: " + imagePath);
            }
        } else {
            System.out.println("Immagine non trovata: " + imagePath);
        }

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

        System.out.println("Valore ricevuto per isVisible: " + updatedProduct.getIsVisible());

        validateProductInput(updatedProduct);
        updateProductDetails(product, updatedProduct);
        if (updatedProduct.getIngredientNames() != null) {
            updateProductIngredients(product, updatedProduct.getIngredientNames());
        }

    }


    @Transactional
    public void updateProductIngredients(Product product, List<String> newIngredients) {
        List<String> currentIngredients = productRepository.findIngredientNamesByProductId(product.getId());

        List<String> ingredientsToAdd = newIngredients.stream()
                .filter(ingredient -> !currentIngredients.contains(ingredient))
                .collect(Collectors.toList());

        List<String> ingredientsToRemove = currentIngredients.stream()
                .filter(ingredient -> !newIngredients.contains(ingredient))
                .collect(Collectors.toList());

        for (String ingredientName : ingredientsToAdd) {
            Ingredient ingredient = getOrCreateIngredientByName(ingredientName);
            productRepository.addIngredientToProduct(product.getId(), ingredient.getId());
        }

        for (String ingredientName : ingredientsToRemove) {
            Ingredient ingredient = ingredientRepository.find("name", ingredientName).firstResult();
            if (ingredient != null) {
                productRepository.removeIngredientFromProduct(product.getId(), ingredient.getId());
            }
        }
    }


    private Product getProductByIdOrThrow(String productId) {
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new ProductNotFoundException("Prodotto non trovato con ID: " + productId);
        }
        return product;
    }

    private void validateProductInput(Product product) {
        if (product.getName() != null && product.getName().trim().isEmpty()) {
            throw new InvalidProductException("Il nome del prodotto non può essere vuoto.");
        }
        if (product.getPrice() != null && product.getPrice() < 0) {
            throw new InvalidProductException("Il prezzo del prodotto non può essere negativo.");
        }
        if (product.getQuantity() != null && product.getQuantity() < 0) {
            throw new InvalidProductException("La quantità del prodotto non può essere negativa.");
        }
    }

    private void addIngredientsToProduct(Product product) {
        if (product.getIngredientNames() != null && !product.getIngredientNames().isEmpty()) {
            for (String ingredientName : product.getIngredientNames()) {
                Ingredient ingredient = getOrCreateIngredientByName(ingredientName);
                productRepository.addIngredientToProduct(product.getId(), ingredient.getId());
            }
        }
    }

    private Ingredient getOrCreateIngredientByName(String ingredientName) {
        Ingredient ingredient = ingredientRepository.find("name", ingredientName).firstResult();
        if (ingredient == null) {
            ingredient = new Ingredient();
            ingredient.setName(ingredientName);
            ingredientRepository.persist(ingredient);
        }
        return ingredient;
    }


    private void updateProductDetails(Product product, Product updatedProduct) {
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
        System.out.println("Updating visibility to: " + updatedProduct.getIsVisible());
        product.setIsVisible(updatedProduct.getIsVisible());

        if (updatedProduct.getCategory() != null) {
            product.setCategory(updatedProduct.getCategory());
        }
    }


    public List<ProductAdminResponse> getProductsByCategoryForAdmin(String category) {

        List<Product> products = productRepository.findByCategory(category);
        List<ProductAdminResponse> productAdminResponses = new ArrayList<>();

        for (Product product : products) {
            List<String> ingredientNames = productRepository.findIngredientNamesByProductId(product.getId());

            ProductAdminResponse response = new ProductAdminResponse(
                    product.getId(),
                    product.getName(),
                    product.getDescription(),
                    product.getImage(),
                    product.getPrice(),
                    product.getCategory().name(),
                    ingredientNames,
                    product.getQuantity(),
                    product.getIsVisible()
            );
            productAdminResponses.add(response);
        }

        return productAdminResponses;
    }


    public List<ProductResponse> getProductsByCategoryForUser(String category) {
        List<Product> products = productRepository.findVisibleProducts().stream()
                .filter(product -> product.getCategory().name().equalsIgnoreCase(category))
                .collect(Collectors.toList());

        if (products.isEmpty()) {
            throw new ProductNotFoundException("Nessun prodotto trovato per la categoria: " + category);
        }

        List<ProductResponse> productResponses = new ArrayList<>();
        for (Product product : products) {
            List<String> ingredientNames = productRepository.findIngredientNamesByProductId(product.getId());

            ProductResponse response = new ProductResponse(
                    product.getId(),
                    product.getName(),
                    product.getDescription(),
                    product.getImage(),
                    product.getPrice(),
                    product.getCategory().name(),
                    ingredientNames,
                    product.getQuantity());
            productResponses.add(response);
        }

        return productResponses;
    }


    public ProductResponse getProductById(String productId) {
        Product product = getProductByIdOrThrow(productId);

        List<String> ingredientNames = productRepository.findIngredientNamesByProductId(product.getId());

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getImage(),
                product.getPrice(),
                product.getCategory().name(),
                ingredientNames,
                product.getQuantity());
    }


}
