package com.ralonsoc.backend.auth.dto;

import org.springframework.http.ResponseCookie;

public record AuthResponse(UserProfileResponse profile, ResponseCookie cookie) {
}
