package com.shopflow.controller;

import com.shopflow.model.dto.OrderDto;
import com.shopflow.model.entity.Order;
import com.shopflow.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Order REST controller.
 *
 * <p>Exposes order lifecycle endpoints. Status transitions are validated
 * in the service layer via explicit state-machine logic.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order placement and lifecycle management")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Place a new order")
    public OrderDto.Response create(@Valid @RequestBody OrderDto.Request request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public OrderDto.Response getById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping
    @Operation(summary = "Get orders by customer email")
    public Page<OrderDto.Response> getByCustomer(
            @RequestParam String customerEmail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return orderService.getOrdersByCustomer(customerEmail, PageRequest.of(page, size));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status (state machine enforced)")
    public OrderDto.Response updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderDto.StatusUpdate request) {
        return orderService.updateOrderStatus(id, request.getStatus());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel an order (PENDING or CONFIRMED only)")
    public void cancel(@PathVariable Long id) {
        orderService.cancelOrder(id);
    }
}
