package com.ecommerce.order.service;

import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.entity.CartItem;
import com.ecommerce.cart.repository.CartRepository;
import com.ecommerce.common.exception.BadRequestException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.order.dto.UpdateOrderStatusRequest;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.product.entity.Product;
import com.ecommerce.user.entity.Role;
import com.ecommerce.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final String EMAIL = "dharani@example.com";

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrderShouldCreateOrderReduceStockAndClearCart() {
        Product product = product(1L, "Keyboard", "129.99", 10, true);
        Cart cart = cartWithItem(product, 2);

        when(cartRepository.findByUserEmail(EMAIL)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.createOrder(EMAIL);

        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.totalItems()).isEqualTo(2);
        assertThat(response.totalAmount()).isEqualByComparingTo("259.98");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).productName()).isEqualTo("Keyboard");
        assertThat(product.getAvailableStock()).isEqualTo(8);
        assertThat(cart.getItems()).isEmpty();

        verify(orderRepository).save(any(Order.class));
        verify(cartRepository).save(cart);
    }

    @Test
    void createOrderShouldRejectEmptyCart() {
        Cart cart = new Cart(user());

        when(cartRepository.findByUserEmail(EMAIL)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.createOrder(EMAIL))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cart is empty");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrderShouldRejectInsufficientStock() {
        Product product = product(1L, "Keyboard", "129.99", 1, true);
        Cart cart = cartWithItem(product, 2);

        when(cartRepository.findByUserEmail(EMAIL)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.createOrder(EMAIL))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Requested quantity exceeds available stock for product: Keyboard");

        assertThat(product.getAvailableStock()).isEqualTo(1);
        assertThat(cart.getItems()).hasSize(1);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void findCurrentUserOrderByIdShouldRejectOrderOwnedByAnotherUser() {
        when(orderRepository.findByIdAndUserEmail(25L, EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findCurrentUserOrderById(EMAIL, 25L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order not found with id: 25");
    }

    @Test
    void updateStatusShouldUpdateOrderStatus() {
        Order order = new Order(user());
        when(orderRepository.findWithItemsById(1L)).thenReturn(Optional.of(order));

        var response = orderService.updateStatus(1L, new UpdateOrderStatusRequest(OrderStatus.CONFIRMED));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void findCurrentUserOrdersShouldReturnOrdersForUser() {
        Order order = new Order(user());
        Product product = product(1L, "Keyboard", "129.99", 10, true);
        order.addItem(new com.ecommerce.order.entity.OrderItem(product, 2));

        when(orderRepository.findByUserEmailOrderByCreatedAtDesc(EMAIL)).thenReturn(List.of(order));

        var response = orderService.findCurrentUserOrders(EMAIL);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).totalItems()).isEqualTo(2);
        assertThat(response.get(0).totalAmount()).isEqualByComparingTo("259.98");
    }

    private Cart cartWithItem(Product product, int quantity) {
        Cart cart = new Cart(user());
        cart.addItem(new CartItem(product, quantity));
        return cart;
    }

    private User user() {
        return new User("Dharani", EMAIL, "encoded-password", Role.CUSTOMER);
    }

    private Product product(Long id, String name, String price, int stock, boolean active) {
        Product product = new Product(
                name,
                name + " description",
                new BigDecimal(price),
                stock,
                active
        );
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
