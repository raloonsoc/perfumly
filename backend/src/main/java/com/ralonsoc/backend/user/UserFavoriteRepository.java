package com.ralonsoc.backend.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, UserFavoriteId> {
    Page<UserFavorite> findByUserId(UUID userId, Pageable pageable);
    Optional<UserFavorite> findByUserIdAndPerfumeId(UUID userId, UUID perfumeId);
    boolean existsByUserIdAndPerfumeId(UUID userId, UUID perfumeId);

}
