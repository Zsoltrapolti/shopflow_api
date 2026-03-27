package com.shopflow.service.impl;

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
import com.shopflow.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.EnumSet;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private static final Set<Order.OrderStatus> CANCELLABLE_STATUSES =
            EnumSet.of(Order.OrderStatus.PENDING, Order.OrderStatus.CONFIRMED);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final DiscountStrategyFactory discountStrategyFactory;
    private final List<OrderEventListener> eventListeners;

    @Override
    @Transactional
    public OrderDto.Response createOrder(OrderDto.Request request) {
        Order order = Order.builder()
                .customerEmail(request.getCustomerEmail())
                .status(Order.OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        List<OrderItem> items = request.getItems().stream()
                .map(itemReq -> buildOrderItem(order, itemReq))
                .toList();

        order.getItems().addAll(items);

        BigDecimal subtotal = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(subtotal);

        // Apply discount via Strategy + Factory
        DiscountStrategy strategy = discountStrategyFactory.getStrategy(order);
        BigDecimal discount = strategy.calculateDiscount(order);
        order.setTotalAmount(subtotal.subtract(discount));

        log.info("Applying discount strategy '{}': -{}", strategy.getName(), discount);

        Order saved = orderRepository.save(order);

        // Notify observers
        eventListeners.forEach(l -> l.onOrderCreated(saved));

        log.info("Created order #{} for {}", saved.getId(), saved.getCustomerEmail());
        return toResponse(saved);
    }

    @Override
    public OrderDto.Response getOrderById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public Page<OrderDto.Response> getOrdersByCustomer(String customerEmail, Pageable pageable) {
        return orderRepository.findByCustomerEmail(customerEmail, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public OrderDto.Response updateOrderStatus(Long id, Order.OrderStatus newStatus) {
        Order order = findOrThrow(id);
        Order.OrderStatus previous = order.getStatus();

        validateStatusTransition(previous, newStatus);
        order.setStatus(newStatus);

        Order saved = orderRepository.save(order);
        eventListeners.forEach(l -> l.onOrderStatusChanged(saved, previous));

        log.info("Order #{} status: {} → {}", id, previous, newStatus);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        Order order = findOrThrow(id);

        if (!CANCELLABLE_STATUSES.contains(order.getStatus())) {
            throw new InvalidOrderStateException(
                    "Order #" + id + " cannot be cancelled in status: " + order.getStatus());
        }

        // Restore stock
        order.getItems().forEach(item -> {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        });

        Order.OrderStatus previous = order.getStatus();
        order.setStatus(Order.OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        eventListeners.forEach(l -> l.onOrderStatusChanged(saved, previous));

        log.info("Cancelled order #{}", id);
    }


    private OrderItem buildOrderItem(Order order, com.shopflow.model.dto.OrderItemDto.Request req) {
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", req.getProductId()));

        if (product.getStockQuantity() < req.getQuantity()) {
            throw new InsufficientStockException(product.getName(),
                    product.getStockQuantity(), req.getQuantity());
        }

        product.setStockQuantity(product.getStockQuantity() - req.getQuantity());
        productRepository.save(product);

        return OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(req.getQuantity())
                .unitPrice(product.getPrice())
                .build();
    }

    private void validateStatusTransition(Order.OrderStatus from, Order.OrderStatus to) {
        boolean valid = switch (from) {
            case PENDING -> to == Order.OrderStatus.CONFIRMED || to == Order.OrderStatus.CANCELLED;
            case CONFIRMED -> to == Order.OrderStatus.SHIPPED || to == Order.OrderStatus.CANCELLED;
            case SHIPPED -> to == Order.OrderStatus.DELIVERED;
            default -> false;
        };

        if (!valid) {
            throw new InvalidOrderStateException(
                    "Invalid transition: " + from + " → " + to);
        }
    }

    private Order findOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    private OrderDto.Response toResponse(Order order) {
        List<OrderItemDto.Response> items = order.getItems().stream()
                .map(item -> OrderItemDto.Response.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        return OrderDto.Response.builder()
                .id(order.getId())
                .customerEmail(order.getCustomerEmail())
                .items(items)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
