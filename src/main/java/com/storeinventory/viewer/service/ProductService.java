package com.storeinventory.viewer.service;

import com.storeinventory.viewer.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    // Public methods (for customers)
    List<Product> getAllProducts();
    List<Product> getAvailableProducts();
    Optional<Product> getProductById(Long id);
    List<Product> getProductsByCategory(String category);
    List<Product> getAvailableProductsByCategory(String category);
    List<Product> searchProducts(String name);
    List<Product> searchAvailableProducts(String name);
    List<String> getAllCategories();

    // Admin methods
    Product createProduct(Product product);
    Product updateProduct(Long id, Product product);
    void deleteProduct(Long id);
    List<Product> getLowStockProducts(Integer threshold);

    // Advanced methods with pagination
    Page<Product> getProductsWithPagination(Pageable pageable);
    Page<Product> getAvailableProductsWithPagination(Pageable pageable);
    Page<Product> getProductsByCategoryWithPagination(String category, Pageable pageable);

    // Validation methods
    boolean productExistsByName(String name);
    boolean productExistsById(Long id);
}