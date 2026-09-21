package com.ralonsoc.backend.perfume;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "perfume_notes")
@Getter
@Setter
public class PerfumeNote {
    @EmbeddedId
    private PerfumeNoteId id = new PerfumeNoteId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("perfumeId")
    @JoinColumn(name = "perfume_id")
    private Perfume perfume;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("noteId")
    @JoinColumn(name = "note_id")
    private Note note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private NoteType type;

}
