package com.ralonsoc.backend.perfume;

import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class PerfumeSpecifications {

    private PerfumeSpecifications() {
    }

    public static Specification<Perfume> visible() {
        return (root, query, cb) -> cb.isNull(root.get("hiddenAt"));
    }

    public static Specification<Perfume> hasGender(Gender gender) {
        return (root, query, cb) -> gender == null ? null : cb.equal(root.get("gender"), gender);
    }

    public static Specification<Perfume> hasBrand(UUID brandId) {
        return (root, query, cb) -> brandId == null ? null : cb.equal(root.get("brand").get("id"), brandId);
    }

    public static Specification<Perfume> nameContains(String search) {
        return (root, query, cb) -> search == null || search.isBlank() ? null
                : cb.like(cb.lower(root.get("name")), "%" + search.trim().toLowerCase() + "%");
    }

    /** Public catalog: visibility is always applied, the other filters are optional and combinable. */
    public static Specification<Perfume> publicCatalog(Gender gender, UUID brandId, String search) {
        return Specification.where(visible())
                .and(hasGender(gender))
                .and(hasBrand(brandId))
                .and(nameContains(search));
    }
}
