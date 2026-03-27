package com.shopflow.pattern.factory;

import com.shopflow.model.entity.Order;
import com.shopflow.pattern.strategy.DiscountStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class DiscountStrategyFactory {

    private static final BigDecimal LARGE_ORDER_THRESHOLD = new BigDecimal("200.00");

    private final Map<String, DiscountStrategy> strategies;

    /**
     * Returns the best applicable discount strategy for an order.
     *
     * @param order the order to evaluate
     * @return a suitable {@link DiscountStrategy}
     */
    public DiscountStrategy getStrategy(Order order) {
        if (order.getTotalAmount().compareTo(LARGE_ORDER_THRESHOLD) >= 0) {
            return strategies.get("loyaltyDiscount");
        }
        return strategies.get("noDiscount");
    }

    public DiscountStrategy getStrategy(String strategyName) {
        return strategies.getOrDefault(strategyName, strategies.get("noDiscount"));
    }
}
