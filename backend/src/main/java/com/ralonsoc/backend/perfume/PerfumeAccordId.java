package com.ralonsoc.backend.perfume;

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
public class PerfumeAccordId implements Serializable {

    private UUID perfumeId;
    private UUID accordId;
}
