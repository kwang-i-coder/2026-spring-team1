package com.team1.__spring_team1.domain.meeting.dto;

import lombok.Getter;

@Getter
public class MeetingNoteCreateResponse {

    private final Long meetingNoteId;
    private final String title;
    private final String message;

    public MeetingNoteCreateResponse(Long meetingNoteId, String title) {
        this.meetingNoteId = meetingNoteId;
        this.title = title;
        this.message = "meeting note saved";
    }
}