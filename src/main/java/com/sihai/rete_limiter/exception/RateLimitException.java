package com.sihai.rete_limiter.exception;

public class RateLimitException extends Exception {

    public RateLimitException(String message) {
        super(message);
    }
}
