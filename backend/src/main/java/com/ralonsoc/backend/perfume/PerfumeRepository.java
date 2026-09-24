package com.ralonsoc.backend.perfume;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface PerfumeRepository extends JpaRepository<Perfume, UUID> {

    Page<Perfume> findByGender(Gender gender, Pageable pageable);

    Page<Perfume> findByBrandId(UUID brandId, Pageable pageable);

    Page<Perfume> findByGenderAndBrandId(Gender gender, UUID brandId, Pageable pageable);

    @Query("SELECT p FROM Perfume p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Perfume> searchByName(String query, Pageable pageable);
}
