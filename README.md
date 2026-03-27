# 🛒 ShopFlow API

> Production-grade E-Commerce REST API built with **Spring Boot 3**, **Java 21**, and clean architecture principles.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Tests](https://img.shields.io/badge/Tests-JUnit%205-red?logo=junit5)](https://junit.org/junit5/)

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Architecture](#-architecture)
- [Design Patterns](#-design-patterns)
- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
- [API Reference](#-api-reference)
- [Project Structure](#-project-structure)
- [Testing](#-testing)
- [SOLID Principles](#-solid-principles)

---

## 🔍 Overview

ShopFlow is a fully featured e-commerce backend demonstrating **real-world Spring Boot patterns** used in production systems. It covers the complete order lifecycle — from product catalogue browsing to order placement, discount calculation, stock management, and status tracking — all wired together with clean interfaces and testable code.

**Key highlights:**

| Feature | Implementation |
|---|---|
| REST API | Spring MVC with full CRUD |
| Design Patterns | Observer, Strategy, Factory |
| Error Handling | RFC 7807 Problem Details |
| Validation | Bean Validation (JSR-380) |
| Security | Spring Security (stateless, JWT-ready) |
| Documentation | Swagger UI / OpenAPI 3 |
| Testing | JUnit 5 + Mockito + MockMvc |
| Architecture | Layered (Controller → Service → Repository) |

---

## 🏛️ Architecture

```
┌─────────────────────────────────────────────────┐
│                  Client / Swagger UI            │
└────────────────────┬────────────────────────────┘
                     │ HTTP
┌────────────────────▼────────────────────────────┐
│              Controller Layer                   │
│         ProductController, OrderController      │
│   • HTTP binding only  • Delegates to service   │
└────────────────────┬────────────────────────────┘
                     │ DTOs
┌────────────────────▼────────────────────────────┐
│               Service Layer                     │
│        ProductService, OrderService             │
│   • Business logic  • Pattern orchestration     │
│   • Transaction management                     │
└──────┬──────────────────────────┬───────────────┘
       │ Entities                 │ Patterns
┌──────▼──────────┐   ┌───────────▼──────────────┐
│  Repository     │   │  Observer / Strategy /   │
│  Layer (JPA)    │   │  Factory                 │
│                 │   │                          │
│  ProductRepo    │   │  EmailListener           │
│  OrderRepo      │   │  InventoryListener       │
└─────────────────┘   │  DiscountStrategies      │
                      │  DiscountStrategyFactory │
                      └──────────────────────────┘
```

---

## 🎨 Design Patterns

### 1. Observer Pattern — Order Event System

Decouples order lifecycle events from their side effects. New listeners (analytics, push notifications, etc.) can be added **without modifying the core order logic** — demonstrating the **Open/Closed Principle**.

```
OrderService ──publishes──► OrderEventListener (interface)
                                    ▲
                    ┌───────────────┼───────────────┐
                    │               │               │
          EmailNotification  InventoryTracking  (+ more)
              Listener           Listener
```

```java
// Adding a new listener requires zero changes to OrderService
@Component
public class AnalyticsListener implements OrderEventListener {
    @Override
    public void onOrderCreated(Order order) {
        analyticsService.trackConversion(order);
    }
}
```

### 2. Strategy Pattern — Discount Calculation

Pluggable, interchangeable discount algorithms behind a clean interface. The calling code never knows which algorithm runs.

```java
public interface DiscountStrategy {
    BigDecimal calculateDiscount(Order order);
    String getName();
}

// Three concrete strategies:
// • NoDiscountStrategy     → 0% always
// • PercentageDiscount     → flat 10%
// • LoyaltyDiscount        → 15% on orders > $200
```

### 3. Factory Pattern — Strategy Selection

The `DiscountStrategyFactory` encapsulates the selection logic, keeping it out of the service layer.

```java
// OrderService simply asks the factory — no if/else chains
DiscountStrategy strategy = discountStrategyFactory.getStrategy(order);
BigDecimal discount = strategy.calculateDiscount(order);
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (Records, Pattern Matching, Text Blocks) |
| Framework | Spring Boot 3.2 |
| Persistence | Spring Data JPA + Hibernate |
| Database | H2 (dev) / PostgreSQL-ready |
| Security | Spring Security 6 |
| Validation | Jakarta Bean Validation |
| API Docs | SpringDoc OpenAPI 3 / Swagger UI |
| Utilities | Lombok |
| Testing | JUnit 5, Mockito, MockMvc |
| Build | Maven 3.9 |

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+

### Run locally

```bash
# Clone the repository
git clone https://github.com/yourusername/shopflow-api.git
cd shopflow-api

# Run with Maven
./mvnw spring-boot:run
```

The API starts on **http://localhost:8080**

### Explore

| URL | Description |
|---|---|
| http://localhost:8080/swagger-ui.html | Interactive API docs |
| http://localhost:8080/h2-console | In-memory DB console |
| http://localhost:8080/v3/api-docs | OpenAPI JSON spec |

> **H2 Console settings:** JDBC URL: `jdbc:h2:mem:shopflowdb`, User: `sa`, Password: *(empty)*

---

## 📡 API Reference

### Products

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/products` | List all products (paginated) |
| `GET` | `/api/v1/products/{id}` | Get product by ID |
| `GET` | `/api/v1/products/category/{category}` | Filter by category |
| `GET` | `/api/v1/products/search?keyword=` | Search by name |
| `POST` | `/api/v1/products` | Create product 🔒 |
| `PUT` | `/api/v1/products/{id}` | Update product 🔒 |
| `DELETE` | `/api/v1/products/{id}` | Delete product 🔒 |

### Orders

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/orders` | Place a new order 🔒 |
| `GET` | `/api/v1/orders/{id}` | Get order by ID 🔒 |
| `GET` | `/api/v1/orders?customerEmail=` | Get customer orders 🔒 |
| `PATCH` | `/api/v1/orders/{id}/status` | Update order status 🔒 |
| `DELETE` | `/api/v1/orders/{id}` | Cancel order 🔒 |

🔒 = Requires authentication

### Example: Place an Order

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -u user:password \
  -d '{
    "customerEmail": "john@example.com",
    "items": [
      { "productId": 1, "quantity": 2 },
      { "productId": 4, "quantity": 1 }
    ]
  }'
```

### Order State Machine

```
PENDING ──► CONFIRMED ──► SHIPPED ──► DELIVERED
   │              │
   └──────────────┴──────────────────► CANCELLED
```

Attempting an invalid transition (e.g. `SHIPPED → PENDING`) returns a `422 Unprocessable Entity` with a descriptive Problem Detail.

### Error Format (RFC 7807)

All errors follow the RFC 7807 Problem Details standard:

```json
{
  "type": "https://shopflow.com/errors/not-found",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Product not found with id: 99",
  "timestamp": "2024-03-15T10:30:00Z"
}
```

---

## 📁 Project Structure

```
src/
├── main/java/com/shopflow/
│   ├── ShopFlowApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java       # Spring Security (stateless)
│   │   └── OpenApiConfig.java        # Swagger configuration
│   ├── controller/
│   │   ├── ProductController.java
│   │   └── OrderController.java
│   ├── service/
│   │   ├── ProductService.java       # Interface (DIP)
│   │   ├── OrderService.java         # Interface (DIP)
│   │   └── impl/
│   │       ├── ProductServiceImpl.java
│   │       └── OrderServiceImpl.java
│   ├── repository/
│   │   ├── ProductRepository.java
│   │   └── OrderRepository.java
│   ├── model/
│   │   ├── entity/
│   │   │   ├── Product.java
│   │   │   ├── Order.java
│   │   │   └── OrderItem.java
│   │   └── dto/
│   │       ├── ProductDto.java       # Request + Response nested
│   │       ├── OrderDto.java
│   │       └── OrderItemDto.java
│   ├── pattern/
│   │   ├── observer/
│   │   │   ├── OrderEventListener.java        # Observer interface
│   │   │   ├── EmailNotificationListener.java # Concrete observer
│   │   │   └── InventoryTrackingListener.java # Concrete observer
│   │   ├── strategy/
│   │   │   ├── DiscountStrategy.java          # Strategy interface
│   │   │   └── DiscountStrategies.java        # Concrete strategies
│   │   └── factory/
│   │       └── DiscountStrategyFactory.java   # Factory
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       ├── ResourceNotFoundException.java
│       ├── DuplicateResourceException.java
│       ├── InsufficientStockException.java
│       └── InvalidOrderStateException.java
└── test/java/com/shopflow/
    ├── controller/
    │   └── ProductControllerTest.java   # MockMvc integration tests
    └── service/
        ├── ProductServiceTest.java      # Unit tests with Mockito
        └── DiscountStrategyTest.java    # Pattern unit tests
```

---

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Run with coverage report
./mvnw test jacoco:report
```

### Test Strategy

| Test Type | Tool | Coverage |
|---|---|---|
| Unit tests | JUnit 5 + Mockito | Service layer, patterns |
| Integration tests | MockMvc + @WebMvcTest | Controller layer, validation |
| Pattern tests | JUnit 5 | Strategy + Factory |

Tests follow the **Arrange / Act / Assert** pattern with `@Nested` classes for readable grouping.

---

## ✅ SOLID Principles

| Principle | Where applied |
|---|---|
| **S** — Single Responsibility | Each class has one clear job: controllers handle HTTP, services handle logic, repositories handle data |
| **O** — Open/Closed | `OrderEventListener` — add new observers without touching existing code |
| **L** — Liskov Substitution | All `DiscountStrategy` implementations are fully interchangeable |
| **I** — Interface Segregation | `ProductService` and `OrderService` are separate contracts |
| **D** — Dependency Inversion | Controllers depend on service interfaces, not concrete implementations |

---

## 📄 License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
