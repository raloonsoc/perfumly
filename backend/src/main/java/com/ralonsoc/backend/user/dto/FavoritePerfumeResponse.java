package com.ralonsoc.backend.user.dto;

import com.ralonsoc.backend.perfume.Gender;

import java.util.UUID;

public record FavoritePerfumeResponse(UUID perfumeId, String name, String brandName, Gender gender) {
}
