package com.ecommerce.common.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class Address {

    private String addressLine;

    private String city;

    private String state;

    private String postalCode;

    private String country;

    protected Address() {
    }

    public Address(String addressLine, String city, String state, String postalCode, String country) {
        this.addressLine = addressLine;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }

    public static Address copyOf(Address address) {
        return new Address(
                address.getAddressLine(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry()
        );
    }

    public String getAddressLine() {
        return addressLine;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    public boolean isComplete() {
        return isPresent(addressLine)
                && isPresent(city)
                && isPresent(state)
                && isPresent(postalCode)
                && isPresent(country);
    }

    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
