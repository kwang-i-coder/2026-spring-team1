package com.team1.__spring_team1.domain.meeting.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

public enum MeetingFileStatus {
    TRANSCRIBING,  // 업로드 성공 → 바로 이 상태로 시작 (API 응답에서 확인됨)
    COMPLETED,     // STT 변환 완료
    FAILED         // 처리 실패
}