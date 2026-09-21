package com.ralonsoc.backend.perfume.dto;

import java.util.UUID;

public record AccordResponse(UUID id, String name, Integer position) {
}
