package com.ralonsoc.backend.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Page<Review> findByPerfumeId(UUID perfumeId, Pageable pageable);
    boolean existsByUserIdAndPerfumeId(UUID userId, UUID perfumeId);
}
