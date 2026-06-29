package com.team1.__spring_team1.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // AUTH
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다"),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다"),
    SESSION_NOT_FOUND(HttpStatus.UNAUTHORIZED, "유효하지 않은 세션입니다"),
    SESSION_EXPIRED(HttpStatus.UNAUTHORIZED, "세션이 만료 되었습니다"),

    // USER
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "이메일 형식이 올바르지 않습니다"),
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재 하지 않는 사용자입니다"),

    // PROJECT
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재 하지 않는 프로젝트입니다"),
    NOT_PROJECT_MEMBER(HttpStatus.FORBIDDEN, "프로젝트 멤버가 아닙니다"),
    PROJECT_LEADER_ONLY(HttpStatus.FORBIDDEN, "프로젝트 리더만 할 수 있습니다"),
    ALREADY_PROJECT_MEMBER(HttpStatus.CONFLICT, "이미 참여한 프로젝트입니다"),

    // INVITE
    INVITE_NOT_FOUND(HttpStatus.NOT_FOUND, "유효하지 않은 초대 링크입니다"),
    INVITE_EXPIRED(HttpStatus.GONE, "만료된 초대 링크입니다"),

    // MEETING
    MEETING_NOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회의록입니다"),
    MEETING_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회의 파일입니다"),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다"),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다"),
    STT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "STT 변환에 실패했습니다"),

    // STAGE
    STAGE_DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재 하지 않는 문서입니다"),
    STAGE_STEP_ORDER_INVALID(HttpStatus.BAD_REQUEST, "이전 단계를 먼저 완료해야 합니다"),
    STAGE_COMPLETED(HttpStatus.CONFLICT, "이미 완료된 단계입니다"),
    STAGE_TYPE_INVALID(HttpStatus.BAD_REQUEST, "올바르지 않은 단계 유형입니다."),

    // AI
    AI_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 생성에 실패했습니다"),
    AI_RESPONSE_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "AI 응답 형식이 올바르지 않습니다"),

    // SCREEN
    SCREEN_NOT_FOUND(HttpStatus.NOT_FOUND, "존재 하지 않는 화면입니다"),

    // WIREFRAME
    WIREFRAME_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 와이어프레임입니다."),
    WIREFRAME_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "와이어프레임 생성에 실패했습니다."),

    // WIREFRAME REQUEST
    REGENERATION_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 재생성 요청입니다."),
    REGENERATION_REQUEST_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 대기 중인 재생성 요청이 있습니다."),
    REGENERATION_REQUEST_ALREADY_HANDLED(HttpStatus.CONFLICT, "이미 처리된 재생성 요청입니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
