package com.commerce.productservice.repository;

import com.commerce.productservice.entity.Wishlist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long>, WishlistRepositoryDsl {
    Optional<Wishlist> findByUserIdAndProductId(String userId, Long productId);

}
