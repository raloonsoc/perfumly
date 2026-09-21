package com.ralonsoc.backend.user;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class UserFavoriteId implements Serializable {

    private UUID userId;
    private UUID perfumeId;
}
