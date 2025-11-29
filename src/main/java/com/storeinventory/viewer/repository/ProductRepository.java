package com.storeinventory.viewer.repository;

import com.storeinventory.viewer.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find all available products
    List<Product> findByAvailableTrue();

    // Find products by category
    List<Product> findByCategory(String category);

    // Find available products by category
    List<Product> findByCategoryAndAvailableTrue(String category);

    // Find products by name containing (case-insensitive)
    List<Product> findByNameContainingIgnoreCase(String name);

    // Find available products by name containing (case-insensitive)
    List<Product> findByNameContainingIgnoreCaseAndAvailableTrue(String name);

    // Find products by category and name containing (case-insensitive)
    List<Product> findByCategoryAndNameContainingIgnoreCase(String category, String name);

    // Find available products by category and name containing (case-insensitive)
    List<Product> findByCategoryAndNameContainingIgnoreCaseAndAvailableTrue(String category, String name);

    // Find products with low stock (less than specified quantity)
    List<Product> findByQuantityLessThan(Integer quantity);

    // Find available products with low stock
    List<Product> findByQuantityLessThanAndAvailableTrue(Integer quantity);

    // Get all distinct categories
    @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
    List<String> findAllDistinctCategories();

    // Check if product exists by name (for validation)
    boolean existsByName(String name);

    // Find product by name (exact match, case-sensitive)
    Optional<Product> findByName(String name);
}