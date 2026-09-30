package com.ralonsoc.backend;

import com.ralonsoc.backend.perfume.Accord;
import com.ralonsoc.backend.perfume.Brand;
import com.ralonsoc.backend.perfume.Gender;
import com.ralonsoc.backend.perfume.Note;
import com.ralonsoc.backend.perfume.NoteType;
import com.ralonsoc.backend.perfume.Perfume;
import com.ralonsoc.backend.perfume.PerfumeAccord;
import com.ralonsoc.backend.perfume.PerfumeNote;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Catalog fixtures for tests (RNF-8). Brand, notes and accords are reused by name
 * (UNIQUE columns); each {@code build()} persists in its own transaction.
 */
@Component
public class CatalogFixtures {

    @PersistenceContext private EntityManager em;
    private final TransactionTemplate tx;

    CatalogFixtures(TransactionTemplate tx) {
        this.tx = tx;
    }

    public PerfumeBuilder perfume(String name) {
        return new PerfumeBuilder(name);
    }

    public class PerfumeBuilder {
        private final String name;
        private String brandName = "Test Brand";
        private String brandCountry = "Testland";
        private Gender gender = Gender.UNISEX;
        private Integer year;
        private boolean hidden;
        private final Map<String, NoteType> notes = new LinkedHashMap<>();
        private final Map<String, Integer> accords = new LinkedHashMap<>();

        private PerfumeBuilder(String name) {
            this.name = name;
        }

        public PerfumeBuilder brand(String name, String country) {
            this.brandName = name;
            this.brandCountry = country;
            return this;
        }

        public PerfumeBuilder gender(Gender gender) {
            this.gender = gender;
            return this;
        }

        public PerfumeBuilder year(Integer year) {
            this.year = year;
            return this;
        }

        /** Persists the perfume hidden ({@code hidden_at} set). */
        public PerfumeBuilder hidden() {
            this.hidden = true;
            return this;
        }

        public PerfumeBuilder topNote(String name) {
            notes.put(name, NoteType.TOP);
            return this;
        }

        public PerfumeBuilder middleNote(String name) {
            notes.put(name, NoteType.MIDDLE);
            return this;
        }

        public PerfumeBuilder baseNote(String name) {
            notes.put(name, NoteType.BASE);
            return this;
        }

        /** Accord with an explicit {@code position}. */
        public PerfumeBuilder accord(String name, int position) {
            accords.put(name, position);
            return this;
        }

        /** Accords in relevance order: positions 1..n. */
        public PerfumeBuilder accords(String... names) {
            for (int i = 0; i < names.length; i++) {
                accords.put(names[i], i + 1);
            }
            return this;
        }

        public Perfume build() {
            return tx.execute(status -> {
                Perfume perfume = new Perfume();
                perfume.setName(name);
                perfume.setBrand(findOrCreateBrand());
                perfume.setGender(gender);
                perfume.setYear(year);
                if (hidden) perfume.setHiddenAt(java.time.Instant.now());
                em.persist(perfume);

                List<PerfumeNote> perfumeNotes = new ArrayList<>();
                notes.forEach((noteName, type) -> {
                    PerfumeNote pn = new PerfumeNote();
                    pn.setPerfume(perfume);
                    pn.setNote(findOrCreate(Note.class, noteName));
                    pn.setType(type);
                    em.persist(pn);
                    perfumeNotes.add(pn);
                });
                List<PerfumeAccord> perfumeAccords = new ArrayList<>();
                accords.forEach((accordName, position) -> {
                    PerfumeAccord pa = new PerfumeAccord();
                    pa.setPerfume(perfume);
                    pa.setAccord(findOrCreate(Accord.class, accordName));
                    pa.setPosition(position);
                    em.persist(pa);
                    perfumeAccords.add(pa);
                });
                perfume.setNotes(perfumeNotes);
                perfume.setAccords(perfumeAccords);
                return perfume;
            });
        }

        private Brand findOrCreateBrand() {
            return em.createQuery("select b from Brand b where b.name = :n", Brand.class)
                    .setParameter("n", brandName).getResultStream().findFirst()
                    .orElseGet(() -> {
                        Brand b = new Brand();
                        b.setName(brandName);
                        b.setCountry(brandCountry);
                        em.persist(b);
                        return b;
                    });
        }

        @SuppressWarnings("unchecked")
        private <T> T findOrCreate(Class<T> type, String entityName) {
            return em.createQuery("select e from " + type.getSimpleName() + " e where e.name = :n", type)
                    .setParameter("n", entityName).getResultStream().findFirst()
                    .orElseGet(() -> {
                        try {
                            T e = type.getDeclaredConstructor().newInstance();
                            type.getMethod("setName", String.class).invoke(e, entityName);
                            em.persist(e);
                            return e;
                        } catch (ReflectiveOperationException ex) {
                            throw new IllegalStateException(ex);
                        }
                    });
        }
    }
}
