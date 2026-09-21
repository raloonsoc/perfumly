package com.ralonsoc.backend.perfume.dto;

import java.util.List;

public record PerfumeNotesResponse(List<NoteResponse> top, List<NoteResponse> middle, List<NoteResponse> base) {
}
