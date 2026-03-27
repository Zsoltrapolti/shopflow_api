package com.shopflow.config;

import com.shopflow.model.entity.Product;
import com.shopflow.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.List;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataLoader {

    private final ProductRepository productRepository;

    @Bean
    @Profile("!test")
    public CommandLineRunner seedData() {
        return args -> {
            if (productRepository.count() > 0) {
                log.info("Database already seeded — skipping.");
                return;
            }

            List<Product> products = List.of(
                    product("Sony WH-1000XM5",
                            "Industry-leading noise cancelling headphones with 30-hour battery",
                            "349.99", 42, Product.Category.ELECTRONICS),
                    product("Apple AirPods Pro (2nd Gen)",
                            "Active Noise Cancellation, Transparency mode, Spatial Audio",
                            "249.00", 87, Product.Category.ELECTRONICS),
                    product("Kindle Paperwhite 11th Gen",
                            "6.8\" display, adjustable warm light, IPX8 waterproof, 8-week battery",
                            "139.99", 120, Product.Category.ELECTRONICS),
                    product("Clean Code",
                            "A Handbook of Agile Software Craftsmanship — Robert C. Martin",
                            "34.99", 200, Product.Category.BOOKS),
                    product("Effective Java, 3rd Edition",
                            "Best practices for the Java platform — Joshua Bloch",
                            "44.99", 150, Product.Category.BOOKS),
                    product("Designing Data-Intensive Applications",
                            "The big ideas behind reliable, scalable, and maintainable systems",
                            "49.99", 95, Product.Category.BOOKS),
                    product("Nike Air Max 270",
                            "Lightweight running shoes with MAX Air cushioning unit",
                            "119.99", 60, Product.Category.SPORTS),
                    product("Manduka PRO Yoga Mat",
                            "6mm, lifetime guarantee, closed-cell surface, eco-certified",
                            "79.99", 300, Product.Category.SPORTS),
                    product("Levi's 501 Original Jeans",
                            "Classic straight fit, 100% cotton denim",
                            "69.99", 180, Product.Category.CLOTHING),
                    product("Uniqlo Ultra Light Down Jacket",
                            "Packable, warm, windproof — folds into its own pocket",
                            "89.99", 75, Product.Category.CLOTHING)
            );

            productRepository.saveAll(products);
            log.info("Seeded {} products into the database.", products.size());
        };
    }

    private Product product(String name, String description,
                            String price, int stock, Product.Category category) {
        return Product.builder()
                .name(name)
                .description(description)
                .price(new BigDecimal(price))
                .stockQuantity(stock)
                .category(category)
                .build();
    }
}
