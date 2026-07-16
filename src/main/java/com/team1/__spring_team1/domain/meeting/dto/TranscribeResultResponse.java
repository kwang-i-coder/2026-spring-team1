package com.team1.__spring_team1.domain.meeting.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// Transcribe -> S3에 저장되는 json 파싱용 DTO
@JsonIgnoreProperties(ignoreUnknown = true)
public record TranscribeResultResponse(
        Results results
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Results(
            List<Transcript> transcripts
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Transcript(
            String transcript
    ) {
    }

    public String extractTranscript() {
        if (results == null || results.transcripts() == null || results.transcripts().isEmpty()) {
            return "";
        }
        return results.transcripts().get(0).transcript();
    }
}