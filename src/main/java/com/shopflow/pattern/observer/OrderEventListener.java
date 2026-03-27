package com.shopflow.pattern.observer;

import com.shopflow.model.entity.Order;


public interface OrderEventListener {

    void onOrderCreated(Order order);

    void onOrderStatusChanged(Order order, Order.OrderStatus previousStatus);
}
