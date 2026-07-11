package com.team1.__spring_team1.domain.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SCREEN_SPEC(3단계) AI 응답 content 구조.
 * Gemini가 반환하는 JSON을 역직렬화하는 DTO.
 *
 * confirm 시점에 screens 배열을 파싱해서
 * Screen 엔티티로 변환 후 screens 테이블에 저장된다.
 */

@Getter
@NoArgsConstructor
public class ScreenSpecContent {

    private List<ScreenItem> screens;

    @Getter
    @NoArgsConstructor
    public static class ScreenItem {
        private Integer screenId;
        private String name;
        private String purpose;
        private List<Component> componenets;
        private List<Input> inputs;
        private List<Button> buttons;
        private List<Navigation> navigation;
        private List<ExceptionItem> exceptions;
    }

    @Getter
    @NoArgsConstructor
    public static class Component {
        private String id;
        private String type;
        private String name;
        private String description;
    }

    @Getter
    @NoArgsConstructor
    public static class Input {
        private String id;
        private String label;
        private String inputType;
        private String placeholder;
        private boolean required;
        private String validation;
    }

    @Getter
    @NoArgsConstructor
    public static class Button {
        private String id;
        private String label;
        private String action;
        private String role;
    }

    @Getter
    @NoArgsConstructor
    public static class Navigation {
        private String triggerId;
        private String targetScreenId;
        private String targetScreenName;
        private String condition;
    }

    @Getter
    @NoArgsConstructor
    public static class ExceptionItem {
        private String type;
        private String condition;
        private String message;
        private String handling;
    }
}
