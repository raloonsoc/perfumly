package com.ralonsoc.backend.user;

import com.ralonsoc.backend.perfume.Perfume;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_favorites")
@Getter
@Setter
public class UserFavorite {
    @EmbeddedId
    private UserFavoriteId id = new UserFavoriteId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("perfumeId")
    @JoinColumn(name = "perfume_id")
    private Perfume perfume;

}
