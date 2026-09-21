package com.ralonsoc.backend.seed;


import com.ralonsoc.backend.perfume.*;
import com.ralonsoc.backend.perfume.PerfumeNote;
import com.ralonsoc.backend.perfume.PerfumeAccord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    private final Map<String, UUID> brandIds = new HashMap<>();
    private final Map<String, UUID> noteIds = new HashMap<>();
    private final Map<String, UUID> accordIds = new HashMap<>();
    private final Set<String> seenPerfumes = new HashSet<>();

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM perfumes", Integer.class);

        if (!seedEnabled || (count != null && count > 0)) {
            log.info("Seed skipped — {} perfumes already exist in database.", count);
            return;
        }

        StopWatch stopWatch = new StopWatch("DataSeeder");
        stopWatch.start();
        log.info("▶ Starting perfume catalog import...");

        List<Object[]> perfumeNoteBatch = new ArrayList<>();
        List<Object[]> perfumeAccordBatch = new ArrayList<>();
        int imported = 0;
        int skippedDuplicates = 0;

        try (Reader reader = new InputStreamReader(
                new ClassPathResource("db/seed/perfumes.csv").getInputStream(),
                StandardCharsets.ISO_8859_1)) {

            CSVParser parser = CSVFormat.DEFAULT.builder()
                    .setDelimiter(';')
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader);

            for (CSVRecord record : parser) {
                String perfumeSlug = record.get("Perfume");
                String brandSlug = record.get("Brand");
                String dedupeKey = brandSlug + "|" + perfumeSlug;

                if (!seenPerfumes.add(dedupeKey)) {
                    skippedDuplicates++;
                    continue;
                }

                UUID brandId = getOrCreateBrand(brandSlug, record.get("Country"));
                UUID perfumeId = insertPerfume(record, perfumeSlug, brandId);

                Set<UUID> seenNoteForThisPerfume = new HashSet<>();
                collectNotes(record, "Top", NoteType.TOP, perfumeId, perfumeNoteBatch, seenNoteForThisPerfume);
                collectNotes(record, "Middle", NoteType.MIDDLE, perfumeId, perfumeNoteBatch, seenNoteForThisPerfume);
                collectNotes(record, "Base", NoteType.BASE, perfumeId, perfumeNoteBatch, seenNoteForThisPerfume);

                Set<UUID> seenAccordsForThisPerfume = new HashSet<>();
                collectAccords(record, perfumeId, perfumeAccordBatch, seenAccordsForThisPerfume);

                imported++;
                if (imported % 5000 == 0) {
                    log.info("  → {} processed perfumes...", imported);
                }
            }
        }

        stopWatch.stop();
        log.info("""
            ✔ Seed completed in {} s
              ├─ Imported perfumes : {}
              ├─ Skipped duplicates : {}
              ├─ Brands              : {}
              ├─ Notes               : {}
              ├─ Accords             : {}
              ├─ Related notes     : {}
              └─ Related accords   : {}""",
                String.format("%.2f", stopWatch.getTotalTimeSeconds()),
                imported, skippedDuplicates, brandIds.size(), noteIds.size(), accordIds.size(),
                perfumeNoteBatch.size(), perfumeAccordBatch.size());

        insertPerfumeNotesBatch(perfumeNoteBatch);
        insertPerfumeAccordsBatch(perfumeAccordBatch);
    }
    private UUID getOrCreateBrand(String slug, String country) {
        return brandIds.computeIfAbsent(slug, k -> {
            UUID id = UUID.randomUUID();
            Timestamp now = Timestamp.from(Instant.now());
            jdbcTemplate.update(
                    "INSERT INTO brands (id, name, country, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                    id, slugToName(slug), country, now, now
            );
            return id;
        });
    }

    private UUID getOrCreateNote(String rawName) {
        String name = rawName.trim().toLowerCase();
        return noteIds.computeIfAbsent(name, k -> {
            UUID id = UUID.randomUUID();
            jdbcTemplate.update(
                    "INSERT INTO notes (id, name) VALUES (?, ?)",
                    id, capitalize(name)
            );
            return id;
        });
    }

    private UUID getOrCreateAccord(String rawName) {
        String name = rawName.trim().toLowerCase();
        return accordIds.computeIfAbsent(name, k -> {
            UUID id = UUID.randomUUID();
            jdbcTemplate.update(
                    "INSERT INTO accords (id, name) VALUES (?, ?)",
                    id, capitalize(name)
            );
            return id;
        });
    }

    private UUID insertPerfume(CSVRecord record, String slug, UUID brandId) {
        UUID id = UUID.randomUUID();
        Timestamp now = Timestamp.from(Instant.now());

        String genderRaw = record.get("Gender");
        Gender gender = switch (genderRaw) {
            case "men" -> Gender.MALE;
            case "women" -> Gender.FEMALE;
            default -> Gender.UNISEX;
        };

        Integer year = parseYear(record.get("Year"));

        jdbcTemplate.update(
                "INSERT INTO perfumes (id, name, brand_id, gender, year, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                id, slugToName(slug), brandId, gender.name(), year, now, now
        );
        return id;
    }

    private void collectNotes(CSVRecord record, String column, NoteType type,
                              UUID perfumeId, List<Object[]> batch, Set<UUID> seenForThisPerfume) {
        String raw = record.get(column);
        if (raw == null || raw.isBlank() || raw.equalsIgnoreCase("unknown")) {
            return;
        }
        for (String noteName : raw.split(",")) {
            if (noteName.isBlank()) continue;
            UUID noteId = getOrCreateNote(noteName);
            if (seenForThisPerfume.add(noteId)) {
                batch.add(new Object[]{perfumeId, noteId, type.name()});
            }
        }
    }

    private void collectAccords(CSVRecord record, UUID perfumeId, List<Object[]> batch, Set<UUID> seenForThisPerfume) {
        String[] columns = {"mainaccord1", "mainaccord2", "mainaccord3", "mainaccord4", "mainaccord5"};
        for (int i = 0; i < columns.length; i++) {
            String raw = record.get(columns[i]);
            if (raw == null || raw.isBlank()) continue;
            UUID accordId = getOrCreateAccord(raw);
            if (seenForThisPerfume.add(accordId)) {
                batch.add(new Object[]{perfumeId, accordId, i + 1});
            }
        }
    }

    private void insertPerfumeNotesBatch(List<Object[]> batch) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO perfume_notes (perfume_id, note_id, type) VALUES (?, ?, ?)",
                batch
        );
    }

    private void insertPerfumeAccordsBatch(List<Object[]> batch) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO perfume_accords (perfume_id, accord_id, position) VALUES (?, ?, ?)",
                batch
        );
    }

    private Integer parseYear(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return (int) Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String slugToName(String slug) {
        String[] words = slug.replace('-', ' ').split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            sb.append(Character.toUpperCase(w.charAt(0)))
                    .append(w.substring(1))
                    .append(' ');
        }
        return sb.toString().trim();
    }

    private String capitalize(String s) {
        if (s.isBlank()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
