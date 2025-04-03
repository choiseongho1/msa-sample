package com.commerce.productservice.dto;


import com.commerce.productservice.entity.ProductOption;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class ProductOptionDetailDto {
    private String productTitle;

    private String optionName;

    private Integer price;

    private String imageLob;


}