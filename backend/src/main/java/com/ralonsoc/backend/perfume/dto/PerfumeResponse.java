package com.ralonsoc.backend.perfume.dto;

import com.ralonsoc.backend.perfume.Gender;

import java.util.List;
import java.util.UUID;

public record PerfumeResponse(UUID id, String name, BrandResponse brand, Gender gender, Integer year, PerfumeNotesResponse notes, List<AccordResponse> accords) {
}
