package com.ralonsoc.backend.perfume;
import com.ralonsoc.backend.perfume.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PerfumeService {

    private final PerfumeRepository perfumeRepository;

    public Page<PerfumeSummaryResponse> listPerfumes(Gender gender, UUID brandId, Pageable pageable) {
        Page<Perfume> perfumes;
        if (gender != null && brandId != null) {
            perfumes = perfumeRepository.findByGenderAndBrandId(gender, brandId, pageable);
        } else if (gender != null) {
            perfumes = perfumeRepository.findByGender(gender, pageable);
        } else if (brandId != null) {
            perfumes = perfumeRepository.findByBrandId(brandId, pageable);
        } else {
            perfumes = perfumeRepository.findAll(pageable);
        }

        return perfumes.map(this::toSummaryResponse);
    }

    public Page<PerfumeSummaryResponse> searchPerfumes(String query, Pageable pageable) {
        return perfumeRepository.searchByName(query, pageable)
                .map(this::toSummaryResponse);
    }

    public PerfumeResponse getPerfumeById(UUID id) {
        Perfume perfume = perfumeRepository.findById(id)
                .orElseThrow(() -> new PerfumeNotFoundException(id));
        return toFullResponse(perfume);
    }

    private PerfumeSummaryResponse toSummaryResponse(Perfume perfume) {
        List<String> mainAccords = perfume.getAccords().stream()
                .sorted((a, b) -> a.getPosition().compareTo(b.getPosition()))
                .limit(3)
                .map(pa -> pa.getAccord().getName())
                .collect(Collectors.toList());

        return new PerfumeSummaryResponse(
                perfume.getId(),
                perfume.getName(),
                perfume.getBrand().getName(),
                perfume.getGender(),
                perfume.getYear(),
                mainAccords
        );
    }

    private PerfumeResponse toFullResponse(Perfume perfume) {
        BrandResponse brand = new BrandResponse(
                perfume.getBrand().getId(),
                perfume.getBrand().getName(),
                perfume.getBrand().getCountry()
        );

        PerfumeNotesResponse notes = new PerfumeNotesResponse(
                filterNotesByType(perfume, NoteType.TOP),
                filterNotesByType(perfume, NoteType.MIDDLE),
                filterNotesByType(perfume, NoteType.BASE)
        );

        List<AccordResponse> accords = perfume.getAccords().stream()
                .sorted((a, b) -> a.getPosition().compareTo(b.getPosition()))
                .map(pa -> new AccordResponse(
                        pa.getAccord().getId(),
                        pa.getAccord().getName(),
                        pa.getPosition()
                ))
                .collect(Collectors.toList());

        return new PerfumeResponse(
                perfume.getId(),
                perfume.getName(),
                brand,
                perfume.getGender(),
                perfume.getYear(),
                notes,
                accords
        );
    }

    private List<NoteResponse> filterNotesByType(Perfume perfume, NoteType type) {
        return perfume.getNotes().stream()
                .filter(pn -> pn.getType() == type)
                .map(pn -> new NoteResponse(pn.getNote().getId(), pn.getNote().getName()))
                .collect(Collectors.toList());
    }
}