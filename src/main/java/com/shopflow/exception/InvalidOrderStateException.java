package com.shopflow.exception;

import com.shopflow.model.entity.Order;

public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(Long orderId, Order.OrderStatus current, Order.OrderStatus attempted) {
        super(String.format(
                "Order #%d cannot transition from %s to %s — order is in a terminal state.",
                orderId, current, attempted));
    }

    public InvalidOrderStateException(String message) {
        super(message);
    }
}

