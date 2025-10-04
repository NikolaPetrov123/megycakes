package com.megycakes.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrueOrderByNameAsc();
    List<Product> findByCategory_SlugAndActiveTrueOrderByNameAsc(String categorySlug);
    Optional<Product> findBySlugAndActiveTrue(String slug);
}