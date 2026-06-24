package com.ecommerce.order.entity;

import com.ecommerce.common.model.Address;
import com.ecommerce.user.entity.User;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "addressLine", column = @Column(name = "shipping_address_line", nullable = false, length = 255)),
            @AttributeOverride(name = "city", column = @Column(name = "shipping_city", nullable = false, length = 120)),
            @AttributeOverride(name = "state", column = @Column(name = "shipping_state", nullable = false, length = 120)),
            @AttributeOverride(name = "postalCode", column = @Column(name = "shipping_postal_code", nullable = false, length = 30)),
            @AttributeOverride(name = "country", column = @Column(name = "shipping_country", nullable = false, length = 120))
    })
    private Address shippingAddress = new Address("", "", "", "", "");

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    protected Order() {
    }

    public Order(User user) {
        this.user = user;
    }

    public Order(User user, Address shippingAddress) {
        this.user = user;
        this.shippingAddress = Address.copyOf(shippingAddress);
    }

    @PrePersist
    void beforeCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void beforeUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getShippingAddressLine() {
        return shippingAddress.getAddressLine();
    }

    public String getShippingCity() {
        return shippingAddress.getCity();
    }

    public String getShippingState() {
        return shippingAddress.getState();
    }

    public String getShippingPostalCode() {
        return shippingAddress.getPostalCode();
    }

    public String getShippingCountry() {
        return shippingAddress.getCountry();
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        totalAmount = totalAmount.add(item.getLineTotal());
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
