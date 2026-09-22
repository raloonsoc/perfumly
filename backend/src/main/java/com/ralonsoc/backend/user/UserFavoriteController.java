package com.ralonsoc.backend.user;

import com.ralonsoc.backend.user.dto.FavoritePerfumeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/me/favorites")
@RequiredArgsConstructor
public class UserFavoriteController {

    private final UserFavoriteService userFavoriteService;
    @PostMapping("/{perfumeId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addFavorite(@AuthenticationPrincipal User user, @PathVariable UUID perfumeId) {
        userFavoriteService.addFavorite(user, perfumeId);
    }

    @DeleteMapping("/{perfumeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavorite(@AuthenticationPrincipal User user, @PathVariable UUID perfumeId) {
        userFavoriteService.removeFavorite(user, perfumeId);
    }

    @GetMapping
    public Page<FavoritePerfumeResponse> listFavorites(@AuthenticationPrincipal User user, Pageable pageable) {
        return userFavoriteService.listFavorites(user, pageable);
    }
}
