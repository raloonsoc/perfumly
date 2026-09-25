package com.ralonsoc.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Requires: min 8 chars, at least one lowercase, one uppercase, one digit and one special char.
public record RegisterRequest(@NotBlank @Size(max = 50) String username, @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 64)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-=\\[\\]{};:'\",.<>/\\\\|~`]).+$",
                message = "The password must contain at least one uppercase letter, one lowercase letter, one digit and one special character.")
        String password) {
}
