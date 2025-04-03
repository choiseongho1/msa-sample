package com.commerce.batchservice.repository;

import com.commerce.batchservice.entity.Wishlist;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    List<Wishlist> findByWishlistCreatedAtBefore(LocalDateTime cutoffDate);

    List<Wishlist> findByUserIdAndProductId(String userId, Long productId);
}
