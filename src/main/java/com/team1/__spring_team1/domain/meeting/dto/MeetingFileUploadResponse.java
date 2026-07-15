package com.team1.__spring_team1.domain.meeting.dto;

import com.team1.__spring_team1.domain.meeting.entity.MeetingFileStatus;
import lombok.Getter;

@Getter
public class MeetingFileUploadResponse {

    private final Long fileId;
    private final String fileName;
    private final MeetingFileStatus status;
    private final String message;

    public MeetingFileUploadResponse(Long fileId, String fileName, MeetingFileStatus status) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.status = status;
        this.message = "file uploaded and transcribing started";
    }
}