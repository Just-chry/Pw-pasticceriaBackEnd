package it.ITSincom.WebDev.service;

import it.ITSincom.WebDev.persistence.ProductRepository;
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

    @Inject
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
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
        productRepository.persist(product);
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
