package com.ralonsoc.backend.auth;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {
        super("Invalid or already used token");
    }
}
