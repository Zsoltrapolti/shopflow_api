package com.shopflow.service;

import com.shopflow.model.entity.Order;
import com.shopflow.pattern.factory.DiscountStrategyFactory;
import com.shopflow.pattern.strategy.DiscountStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Discount Strategy Pattern")
class DiscountStrategyTest {

    /**
     * Verify that the Factory selects the correct Strategy based on order total.
     */
    @Test
    @DisplayName("should apply loyalty discount for orders over $200")
    void shouldApplyLoyaltyDiscountForLargeOrders() {
        Order order = new Order();
        order.setTotalAmount(new BigDecimal("250.00"));

        // Manually wire the strategies as the factory would
        var loyaltyBean = createStrategy("loyaltyDiscount", new BigDecimal("0.15"));
        var noDiscount   = createStrategy("noDiscount", BigDecimal.ZERO);

        DiscountStrategyFactory factory = new DiscountStrategyFactory(
                Map.of("loyaltyDiscount", loyaltyBean, "noDiscount", noDiscount));

        DiscountStrategy selected = factory.getStrategy(order);
        BigDecimal discount = selected.calculateDiscount(order);

        assertThat(selected.getName()).isEqualTo("LOYALTY_15");
        assertThat(discount).isEqualByComparingTo("37.50"); // 15% of 250
    }

    @Test
    @DisplayName("should apply no discount for orders under $200")
    void shouldApplyNoDiscountForSmallOrders() {
        Order order = new Order();
        order.setTotalAmount(new BigDecimal("99.00"));

        var noDiscount = createStrategy("noDiscount", BigDecimal.ZERO);

        DiscountStrategyFactory factory = new DiscountStrategyFactory(
                Map.of("loyaltyDiscount", createStrategy("loyaltyDiscount", new BigDecimal("0.15")),
                        "noDiscount", noDiscount));

        DiscountStrategy selected = factory.getStrategy(order);
        BigDecimal discount = selected.calculateDiscount(order);

        assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ── Minimal stub strategy for unit testing the factory ─────────────────
    private DiscountStrategy createStrategy(String beanName, BigDecimal rate) {
        return new DiscountStrategy() {
            @Override
            public BigDecimal calculateDiscount(Order order) {
                if (rate.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
                // Only apply loyalty if threshold met
                if (order.getTotalAmount().compareTo(new BigDecimal("200")) >= 0) {
                    return order.getTotalAmount().multiply(rate)
                            .setScale(2, java.math.RoundingMode.HALF_UP);
                }
                return BigDecimal.ZERO;
            }
            @Override
            public String getName() {
                return beanName.equals("loyaltyDiscount") ? "LOYALTY_15" : "NO_DISCOUNT";
            }
        };
    }
}
