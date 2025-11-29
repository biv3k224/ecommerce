package com.storeinventory.viewer.service.impl;

import com.storeinventory.viewer.entity.Product;
import com.storeinventory.viewer.repository.ProductRepository;
import com.storeinventory.viewer.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAvailableProducts() {
        log.info("Fetching all available products");
        return productRepository.findByAvailableTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        log.info("Fetching product with id: {}", id);
        return productRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        log.info("Fetching products by category: {}", category);
        return productRepository.findByCategory(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAvailableProductsByCategory(String category) {
        log.info("Fetching available products by category: {}", category);
        return productRepository.findByCategoryAndAvailableTrue(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String name) {
        log.info("Searching products with name containing: {}", name);
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchAvailableProducts(String name) {
        log.info("Searching available products with name containing: {}", name);
        return productRepository.findByNameContainingIgnoreCaseAndAvailableTrue(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        log.info("Fetching all distinct categories");
        return productRepository.findAllDistinctCategories();
    }

    @Override
    @Transactional
    public Product createProduct(Product product) {
        log.info("Creating new product: {}", product.getName());

        if (productRepository.existsByName(product.getName())) {
            throw new IllegalArgumentException("Product with name '" + product.getName() + "' already exists");
        }

        if (product.getAvailable() == null) {
            product.setAvailable(true);
        }

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        log.info("Updating product with id: {}", id);

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));

        if (!existingProduct.getName().equals(productDetails.getName()) &&
                productRepository.existsByName(productDetails.getName())) {
            throw new IllegalArgumentException("Product with name '" + productDetails.getName() + "' already exists");
        }

        existingProduct.setName(productDetails.getName());
        existingProduct.setDescription(productDetails.getDescription());
        existingProduct.setCategory(productDetails.getCategory());
        existingProduct.setPrice(productDetails.getPrice());
        existingProduct.setQuantity(productDetails.getQuantity());
        existingProduct.setAvailable(productDetails.getAvailable());
        existingProduct.setImageUrl(productDetails.getImageUrl());

        return productRepository.save(existingProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);

        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found with id: " + id);
        }

        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts(Integer threshold) {
        log.info("Fetching products with stock less than: {}", threshold);
        return productRepository.findByQuantityLessThanAndAvailableTrue(threshold);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getProductsWithPagination(Pageable pageable) {
        log.info("Fetching products with pagination: {}", pageable);
        return productRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getAvailableProductsWithPagination(Pageable pageable) {
        log.info("Fetching available products with pagination: {}", pageable);
        return (Page<Product>) productRepository.findAll(pageable)
                .map(product -> product.getAvailable() ? product : null)
                .filter(Objects::nonNull);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategoryWithPagination(String category, Pageable pageable) {
        log.info("Fetching products by category '{}' with pagination: {}", category, pageable);
        return (Page<Product>) productRepository.findAll(pageable)
                .map(product -> product.getCategory().equals(category) ? product : null)
                .filter(Objects::nonNull);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean productExistsByName(String name) {
        return productRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean productExistsById(Long id) {
        return productRepository.existsById(id);
    }
}