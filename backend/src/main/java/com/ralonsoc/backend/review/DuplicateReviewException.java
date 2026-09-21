package com.ralonsoc.backend.review;

import java.util.UUID;

public class DuplicateReviewException extends RuntimeException {
    public DuplicateReviewException(UUID perfumeId) {
        super("You already reviewed this perfume:  " + perfumeId);
    }
}
