package com.ralonsoc.backend.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReviewRequest(@Min(1) @Max(10) Integer rating, @NotBlank String description) {
}
