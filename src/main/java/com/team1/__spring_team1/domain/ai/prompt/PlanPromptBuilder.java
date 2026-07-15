package com.team1.__spring_team1.domain.ai.prompt;

import org.springframework.stereotype.Component;

/**
 * PLAN(1단계) 기획서 생성 prompt 빌더.
 * 회의자료 텍스트를 받아서 Gemini에 보낼 최종 prompt 문자열을 조립한다.
 */
@Component
public class PlanPromptBuilder {

    public String build(String sourceContent) {
        return """
                You are an expert product planner.
                Based on the following meeting notes, generate a planning document.
                
                [RULES]
                - Respond ONLY with a valid JSON object. Do not include any explanation or markdown.
                - All values must be written in Korean.
                - The JSON must strictly follow this structure:
                
                {
                  "problemDefinition": "string - the core problem this service aims to solve",
                  "targetUser": "string - the primary target user group",
                  "servicePurpose": "string - the main purpose of the service",
                  "coreValue": "string - the key value this service delivers"
                }
                
                [MEETING NOTES]
                %s
                """.formatted(sourceContent);
    }
}