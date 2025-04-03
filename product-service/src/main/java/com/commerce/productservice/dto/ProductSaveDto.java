package com.commerce.productservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.commerce.productservice.entity.Product;
import com.commerce.productservice.entity.Product.ProductCategory;
import com.commerce.productservice.entity.Product.ProductStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
public class ProductSaveDto {
    @NotBlank(message = "상품명은 필수입니다")
    @Schema(description = "상품명", defaultValue = "프리미엄 가죽 소파")
    private String title;

    @NotBlank(message = "상품 설명은 필수입니다")
    @Schema(description = "상품 설명", defaultValue = "최고급 이태리 가죽을 사용한 3인용 소파입니다.")
    private String content;

    @NotNull(message = "이미지는 필수입니다")
    @Schema(description = "이미지 Base64 인코딩 값", defaultValue = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAACAIAAADZ6Z8bAAAAFElEQVR42mJ8//8/wcAAwAB/0W8QxAAAAAElFTkSuQmCC")
    private String imageLob;

    @NotNull(message = "상품 상태는 필수입니다")
    @Schema(description = "상품 상태", defaultValue = "AVAILABLE")
    private ProductStatus status;

    @Min(value = 0, message = "가격은 0 이상이어야 합니다")
    @Schema(description = "상품 가격", defaultValue = "1200000")
    private Integer price;

    @NotNull(message = "카테고리는 필수입니다")
    @Schema(description = "카테고리", defaultValue = "LIVING")
    private ProductCategory category;

    @Schema(description = "상품 등록 시작 날짜", defaultValue = "2025-01-03T14:39:39")
    private LocalDateTime startDate;
    
    private List<ProductOptionSaveDto> options = new ArrayList<>();

    public Product toEntity() {
        return Product.builder()
                .title(title)
                .content(content)
                .status(status)
                .price(price)
                .category(category)
                .startDate(startDate)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}

