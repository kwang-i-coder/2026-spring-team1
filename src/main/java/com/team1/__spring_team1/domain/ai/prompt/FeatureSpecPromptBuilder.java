package com.team1.__spring_team1.domain.ai.prompt;

import org.springframework.stereotype.Component;

/**
 * FEATURE_SPEC(2단계) 기능명세서 생성 prompt 빌더.
 * 확정된 기획서 content를 받아서 Gemini에 보낼 최종 prompt 문자열을 조립한다.
 */
@Component
public class FeatureSpecPromptBuilder {

    public String build(String planContent) {
        return """
                You are an expert product manager.
                Based on the following planning document, generate a feature specification.
                
                [RULES]
                - Respond ONLY with a valid JSON object. Do not include any explanation or markdown.
                - All values must be written in Korean.
                - priority must be one of: HIGH, MEDIUM, LOW
                - includedInMvp must be a boolean (true or false)
                - The JSON must strictly follow this structure:
                
                {
                  "features": [
                    {
                      "name": "string - feature name",
                      "description": "string - detailed description of the feature",
                      "priority": "HIGH | MEDIUM | LOW",
                      "includedInMvp": true | false
                    }
                  ]
                }
                
                [PLANNING DOCUMENT]
                %s
                """.formatted(planContent);
    }
}