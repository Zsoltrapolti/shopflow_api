package com.shopflow.pattern.observer;

import com.shopflow.model.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class EmailNotificationListener implements OrderEventListener {

    @Override
    public void onOrderCreated(Order order) {
        log.info("[EMAIL] Sending order confirmation to {} for order #{}",
                order.getCustomerEmail(), order.getId());
        // Production: emailService.sendOrderConfirmation(order);
    }

    @Override
    public void onOrderStatusChanged(Order order, Order.OrderStatus previousStatus) {
        log.info("[EMAIL] Notifying {} about order #{} status change: {} → {}",
                order.getCustomerEmail(), order.getId(), previousStatus, order.getStatus());
        // Production: emailService.sendStatusUpdate(order, previousStatus);
    }
}
