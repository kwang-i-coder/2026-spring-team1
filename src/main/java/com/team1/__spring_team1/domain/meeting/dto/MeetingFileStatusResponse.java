package com.team1.__spring_team1.domain.meeting.dto;

import com.team1.__spring_team1.domain.meeting.entity.MeetingFileStatus;
import lombok.Getter;

@Getter
public class MeetingFileStatusResponse{

    private final Long fileId;
    private final MeetingFileStatus status;
    private final String transcript;

    public MeetingFileStatusResponse(Long fileId, MeetingFileStatus status, String transcript) {
        this.fileId = fileId;
        this.status = status;
        this.transcript = transcript;
    }
}