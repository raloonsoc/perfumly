package com.ralonsoc.backend.perfume;


import com.ralonsoc.backend.perfume.dto.PerfumeResponse;
import com.ralonsoc.backend.perfume.dto.PerfumeSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/perfumes")
@RequiredArgsConstructor
public class PerfumeController {
    private final PerfumeService perfumeService;

    @GetMapping
    public Page<PerfumeSummaryResponse> listPerfumes(
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) UUID brandId,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return perfumeService.listPerfumes(gender, brandId, search, pageable);
    }

    @GetMapping("/{id}")
    public PerfumeResponse getPerfume(@PathVariable UUID id) {
        return perfumeService.getPerfumeById(id);
    }
}
