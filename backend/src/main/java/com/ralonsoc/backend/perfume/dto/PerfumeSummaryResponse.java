package com.ralonsoc.backend.perfume.dto;

import com.ralonsoc.backend.perfume.Gender;

import java.util.List;
import java.util.UUID;

public record PerfumeSummaryResponse(UUID id, String name, String brandName, Gender gender, Integer year, List<String> mainAccords) {
}
