package com.ralonsoc.backend.perfume;

import com.ralonsoc.backend.perfume.dto.BrandResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandRepository brandRepository;

    @GetMapping
    public Page<BrandResponse> listBrands(@RequestParam(required = false) String search, Pageable pageable) {
        Page<Brand> brands = (search != null && !search.isBlank()) ?  brandRepository.searchByName(search, pageable) : brandRepository.findAll(pageable);
        return brands.map(b -> new BrandResponse(b.getId(),b.getName(), b.getCountry()));
    }
}
