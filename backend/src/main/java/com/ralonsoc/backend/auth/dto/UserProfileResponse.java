package com.ralonsoc.backend.auth.dto;

import com.ralonsoc.backend.user.Role;

import java.util.UUID;

public record UserProfileResponse(UUID id, String username, String email, Role role) {
}
