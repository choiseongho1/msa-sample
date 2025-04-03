package com.commerce.productservice.dto;

import java.util.List;

import com.commerce.productservice.entity.Product.ProductCategory;
import com.commerce.productservice.entity.Product.ProductStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter @Setter
@AllArgsConstructor @NoArgsConstructor @Builder
public class ProductSearchDto {
    private String title;
    private List<ProductCategory> category;
    private List<ProductStatus> status;
    private Integer minPrice;
    private Integer maxPrice;
    private String loginId;
}
