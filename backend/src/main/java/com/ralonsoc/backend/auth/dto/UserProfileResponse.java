package com.ralonsoc.backend.auth.dto;

import java.util.UUID;

public record UserProfileResponse(UUID id, String username, String email) {
}
