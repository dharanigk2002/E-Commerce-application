package com.ecommerce.order.service;

import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.entity.CartItem;
import com.ecommerce.cart.repository.CartRepository;
import com.ecommerce.common.exception.BadRequestException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.order.dto.OrderItemResponse;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.UpdateOrderStatusRequest;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.product.entity.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
    }

    @Transactional
    public OrderResponse createOrder(String userEmail) {
        Cart cart = cartRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Order order = new Order(cart.getUser());

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            validateProductForOrder(product, cartItem.getQuantity());

            order.addItem(new OrderItem(product, cartItem.getQuantity()));
            product.reduceStock(cartItem.getQuantity());
        }

        Order savedOrder = orderRepository.save(order);
        cart.clearItems();
        cartRepository.save(cart);

        return toResponse(savedOrder);
    }

    public List<OrderResponse> findCurrentUserOrders(String userEmail) {
        return orderRepository.findByUserEmailOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse findCurrentUserOrderById(String userEmail, Long orderId) {
        return orderRepository.findByIdAndUserEmail(orderId, userEmail)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    public List<OrderResponse> findAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setStatus(request.status());

        return toResponse(order);
    }

    private void validateProductForOrder(Product product, int quantity) {
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new BadRequestException("Product is not active: " + product.getName());
        }

        if (quantity > product.getAvailableStock()) {
            throw new BadRequestException("Requested quantity exceeds available stock for product: " + product.getName());
        }
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems()
                .stream()
                .sorted(Comparator.comparing(OrderItem::getId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toItemResponse)
                .toList();

        int totalItems = items.stream()
                .mapToInt(OrderItemResponse::quantity)
                .sum();

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                items,
                totalItems,
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getLineTotal()
        );
    }
}
