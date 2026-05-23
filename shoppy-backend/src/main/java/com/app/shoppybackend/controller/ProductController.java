package com.app.shoppybackend.controller;

import com.app.shoppybackend.entity.Product;
import com.app.shoppybackend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productRepository.findById(id).orElse(null);
    }
    
    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String q) {
        return productRepository.findByNameContainingIgnoreCase(q);
    }
    
    @GetMapping("/filter")
    public List<Product> filterProducts(
            @RequestParam(defaultValue = "All") String category,
            @RequestParam(defaultValue = "0.0") Double minPrice,
            @RequestParam(defaultValue = "100000.0") Double maxPrice) {
        return productRepository.findFiltered(category, minPrice, maxPrice);
    }
    
    // Feature: Recommendation Engine
    @GetMapping("/{id}/recommendations")
    public List<Product> getRecommendations(@PathVariable Long id) {
        Product current = productRepository.findById(id).orElse(null);
        if (current == null) return List.of();
        
        // Find other products in the same subcategory, excluding this one
        List<Product> sameCategory = productRepository.findBySubCategory(current.getSubCategory());
        return sameCategory.stream()
                .filter(p -> !p.getId().equals(id))
                .limit(5)
                .toList();
    }
}
