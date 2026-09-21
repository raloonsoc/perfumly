package com.ralonsoc.backend.review;

public class ReviewAccessDeniedException extends RuntimeException {
    public ReviewAccessDeniedException() {
        super("You can only modify your own reviews");
    }
}
