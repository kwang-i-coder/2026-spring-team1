package com.team1.__spring_team1.domain.ai.prompt;

import org.springframework.stereotype.Component;

/**
 * WIREFRAME(4단계) 와이어프레임 생성 prompt 빌더.
 * SCREEN_SPEC 확정 시 추출된 Screen 엔티티의 spec_json을 받아서
 * 화면 한 개 분량의 와이어프레임 DSL을 생성한다.
 *
 * 앞 단계들과 달리 화면 단위로 호출되며, 화면끼리는 서로 의존하지 않는다.
 */
@Component
public class WireframePromptBuilder {

    private static final int SCREEN_WIDTH = 375;
    private static final int SCREEN_HEIGHT = 812;

    public String build(String screenSpecJson) {
        return """
                You are an expert UI wireframe designer.
                Based on the following screen specification, generate a low-fidelity wireframe layout.

                [RULES]
                - Respond ONLY with a valid JSON object. Do not include any explanation or markdown.
                - All "text" values must be written in Korean.
                - The canvas is fixed: width must be %d, height must be %d.
                - Every element must fit inside the canvas: x >= 0, y >= 0, x + w <= %d, y + h <= %d.
                - w and h must be greater than 0.
                - Each element id must be unique within the screen.
                - Reuse the component/input/button ids from the screen specification when they exist,
                  so that the wireframe can be traced back to the spec.
                - Lay elements out in a natural top-to-bottom reading order without overlapping.
                - "type" of each element must be one of:
                  navbar | header | text | input | button | list | card | image | icon | divider | tab | modal
                - The JSON must strictly follow this structure:

                {
                  "type": "screen",
                  "width": %d,
                  "height": %d,
                  "elements": [
                    {
                      "id": "string - unique element id (kebab-case)",
                      "type": "string - element type from the list above",
                      "text": "string - label shown to the user (empty string if none)",
                      "x": number,
                      "y": number,
                      "w": number,
                      "h": number
                    }
                  ]
                }

                [SCREEN SPECIFICATION]
                %s
                """.formatted(
                SCREEN_WIDTH, SCREEN_HEIGHT,
                SCREEN_WIDTH, SCREEN_HEIGHT,
                SCREEN_WIDTH, SCREEN_HEIGHT,
                screenSpecJson
        );
    }
}