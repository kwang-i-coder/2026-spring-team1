package com.team1.__spring_team1.domain.ai.client;

import com.team1.__spring_team1.domain.stage.entity.StageType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 로컬/테스트 환경용 Mock AI 클라이언트.
 * 실제 Gemini API를 호출하지 않고 고정된 JSON 문자열을 반환한다.
 * 다른 팀원들이 Gemini API 키 없이도 연동 작업을 진행할 수 있도록 한다.
 *
 * spring.profiles.default: local로 설정되어 있어서 별도로 spring.profiles.active 안 설정해도 @Profile("local")이 동작
 */
@Component
@Profile({"local", "test"})
public class MockAiDocumentClient implements AiDocumentClient {

    @Override
    public String generate(String prompt, StageType stageType) {
        return switch (stageType) {
            case PLAN -> """
                    {
                      "problemDefinition": "해커톤 팀은 짧은 시간 안에 기획과 개발을 동시에 진행해야 한다.",
                      "targetUser": "해커톤 참가자 및 단기 프로젝트 팀",
                      "servicePurpose": "회의록을 기반으로 개발 가능한 기획 산출물을 빠르게 생성한다.",
                      "coreValue": "회의 내용을 화면별 spec과 와이어프레임으로 연결한다."
                    }
                    """;
            case FEATURE_SPEC -> """
                    {
                      "features": [
                        {
                          "name": "회의록 업로드",
                          "description": "회의록 텍스트 또는 녹음 파일을 업로드한다.",
                          "priority": "HIGH",
                          "includedInMvp": true
                        },
                        {
                          "name": "AI 기획서 생성",
                          "description": "업로드된 회의록을 기반으로 기획서 초안을 자동 생성한다.",
                          "priority": "HIGH",
                          "includedInMvp": true
                        }
                      ]
                    }
                    """;
            case SCREEN_SPEC -> """
                    {
                      "screens": [
                        {
                          "screenId": 1,
                          "name": "프로젝트 목록 화면",
                          "purpose": "사용자가 참여 중인 프로젝트를 확인하고 새 프로젝트를 생성하는 화면",
                          "components": [
                            {
                              "id": "project-list",
                              "type": "list",
                              "name": "프로젝트 목록",
                              "description": "참여 중인 프로젝트를 카드 형태로 표시한다"
                            }
                          ],
                          "inputs": [
                            {
                              "id": "project-search-input",
                              "label": "프로젝트 검색",
                              "inputType": "text",
                              "placeholder": "프로젝트명을 입력하세요",
                              "required": false,
                              "validation": "최대 50자"
                            }
                          ],
                          "buttons": [
                            {
                              "id": "create-project-button",
                              "label": "새 프로젝트",
                              "action": "프로젝트 생성 화면으로 이동",
                              "role": "primary"
                            }
                          ],
                          "navigation": [
                            {
                              "triggerId": "create-project-button",
                              "targetScreenId": 2,
                              "targetScreenName": "프로젝트 생성 화면",
                              "condition": "사용자가 새 프로젝트 버튼을 클릭했을 때"
                            }
                          ],
                          "exceptions": [
                            {
                              "type": "EMPTY_STATE",
                              "condition": "참여 중인 프로젝트가 없을 때",
                              "message": "참여 중인 프로젝트가 없습니다.",
                              "handling": "빈 화면 안내와 새 프로젝트 버튼을 표시한다"
                            }
                          ]
                        }
                      ]
                    }
                    """;
        };
    }
}