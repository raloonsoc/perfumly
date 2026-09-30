package com.ralonsoc.backend.auth;

public class AccountBlockedException extends RuntimeException {
    public AccountBlockedException() {
        super("Account blocked");
    }
}
