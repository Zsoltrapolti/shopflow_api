package com.shopflow.pattern.strategy;

import com.shopflow.model.entity.Order;

import java.math.BigDecimal;


public interface DiscountStrategy {


    BigDecimal calculateDiscount(Order order);


    String getName();
}
