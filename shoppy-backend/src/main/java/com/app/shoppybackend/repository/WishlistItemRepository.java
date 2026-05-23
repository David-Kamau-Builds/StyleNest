package com.app.shoppybackend.repository;

import com.app.shoppybackend.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByUserEmail(String userEmail);
    Optional<WishlistItem> findByProductIdAndUserEmail(Long productId, String userEmail);
}
