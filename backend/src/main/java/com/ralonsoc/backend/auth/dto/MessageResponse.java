package com.ralonsoc.backend.auth.dto;

// Generic message-only response, reused where the caller only needs a confirmation
// string (forgot-password, reset-password) rather than a typed payload.
public record MessageResponse(String message) {
}
