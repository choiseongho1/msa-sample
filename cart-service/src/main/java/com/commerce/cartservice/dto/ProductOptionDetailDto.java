package com.commerce.cartservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @Builder
public class ProductOptionDetailDto {
    private String productTitle;

    private String optionName;

    private Integer price;

    private String imageLob;


}