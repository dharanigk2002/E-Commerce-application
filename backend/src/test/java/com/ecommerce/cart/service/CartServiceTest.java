package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.AddCartItemRequest;
import com.ecommerce.cart.dto.UpdateCartItemRequest;
import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.entity.CartItem;
import com.ecommerce.cart.repository.CartItemRepository;
import com.ecommerce.cart.repository.CartRepository;
import com.ecommerce.common.exception.BadRequestException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.user.entity.Role;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    private static final String EMAIL = "dharani@example.com";

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void getCartShouldCreateCartWhenUserHasNoCart() {
        User user = user();

        when(cartRepository.findByUserEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = cartService.getCart(EMAIL);

        assertThat(response.items()).isEmpty();
        assertThat(response.totalItems()).isZero();
        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addItemShouldAddProductToCartAndCalculateTotals() {
        Cart cart = new Cart(user());
        Product product = product();

        when(cartRepository.findByUserEmail(EMAIL)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepository.save(cart)).thenReturn(cart);

        var response = cartService.addItem(EMAIL, new AddCartItemRequest(1L, 2));

        assertThat(cart.getItems()).hasSize(1);
        assertThat(response.totalItems()).isEqualTo(2);
        assertThat(response.totalAmount()).isEqualByComparingTo("259.98");
        assertThat(response.items().get(0).productName()).isEqualTo("Keyboard");
        assertThat(response.items().get(0).lineTotal()).isEqualByComparingTo("259.98");
    }

    @Test
    void addItemShouldIncreaseQuantityWhenProductAlreadyExistsInCart() {
        Cart cart = new Cart(user());
        Product product = product();
        cart.addItem(new CartItem(product, 2));

        when(cartRepository.findByUserEmail(EMAIL)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        var response = cartService.addItem(EMAIL, new AddCartItemRequest(1L, 3));

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
        assertThat(response.totalItems()).isEqualTo(5);
    }

    @Test
    void addItemShouldThrowWhenQuantityExceedsStock() {
        Cart cart = new Cart(user());
        Product product = product();

        when(cartRepository.findByUserEmail(EMAIL)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addItem(EMAIL, new AddCartItemRequest(1L, 99)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Requested quantity exceeds available stock");
    }

    @Test
    void updateItemShouldThrowWhenItemDoesNotBelongToUsersCart() {
        Cart cart = new Cart(user());

        when(cartRepository.findByUserEmail(EMAIL)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.updateItem(EMAIL, 50L, new UpdateCartItemRequest(2)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Cart item not found with id: 50");
    }

    private User user() {
        return new User("Dharani", EMAIL, "encoded-password", Role.CUSTOMER);
    }

    private Product product() {
        Product product = new Product(
                "Keyboard",
                "Mechanical keyboard",
                new BigDecimal("129.99"),
                10,
                true
        );
        ReflectionTestUtils.setField(product, "id", 1L);
        return product;
    }
}
