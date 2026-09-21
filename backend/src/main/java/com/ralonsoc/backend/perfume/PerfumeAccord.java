package com.ralonsoc.backend.perfume;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "perfume_accords")
@Getter
@Setter
public class PerfumeAccord {
    @EmbeddedId
    private PerfumeAccordId id = new PerfumeAccordId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("perfumeId")
    @JoinColumn(name = "perfume_id")
    private Perfume perfume;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("accordId")
    @JoinColumn(name = "accord_id")
    private Accord accord;

    @Column(nullable = false)
    private Integer position;
}
