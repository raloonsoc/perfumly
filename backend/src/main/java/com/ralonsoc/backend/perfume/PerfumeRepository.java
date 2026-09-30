package com.ralonsoc.backend.perfume;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface PerfumeRepository extends JpaRepository<Perfume, UUID>, JpaSpecificationExecutor<Perfume> {
}
