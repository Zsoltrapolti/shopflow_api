package com.shopflow.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ShopFlow API")
                        .description("""
                                Production-grade E-Commerce REST API built with Spring Boot 3.
                                
                                **Design Patterns implemented:**
                                - Observer — order event notifications (email, inventory)
                                - Strategy — pluggable discount calculation
                                - Factory — discount strategy selection
                                
                                **Architecture principles:**
                                - SOLID throughout all layers
                                - Clean layered architecture (Controller → Service → Repository)
                                - RFC 7807 Problem Details error format
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ShopFlow Team")
                                .url("https://github.com/yourusername/shopflow-api")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development")
                ));
    }
}
