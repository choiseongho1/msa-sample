package com.commerce.productservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.commerce.productservice.dto.ProductListDto;
import com.commerce.productservice.dto.ProductSearchDto;

public interface ProductRepositoryDsl {
    Page<ProductListDto> findProductsList(ProductSearchDto productSearchDto, Pageable pageable);
}