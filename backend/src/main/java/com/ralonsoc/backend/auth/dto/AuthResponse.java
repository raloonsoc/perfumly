package com.ralonsoc.backend.auth.dto;

public record AuthResponse(String token, String username, String email) {
}
