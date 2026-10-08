package com.shopease.exception;

/** Thrown when the request is well-formed but breaks a business rule (empty cart, duplicate email...). */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
