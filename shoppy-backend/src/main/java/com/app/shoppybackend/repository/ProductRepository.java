package com.app.shoppybackend.repository;

import com.app.shoppybackend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    List<Product> findBySubCategory(String subCategory);
    
    // For search feature
    List<Product> findByNameContainingIgnoreCase(String name);
    
    // For pagination/filtering
    @Query("SELECT p FROM Product p WHERE (:category = 'All' OR p.subCategory = :category OR p.category = :category) AND p.price >= :minPrice AND p.price <= :maxPrice")
    List<Product> findFiltered(String category, Double minPrice, Double maxPrice);
}
