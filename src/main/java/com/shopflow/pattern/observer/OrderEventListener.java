package com.shopflow.pattern.observer;

import com.shopflow.model.entity.Order;

/**
 * Observer Pattern — Order Event Listener
 *
 * <p>Implements the Observer (GoF) design pattern for order lifecycle events.
 * Each listener handles a specific concern (email, inventory, analytics)
 * without coupling them to the core order logic.
 *
 * <p>This demonstrates the Open/Closed Principle: new listeners can be added
 * without modifying existing code.
 */
public interface OrderEventListener {

    void onOrderCreated(Order order);

    void onOrderStatusChanged(Order order, Order.OrderStatus previousStatus);
}
