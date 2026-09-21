package com.ralonsoc.backend.review;

import com.ralonsoc.backend.perfume.Perfume;
import com.ralonsoc.backend.perfume.PerfumeNotFoundException;
import com.ralonsoc.backend.perfume.PerfumeRepository;
import com.ralonsoc.backend.review.dto.ReviewRequest;
import com.ralonsoc.backend.review.dto.ReviewResponse;
import com.ralonsoc.backend.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PerfumeRepository perfumeRepository;

    public ReviewResponse createReview(UUID perfumeId, User user, ReviewRequest request) {
        if (reviewRepository.existsByUserIdAndPerfumeId(user.getId(), perfumeId)) {
            throw new DuplicateReviewException(perfumeId);
        }

        Perfume perfume = perfumeRepository.findById(perfumeId)
                .orElseThrow(() -> new PerfumeNotFoundException(perfumeId));

        Review review = new Review();
        review.setPerfume(perfume);
        review.setUser(user);
        review.setRating(request.rating());
        review.setDescription(request.description());

        Review saved = reviewRepository.saveAndFlush(review);
        return toResponse(saved);
    }

    public Page<ReviewResponse> listReviewsForPerfume(UUID perfumeId, Pageable pageable) {
        return reviewRepository.findByPerfumeId(perfumeId, pageable).map(this::toResponse);
    }

    public ReviewResponse updateReview(UUID reviewId, User user, ReviewRequest request) {
        Review review = getOwnedReview(reviewId, user);
        review.setRating(request.rating());
        review.setDescription(request.description());
        reviewRepository.flush();
        return toResponse(review);
    }

    public void deleteReview(UUID reviewId, User user) {
        Review review = getOwnedReview(reviewId, user);
        reviewRepository.delete(review);
    }

    private Review getOwnedReview(UUID reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new ReviewAccessDeniedException();
        }
        return review;
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getPerfume().getId(),
                review.getUser().getUsername(),
                review.getRating(),
                review.getDescription(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}

