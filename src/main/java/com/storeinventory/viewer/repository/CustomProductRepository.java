package com.storeinventory.viewer.repository;

import com.storeinventory.viewer.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomProductRepository {

    // Search products with multiple criteria
    List<Product> searchProducts(String name, String category, Double minPrice, Double maxPrice, Boolean available);

    // Get products with pagination and filtering
    Page<Product> findProductsWithFilters(String category, Boolean available, Pageable pageable);

    // Get product statistics by category
    List<Object[]> getProductCountByCategory();
}