package com.shopflow.pattern.strategy;

import com.shopflow.model.entity.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Concrete Strategies — Discount Implementations
 */

// ── No discount ──────────────────────────────────────────────────────────────
@Component("noDiscount")
class NoDiscountStrategy implements DiscountStrategy {

    @Override
    public BigDecimal calculateDiscount(Order order) {
        return BigDecimal.ZERO;
    }

    @Override
    public String getName() {
        return "NO_DISCOUNT";
    }
}

// ── Percentage off ────────────────────────────────────────────────────────────
@Component("percentageDiscount")
class PercentageDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal PERCENTAGE = new BigDecimal("0.10"); // 10%

    @Override
    public BigDecimal calculateDiscount(Order order) {
        return order.getTotalAmount()
                .multiply(PERCENTAGE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getName() {
        return "PERCENTAGE_10";
    }
}

// ── Loyalty discount (orders over $200 get 15% off) ──────────────────────────
@Component("loyaltyDiscount")
class LoyaltyDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal THRESHOLD = new BigDecimal("200.00");
    private static final BigDecimal RATE = new BigDecimal("0.15");

    @Override
    public BigDecimal calculateDiscount(Order order) {
        if (order.getTotalAmount().compareTo(THRESHOLD) >= 0) {
            return order.getTotalAmount()
                    .multiply(RATE)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public String getName() {
        return "LOYALTY_15";
    }
}
