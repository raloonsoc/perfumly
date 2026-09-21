package com.ralonsoc.backend.perfume.dto;


import java.util.UUID;

public record BrandResponse(UUID id, String name, String country) {
}
