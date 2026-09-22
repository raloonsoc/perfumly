package com.ralonsoc.backend.user;

import java.util.UUID;

public class FavoriteNotFoundException extends RuntimeException {
    public FavoriteNotFoundException(UUID perfumeId) {
        super("Favorite not found for perfume: " + perfumeId);
    }
}
