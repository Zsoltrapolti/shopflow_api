package com.shopflow.pattern.observer;

import com.shopflow.model.entity.Order;
import com.shopflow.model.entity.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class InventoryTrackingListener implements OrderEventListener {

    private static final int LOW_STOCK_THRESHOLD = 10;

    @Override
    public void onOrderCreated(Order order) {
        order.getItems().forEach(this::checkStockLevel);
    }

    @Override
    public void onOrderStatusChanged(Order order, Order.OrderStatus previousStatus) {
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            log.info("[INVENTORY] Order #{} cancelled — restoring stock for {} items",
                    order.getId(), order.getItems().size());
        }
    }

    private void checkStockLevel(OrderItem item) {
        int remaining = item.getProduct().getStockQuantity() - item.getQuantity();
        if (remaining < LOW_STOCK_THRESHOLD) {
            log.warn("[INVENTORY] Low stock alert: '{}' has {} units remaining",
                    item.getProduct().getName(), remaining);
        }
    }
}
