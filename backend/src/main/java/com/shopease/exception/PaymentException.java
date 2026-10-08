package com.shopease.exception;

/** Thrown when the payment provider (Stripe) fails or rejects something. */
public class PaymentException extends RuntimeException {

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
