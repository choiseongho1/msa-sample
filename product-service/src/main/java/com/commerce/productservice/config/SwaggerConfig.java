package com.commerce.productservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {


    @Bean
    public GroupedOpenApi productApi() {
        return GroupedOpenApi.builder()
            .group("상품 API")
            .pathsToMatch("/product/**")
            .packagesToScan("com.commerce.productservice.controller")
            .build();
    }

    @Bean
    public GroupedOpenApi wishlistApi() {
        return GroupedOpenApi.builder()
            .group("찜 API")
            .pathsToMatch("/wishlist/**")
            .packagesToScan("com.commerce.productservice.controller")
            .build();
    }


    @Bean
    public OpenAPI openAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .in(SecurityScheme.In.HEADER)
            .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
            .info(new Info()
                .title("PRODUCT Service API")
                .description("PRODUCT Service API Documentation")
                .version("1.0.0"))

            .servers(List.of(
                new Server().url("http://localhost:8000/product-service").description("API Gateway"),
                new Server().url("http://localhost:8082").description("Product Service")
            ))
            .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
            .addSecurityItem(securityRequirement);
    }
}