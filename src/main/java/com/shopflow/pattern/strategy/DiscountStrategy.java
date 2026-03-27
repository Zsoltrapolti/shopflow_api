package com.shopflow.pattern.strategy;

import com.shopflow.model.entity.Order;

import java.math.BigDecimal;

/**
 * Strategy Pattern — Discount Calculation
 *
 * <p>Defines the contract for interchangeable discount algorithms.
 * This demonstrates the Strategy (GoF) pattern and the
 * Dependency Inversion Principle — callers depend on this abstraction,
 * not on concrete implementations.
 */
public interface DiscountStrategy {

    /**
     * Calculates the discount amount for a given order.
     *
     * @param order the order to evaluate
     * @return the discount amount (never negative)
     */
    BigDecimal calculateDiscount(Order order);

    /**
     * Human-readable name for logging and API responses.
     */
    String getName();
}
