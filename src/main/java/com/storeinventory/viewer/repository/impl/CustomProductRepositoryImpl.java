package com.storeinventory.viewer.repository.impl;

import com.storeinventory.viewer.entity.Product;
import com.storeinventory.viewer.repository.CustomProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CustomProductRepositoryImpl implements CustomProductRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Product> searchProducts(String name, String category, Double minPrice, Double maxPrice, Boolean available) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> root = query.from(Product.class);

        List<Predicate> predicates = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }

        if (category != null && !category.isEmpty()) {
            predicates.add(cb.equal(root.get("category"), category));
        }

        if (minPrice != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }

        if (maxPrice != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        if (available != null) {
            predicates.add(cb.equal(root.get("available"), available));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.asc(root.get("name")));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public Page<Product> findProductsWithFilters(String category, Boolean available, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> root = query.from(Product.class);

        List<Predicate> predicates = new ArrayList<>();

        if (category != null && !category.isEmpty()) {
            predicates.add(cb.equal(root.get("category"), category));
        }

        if (available != null) {
            predicates.add(cb.equal(root.get("available"), available));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.asc(root.get("name")));

        List<Product> result = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // Count query for pagination
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Product> countRoot = countQuery.from(Product.class);
        countQuery.select(cb.count(countRoot)).where(predicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(result, pageable, total);
    }

    @Override
    public List<Object[]> getProductCountByCategory() {
        String jpql = "SELECT p.category, COUNT(p), SUM(p.quantity) FROM Product p GROUP BY p.category ORDER BY p.category";
        return entityManager.createQuery(jpql, Object[].class).getResultList();
    }
}