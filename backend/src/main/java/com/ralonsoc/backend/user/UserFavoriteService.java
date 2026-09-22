package com.ralonsoc.backend.user;

import com.ralonsoc.backend.perfume.Perfume;
import com.ralonsoc.backend.perfume.PerfumeNotFoundException;
import com.ralonsoc.backend.perfume.PerfumeRepository;
import com.ralonsoc.backend.user.dto.FavoritePerfumeResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserFavoriteService {

    private final UserFavoriteRepository userFavoriteRepository;
    private final PerfumeRepository perfumeRepository;

    public void addFavorite(User user, UUID perfumeId) {
        if (userFavoriteRepository.existsByUserIdAndPerfumeId(user.getId(), perfumeId)) {
            throw new FavoriteAlreadyExistsException(perfumeId);
        }

        Perfume perfume = perfumeRepository.findById(perfumeId)
                .orElseThrow(() -> new PerfumeNotFoundException(perfumeId));
        UserFavorite favorite = new UserFavorite();
        favorite.setUser(user);
        favorite.setPerfume(perfume);
        userFavoriteRepository.save(favorite);
    }

    public void removeFavorite(User user, UUID perfumeId) {
        UserFavorite favorite = userFavoriteRepository.findByUserIdAndPerfumeId(user.getId(), perfumeId)
                .orElseThrow(() -> new FavoriteNotFoundException(perfumeId));

        userFavoriteRepository.delete(favorite);
    }

    public Page<FavoritePerfumeResponse> listFavorites(User user, Pageable pageable) {
        return userFavoriteRepository.findByUserId(user.getId(), pageable).map(this::toResponse);
    }

    private FavoritePerfumeResponse toResponse(UserFavorite favorite) {
        Perfume perfume = favorite.getPerfume();
        return new FavoritePerfumeResponse(
                perfume.getId(),
                perfume.getName(),
                perfume.getBrand().getName(),
                perfume.getGender()
        );
    }
}
