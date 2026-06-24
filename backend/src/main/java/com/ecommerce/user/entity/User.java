package com.ecommerce.user.entity;

import com.ecommerce.common.model.Address;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "addressLine", column = @Column(name = "shipping_address_line")),
            @AttributeOverride(name = "city", column = @Column(name = "shipping_city", length = 120)),
            @AttributeOverride(name = "state", column = @Column(name = "shipping_state", length = 120)),
            @AttributeOverride(name = "postalCode", column = @Column(name = "shipping_postal_code", length = 30)),
            @AttributeOverride(name = "country", column = @Column(name = "shipping_country", length = 120))
    })
    private Address shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    protected User() {
    }

    public User(String fullName, String email, String passwordHash, Role role) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
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

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getShippingAddressLine() {
        return shippingAddress == null ? null : shippingAddress.getAddressLine();
    }

    public String getShippingCity() {
        return shippingAddress == null ? null : shippingAddress.getCity();
    }

    public String getShippingState() {
        return shippingAddress == null ? null : shippingAddress.getState();
    }

    public String getShippingPostalCode() {
        return shippingAddress == null ? null : shippingAddress.getPostalCode();
    }

    public String getShippingCountry() {
        return shippingAddress == null ? null : shippingAddress.getCountry();
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void updateShippingAddress(
            String shippingAddressLine,
            String shippingCity,
            String shippingState,
            String shippingPostalCode,
            String shippingCountry
    ) {
        this.shippingAddress = new Address(
                shippingAddressLine,
                shippingCity,
                shippingState,
                shippingPostalCode,
                shippingCountry
        );
    }

    public boolean hasShippingAddress() {
        return shippingAddress != null && shippingAddress.isComplete();
    }

    public Role getRole() {
        return role;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
