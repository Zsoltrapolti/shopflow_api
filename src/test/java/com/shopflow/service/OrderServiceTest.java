package com.shopflow.service;

import com.shopflow.exception.InsufficientStockException;
import com.shopflow.exception.InvalidOrderStateException;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.model.dto.OrderDto;
import com.shopflow.model.dto.OrderItemDto;
import com.shopflow.model.entity.Order;
import com.shopflow.model.entity.OrderItem;
import com.shopflow.model.entity.Product;
import com.shopflow.pattern.factory.DiscountStrategyFactory;
import com.shopflow.pattern.observer.OrderEventListener;
import com.shopflow.pattern.strategy.DiscountStrategy;
import com.shopflow.repository.OrderRepository;
import com.shopflow.repository.ProductRepository;
import com.shopflow.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService")
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private DiscountStrategyFactory discountStrategyFactory;
    @Mock private OrderEventListener eventListener;
    @Mock private DiscountStrategy noDiscount;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("Sony Headphones")
                .price(new BigDecimal("149.99"))
                .stockQuantity(20)
                .category(Product.Category.ELECTRONICS)
                .build();

        when(noDiscount.getName()).thenReturn("NO_DISCOUNT");
        when(noDiscount.calculateDiscount(any())).thenReturn(BigDecimal.ZERO);
        when(discountStrategyFactory.getStrategy(any(Order.class))).thenReturn(noDiscount);
    }

    @Nested
    @DisplayName("createOrder()")
    class CreateOrder {

        @Test
        @DisplayName("should create order and deduct stock")
        void shouldCreateOrderAndDeductStock() {
            OrderDto.Request request = OrderDto.Request.builder()
                    .customerEmail("john@example.com")
                    .items(List.of(
                            OrderItemDto.Request.builder().productId(1L).quantity(2).build()
                    ))
                    .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(any())).thenReturn(sampleProduct);
            when(orderRepository.save(any())).thenAnswer(inv -> {
                Order o = inv.getArgument(0);
                o.setId(1L);
                o.setCreatedAt(Instant.now());
                o.setUpdatedAt(Instant.now());
                return o;
            });

            OrderDto.Response response = orderService.createOrder(request);

            assertThat(response).isNotNull();
            assertThat(response.getCustomerEmail()).isEqualTo("john@example.com");
            assertThat(response.getStatus()).isEqualTo(Order.OrderStatus.PENDING);

            // Stock should be deducted
            assertThat(sampleProduct.getStockQuantity()).isEqualTo(18); // 20 - 2
            verify(eventListener).onOrderCreated(any(Order.class));
        }

        @Test
        @DisplayName("should throw InsufficientStockException when stock is too low")
        void shouldThrowWhenInsufficientStock() {
            sampleProduct.setStockQuantity(1); // Only 1 in stock

            OrderDto.Request request = OrderDto.Request.builder()
                    .customerEmail("jane@example.com")
                    .items(List.of(
                            OrderItemDto.Request.builder().productId(1L).quantity(5).build() // wants 5
                    ))
                    .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

            assertThatThrownBy(() -> orderService.createOrder(request))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("Sony Headphones");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException for unknown product")
        void shouldThrowForUnknownProduct() {
            OrderDto.Request request = OrderDto.Request.builder()
                    .customerEmail("test@example.com")
                    .items(List.of(
                            OrderItemDto.Request.builder().productId(999L).quantity(1).build()
                    ))
                    .build();

            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.createOrder(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("updateOrderStatus()")
    class UpdateOrderStatus {

        @Test
        @DisplayName("should allow PENDING → CONFIRMED transition")
        void shouldAllowPendingToConfirmed() {
            Order order = buildOrder(1L, Order.OrderStatus.PENDING);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            OrderDto.Response result = orderService.updateOrderStatus(1L, Order.OrderStatus.CONFIRMED);

            assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
            verify(eventListener).onOrderStatusChanged(any(), eq(Order.OrderStatus.PENDING));
        }

        @Test
        @DisplayName("should reject DELIVERED → PENDING (invalid transition)")
        void shouldRejectInvalidTransition() {
            Order order = buildOrder(1L, Order.OrderStatus.DELIVERED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, Order.OrderStatus.PENDING))
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("DELIVERED");
        }
    }

    @Nested
    @DisplayName("cancelOrder()")
    class CancelOrder {

        @Test
        @DisplayName("should cancel PENDING order and restore stock")
        void shouldCancelAndRestoreStock() {
            Order order = buildOrder(1L, Order.OrderStatus.PENDING);
            OrderItem item = OrderItem.builder()
                    .product(sampleProduct)
                    .quantity(3)
                    .unitPrice(sampleProduct.getPrice())
                    .build();
            order.getItems().add(item);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            orderService.cancelOrder(1L);

            assertThat(sampleProduct.getStockQuantity()).isEqualTo(23); // 20 + 3 restored
            assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("should throw when attempting to cancel a SHIPPED order")
        void shouldThrowWhenCancellingShippedOrder() {
            Order order = buildOrder(1L, Order.OrderStatus.SHIPPED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.cancelOrder(1L))
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("SHIPPED");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Order buildOrder(Long id, Order.OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setCustomerEmail("customer@example.com");
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("149.99"));
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        return order;
    }
}
