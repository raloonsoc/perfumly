package com.ralonsoc.backend.auth;

public class SamePasswordException extends RuntimeException {
    public SamePasswordException() {
        super("New password must be different from your current password");
    }
}
