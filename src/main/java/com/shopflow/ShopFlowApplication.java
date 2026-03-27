package com.shopflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ShopFlow - Production-grade E-Commerce REST API
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>SOLID principles throughout all layers</li>
 *   <li>Design Patterns: Observer, Strategy, Factory</li>
 *   <li>Clean layered architecture: Controller → Service → Repository</li>
 *   <li>Global exception handling with RFC 7807 Problem Details</li>
 *   <li>DTO mapping with MapStruct</li>
 *   <li>Bean Validation on all inputs</li>
 * </ul>
 */
@SpringBootApplication
public class ShopFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopFlowApplication.class, args);
    }
}
