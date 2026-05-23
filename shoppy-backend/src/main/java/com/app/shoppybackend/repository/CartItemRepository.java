package com.app.shoppybackend.repository;

import com.app.shoppybackend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserEmail(String userEmail);
    Optional<CartItem> findByProductIdAndSelectedSizeAndUserEmail(Long productId, String selectedSize, String userEmail);
    void deleteByUserEmail(String userEmail);
}
