package com.team1.__spring_team1.domain.ai.prompt;

import org.springframework.stereotype.Component;

/**
 * SCREEN_SPEC(3단계) 화면별 기획서 생성 prompt 빌더.
 * 확정된 기능명세서 content를 받아서 Gemini에 보낼 최종 prompt 문자열을 조립한다.
 *
 * 각 화면은 confirm 시점에 Screen 엔티티로 추출되어 저장되며,
 * 회광님(F파트)이 와이어프레임 생성 시 spec_json을 참조한다.
 */
@Component
public class ScreenSpecPromptBuilder {

    public String build(String featureSpecContent) {
        return """
                You are an expert UX planner.
                Based on the following feature specification, generate a screen-by-screen planning document.
                
                [RULES]
                - Respond ONLY with a valid JSON object. Do not include any explanation or markdown.
                - All values must be written in Korean.
                - screenId must start from 1 and increment by 1.
                - components, inputs, buttons, navigation, exceptions must always be arrays (use empty array [] if none).
                - The JSON must strictly follow this structure:
                
                {
                  "screens": [
                    {
                      "screenId": number,
                      "name": "string - screen name",
                      "purpose": "string - what this screen is for",
                      "components": [
                        {
                          "id": "string - unique component id (kebab-case)",
                          "type": "string - component type (e.g. list, card, navbar)",
                          "name": "string - component name",
                          "description": "string - what this component does"
                        }
                      ],
                      "inputs": [
                        {
                          "id": "string - unique input id (kebab-case)",
                          "label": "string - input label",
                          "inputType": "string - input type (e.g. text, password, email)",
                          "placeholder": "string - placeholder text",
                          "required": true | false,
                          "validation": "string - validation rule or null"
                        }
                      ],
                      "buttons": [
                        {
                          "id": "string - unique button id (kebab-case)",
                          "label": "string - button label",
                          "action": "string - what happens when clicked",
                          "role": "string - button role (primary | secondary | danger)"
                        }
                      ],
                      "navigation": [
                        {
                          "triggerId": "string - button or component id that triggers navigation",
                          "targetScreenId": number,
                          "targetScreenName": "string - target screen name",
                          "condition": "string - condition for navigation"
                        }
                      ],
                      "exceptions": [
                        {
                          "type": "string - exception type (e.g. EMPTY_STATE, ERROR, LOADING)",
                          "condition": "string - when this exception occurs",
                          "message": "string - message shown to user",
                          "handling": "string - how to handle this exception"
                        }
                      ]
                    }
                  ]
                }
                
                [FEATURE SPECIFICATION]
                %s
                """.formatted(featureSpecContent);
    }
}