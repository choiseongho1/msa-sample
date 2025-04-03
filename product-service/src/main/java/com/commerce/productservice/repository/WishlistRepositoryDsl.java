package com.commerce.productservice.repository;

import com.commerce.productservice.dto.ProductListDto;
import com.commerce.productservice.dto.ProductSearchDto;
import com.commerce.productservice.dto.WishlistListDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WishlistRepositoryDsl {

    List<WishlistListDto> findWishlistList(String userId);

}