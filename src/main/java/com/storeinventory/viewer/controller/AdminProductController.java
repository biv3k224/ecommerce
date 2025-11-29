package com.storeinventory.viewer.controller;

import com.storeinventory.viewer.entity.Product;
import com.storeinventory.viewer.service.ProductService;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminProductController {

    private final ProductService productService;
    private static final Logger log = LoggerFactory.getLogger(AdminProductController.class);

    // ✅ Constructor — replaces Lombok @RequiredArgsConstructor
    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        log.info("Admin API: Fetching all products");
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        log.info("Admin API: Fetching product by id: {}", id);
        Optional<Product> product = productService.getProductById(id);

        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody Product product) {
        log.info("Admin API: Creating new product: {}", product.getName());

        try {
            Product createdProduct = productService.createProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (IllegalArgumentException e) {
            log.error("Error creating product: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error creating product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating product");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody Product productDetails) {
        log.info("Admin API: Updating product with id: {}", id);

        try {
            Product updatedProduct = productService.updateProduct(id, productDetails);
            return ResponseEntity.ok(updatedProduct);
        } catch (IllegalArgumentException e) {
            log.error("Error updating product: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error updating product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating product");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        log.info("Admin API: Deleting product with id: {}", id);

        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok().body("Product deleted successfully");
        } catch (IllegalArgumentException e) {
            log.error("Error deleting product: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error deleting product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting product");
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        log.info("Admin API: Fetching products by category: {}", category);
        List<Product> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts(@RequestParam(defaultValue = "10") Integer threshold) {
        log.info("Admin API: Fetching low stock products with threshold: {}", threshold);
        List<Product> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String name) {
        log.info("Admin API: Searching products with name: {}", name);
        List<Product> products = productService.searchProducts(name);
        return ResponseEntity.ok(products);
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<?> updateProductAvailability(@PathVariable Long id, @RequestParam Boolean available) {
        log.info("Admin API: Updating product availability for id: {} to {}", id, available);

        try {
            Optional<Product> productOptional = productService.getProductById(id);
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                product.setAvailable(available);
                Product updatedProduct = productService.updateProduct(id, product);
                return ResponseEntity.ok(updatedProduct);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating product availability: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating product availability");
        }
    }
}
