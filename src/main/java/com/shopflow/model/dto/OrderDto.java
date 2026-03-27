package com.shopflow.model.dto;

import com.shopflow.model.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        @NotBlank(message = "Customer email is required")
        @Email(message = "Must be a valid email address")
        @Schema(example = "customer@example.com")
        private String customerEmail;

        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        private List<OrderItemDto.Request> items;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String customerEmail;
        private List<OrderItemDto.Response> items;
        private BigDecimal totalAmount;
        private Order.OrderStatus status;
        private Instant createdAt;
        private Instant updatedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusUpdate {

        @NotNull(message = "Status is required")
        private Order.OrderStatus status;
    }
}
