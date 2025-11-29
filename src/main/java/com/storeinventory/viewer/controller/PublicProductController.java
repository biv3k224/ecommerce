package com.storeinventory.viewer.controller;

import com.storeinventory.viewer.entity.Product;
import com.storeinventory.viewer.service.ProductService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/products")
@CrossOrigin(origins = "*") // Enable CORS for frontend
public class PublicProductController {

    private static final Logger log = LoggerFactory.getLogger(PublicProductController.class);

    private final ProductService productService;

    // ✅ Manual constructor (replaces @RequiredArgsConstructor)
    public PublicProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        log.info("Public API: Fetching all products");
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/available")
    public ResponseEntity<List<Product>> getAvailableProducts() {
        log.info("Public API: Fetching available products");
        List<Product> products = productService.getAvailableProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        log.info("Public API: Fetching product by id: {}", id);
        Optional<Product> product = productService.getProductById(id);

        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        log.info("Public API: Fetching products by category: {}", category);
        List<Product> products = productService.getAvailableProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        log.info("Public API: Fetching all categories");
        List<String> categories = productService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String name) {
        log.info("Public API: Searching products with name: {}", name);
        List<Product> products = productService.searchAvailableProducts(name);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search/category")
    public ResponseEntity<List<Product>> searchProductsByCategoryAndName(
            @RequestParam String category,
            @RequestParam String name) {
        log.info("Public API: Searching products by category '{}' and name '{}'", category, name);

        List<Product> products = productService.getAvailableProductsByCategory(category);

        List<Product> filteredProducts = products.stream()
                .filter(product -> product.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();

        return ResponseEntity.ok(filteredProducts);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Product>> getProductsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Public API: Fetching products with pagination - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.getAvailableProductsWithPagination(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{category}/page")
    public ResponseEntity<Page<Product>> getProductsByCategoryWithPagination(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Public API: Fetching products by category '{}' with pagination - page: {}, size: {}", category, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.getProductsByCategoryWithPagination(category, pageable);
        return ResponseEntity.ok(products);
    }
}
