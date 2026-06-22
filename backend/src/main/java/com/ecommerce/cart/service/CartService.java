package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.AddCartItemRequest;
import com.ecommerce.cart.dto.CartItemResponse;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.dto.UpdateCartItemRequest;
import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.entity.CartItem;
import com.ecommerce.cart.repository.CartItemRepository;
import com.ecommerce.cart.repository.CartRepository;
import com.ecommerce.common.exception.BadRequestException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;

@Service
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CartResponse getCart(String userEmail) {
        Cart cart = getOrCreateCart(userEmail);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse addItem(String userEmail, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(userEmail);
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.productId()));

        CartItem existingItem = findItemByProductId(cart, request.productId());
        int newQuantity = request.quantity();

        if (existingItem != null) {
            newQuantity = existingItem.getQuantity() + request.quantity();
            validateStock(product, newQuantity);
            existingItem.setQuantity(newQuantity);
            return toResponse(cart);
        }

        validateStock(product, newQuantity);
        cart.addItem(new CartItem(product, newQuantity));
        Cart savedCart = cartRepository.save(cart);

        return toResponse(savedCart);
    }

    @Transactional
    public CartResponse updateItem(String userEmail, Long itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userEmail);
        CartItem item = findOwnedItem(cart, itemId);

        validateStock(item.getProduct(), request.quantity());
        item.setQuantity(request.quantity());

        return toResponse(cart);
    }

    @Transactional
    public void removeItem(String userEmail, Long itemId) {
        Cart cart = getOrCreateCart(userEmail);
        CartItem item = findOwnedItem(cart, itemId);

        cart.removeItem(item);
        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(String userEmail) {
        Cart cart = getOrCreateCart(userEmail);
        cart.clearItems();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(String userEmail) {
        return cartRepository.findByUserEmail(userEmail)
                .orElseGet(() -> createCart(userEmail));
    }

    private Cart createCart(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return cartRepository.save(new Cart(user));
    }

    private CartItem findItemByProductId(Cart cart, Long productId) {
        return cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    private CartItem findOwnedItem(Cart cart, Long itemId) {
        return cart.getItems()
                .stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));
    }

    private void validateStock(Product product, int quantity) {
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new BadRequestException("Product is not active");
        }

        if (quantity > product.getAvailableStock()) {
            throw new BadRequestException("Requested quantity exceeds available stock");
        }
    }

    private CartResponse toResponse(Cart cart) {
        var items = cart.getItems()
                .stream()
                .sorted(Comparator.comparing(CartItem::getId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toItemResponse)
                .toList();

        int totalItems = items.stream()
                .mapToInt(CartItemResponse::quantity)
                .sum();

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), items, totalItems, totalAmount);
    }

    private CartItemResponse toItemResponse(CartItem item) {
        Product product = item.getProduct();
        BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                item.getQuantity(),
                lineTotal
        );
    }
}
