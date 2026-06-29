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

    // USER
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "이메일 형식이 올바르지 않습니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재 하지 않는 사용자입니다"),

    // WORKSPACE
    WORKSPACE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재 하지 않는 워크스페이스입니다"),
    WORKSPACE_LEADER_ONLY(HttpStatus.FORBIDDEN, "팀 리더만 수행 할 수 있습니다"),

    // PROJECT
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재 하지 않는 프로젝트입니다"),

    // STAGE
    STAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재 하지 않는 단계입니다"),
    STAGE_STEP_ORDER_INVALID(HttpStatus.BAD_REQUEST, "이전 단계를 먼저 완료해야 합니다"),
    STAGE_COMPLETED(HttpStatus.CONFLICT, "이미 완료된 단계입니다");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
