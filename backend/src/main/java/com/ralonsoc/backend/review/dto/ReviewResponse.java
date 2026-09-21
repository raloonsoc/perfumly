package com.ralonsoc.backend.review.dto;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(UUID id, UUID perfumeId, String username, Integer rating, String description, Instant createdAt, Instant updatedAt) {
}
