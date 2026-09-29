package com.ralonsoc.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 8, max = 64)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-=\\[\\]{};:'\",.<>/\\\\|~`]).+$",
                message = "The password must contain at least one uppercase letter, one lowercase letter, one digit and one special character.")
        String newPassword) {
}
