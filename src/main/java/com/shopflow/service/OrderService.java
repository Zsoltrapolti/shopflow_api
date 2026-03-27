package com.shopflow.service;

import com.shopflow.model.dto.OrderDto;
import com.shopflow.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderDto.Response createOrder(OrderDto.Request request);

    OrderDto.Response getOrderById(Long id);

    Page<OrderDto.Response> getOrdersByCustomer(String customerEmail, Pageable pageable);

    OrderDto.Response updateOrderStatus(Long id, Order.OrderStatus newStatus);

    void cancelOrder(Long id);
}
