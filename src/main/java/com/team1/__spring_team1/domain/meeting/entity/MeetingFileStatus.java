package com.team1.__spring_team1.domain.meeting.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

public enum MeetingFileStatus {
    PENDING,      // 업로드 완료, 처리 대기
    PROCESSING,   // STT 변환 중
    COMPLETED,    // 변환 완료
    FAILED        // 처리 실패
}