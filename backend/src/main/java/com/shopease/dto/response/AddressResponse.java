package com.shopease.dto.response;

public record AddressResponse(
        String street,
        String city,
        String state,
        String postalCode,
        String country,
        String phone
) {
}
