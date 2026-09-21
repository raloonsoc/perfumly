package com.ralonsoc.backend.review;

import com.ralonsoc.backend.review.dto.ReviewRequest;
import com.ralonsoc.backend.review.dto.ReviewResponse;
import com.ralonsoc.backend.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/api/perfumes/{perfumeId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(
            @PathVariable UUID perfumeId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ReviewRequest request) {
        return reviewService.createReview(perfumeId, user, request);
    }

    @GetMapping("/api/perfumes/{perfumeId}/reviews")
    public Page<ReviewResponse> listReviews(@PathVariable UUID perfumeId, Pageable pageable) {
        return reviewService.listReviewsForPerfume(perfumeId, pageable);
    }

    @PutMapping("/api/reviews/{id}")
    public ReviewResponse updateReview(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ReviewRequest request) {
        return reviewService.updateReview(id, user, request);
    }

    @DeleteMapping("/api/reviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        reviewService.deleteReview(id, user);
    }
}
