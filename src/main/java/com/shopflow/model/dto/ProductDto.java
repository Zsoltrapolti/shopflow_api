package com.shopflow.model.dto;

import com.shopflow.model.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

public class ProductDto {

    @Schema(description = "Request DTO for creating or updating a product")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        @NotBlank(message = "Product name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        @Schema(example = "Wireless Headphones Pro")
        private String name;

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        @Schema(example = "Premium noise-cancelling wireless headphones")
        private String description;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Invalid price format")
        @Schema(example = "149.99")
        private BigDecimal price;

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock quantity cannot be negative")
        @Schema(example = "50")
        private Integer stockQuantity;

        @NotNull(message = "Category is required")
        @Schema(example = "ELECTRONICS")
        private Product.Category category;
    }

    @Schema(description = "Response DTO for product data")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stockQuantity;
        private Product.Category category;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
