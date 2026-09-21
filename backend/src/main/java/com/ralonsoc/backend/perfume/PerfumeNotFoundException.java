package com.ralonsoc.backend.perfume;

import java.util.UUID;

public class PerfumeNotFoundException extends RuntimeException {
    public PerfumeNotFoundException(UUID id) {
        super("Perfume not found with id: " + id);
    }
}
