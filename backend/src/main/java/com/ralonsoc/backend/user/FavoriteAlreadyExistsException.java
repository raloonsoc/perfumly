package com.ralonsoc.backend.user;

import java.util.UUID;

public class FavoriteAlreadyExistsException extends RuntimeException {
    public FavoriteAlreadyExistsException(UUID perfumeId) {
        super("Perfume already in favorites: " + perfumeId);
    }
}
