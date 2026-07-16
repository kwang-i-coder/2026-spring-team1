package com.team1.__spring_team1.domain.wireframe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.__spring_team1.domain.ai.dto.WireframeContent;
import com.team1.__spring_team1.domain.ai.service.AiDocumentService;
import com.team1.__spring_team1.domain.project.service.ProjectPermissionService;
import com.team1.__spring_team1.domain.stage.entity.Screen;
import com.team1.__spring_team1.domain.stage.repository.ScreenRepository;
import com.team1.__spring_team1.domain.stage.service.StageService;
import com.team1.__spring_team1.domain.wireframe.dto.response.WireframeDslResponse;
import com.team1.__spring_team1.domain.wireframe.entity.Wireframe;
import com.team1.__spring_team1.domain.wireframe.repository.WireframeRepository;
import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WireframeServiceTest {

    @Mock
    private WireframeRepository wireframeRepository;

    @Mock
    private ScreenRepository screenRepository;

    @Mock
    private StageService stageService;

    @Mock
    private ProjectPermissionService
            projectPermissionService;

    @Mock
    private AiDocumentService aiDocumentService;

    @Mock
    private Screen screen;

    @Mock
    private WireframeContent generatedContent;

    @Mock
    private WireframeContent.Element generatedElement;

    private WireframeService wireframeService;

    @BeforeEach
    void setUp() {
        wireframeService = new WireframeService(
                wireframeRepository,
                screenRepository,
                stageService,
                projectPermissionService,
                aiDocumentService,
                new ObjectMapper()
        );
    }

    @Test
    @DisplayName(
            "재생성된 JSON DSL로 기존 와이어프레임을 교체하고 버전을 증가시킨다"
    )
    void regenerateWireframe() {
        // given
        Long projectId = 1L;
        Long screenId = 10L;

        String screenSpecJson =
                "{\"name\":\"로그인 화면\"}";

        String reason =
                "로그인 버튼을 화면 하단에 배치해주세요.";

        String oldJsonDsl =
                "{\"type\":\"screen\",\"width\":375,"
                        + "\"height\":812,\"elements\":[]}";

        Wireframe wireframe =
                new Wireframe(
                        projectId,
                        screenId,
                        oldJsonDsl
                );

        when(screenRepository.findById(screenId))
                .thenReturn(Optional.of(screen));

        when(screen.getProjectId())
                .thenReturn(projectId);

        when(screen.getSpecJson())
                .thenReturn(screenSpecJson);

        when(wireframeRepository.findByScreenId(screenId))
                .thenReturn(Optional.of(wireframe));

        mockGeneratedContent();

        when(aiDocumentService.regenerateWireframe(
                screenSpecJson,
                reason
        )).thenReturn(generatedContent);

        // when
        WireframeDslResponse response =
                wireframeService.regenerateWireframe(
                        projectId,
                        screenId,
                        reason
                );

        // then
        assertThat(response.type())
                .isEqualTo("screen");

        assertThat(response.width())
                .isEqualTo(375);

        assertThat(response.height())
                .isEqualTo(812);

        assertThat(response.elements())
                .hasSize(1);

        assertThat(response.elements().getFirst().id())
                .isEqualTo("login-button");

        assertThat(wireframe.getVersion())
                .isEqualTo(2);

        assertThat(wireframe.getJsonDsl())
                .isNotEqualTo(oldJsonDsl);

        assertThat(wireframe.getJsonDsl())
                .contains("\"id\":\"login-button\"");

        verify(aiDocumentService)
                .regenerateWireframe(
                        screenSpecJson,
                        reason
                );
    }

    @Test
    @DisplayName(
            "존재하지 않는 화면은 재생성할 수 없다"
    )
    void screenNotFound() {
        // given
        Long projectId = 1L;
        Long screenId = 999L;

        when(screenRepository.findById(screenId))
                .thenReturn(Optional.empty());

        // when
        BusinessException exception =
                catchThrowableOfType(
                        () -> wireframeService
                                .regenerateWireframe(
                                        projectId,
                                        screenId,
                                        "수정 요청"
                                ),
                        BusinessException.class
                );

        // then
        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.SCREEN_NOT_FOUND);

        verifyNoInteractions(
                wireframeRepository,
                aiDocumentService
        );
    }

    @Test
    @DisplayName(
            "요청 프로젝트와 화면의 프로젝트가 다르면 재생성할 수 없다"
    )
    void screenProjectMismatch() {
        // given
        Long requestProjectId = 1L;
        Long screenId = 10L;

        when(screenRepository.findById(screenId))
                .thenReturn(Optional.of(screen));

        when(screen.getProjectId())
                .thenReturn(2L);

        // when
        BusinessException exception =
                catchThrowableOfType(
                        () -> wireframeService
                                .regenerateWireframe(
                                        requestProjectId,
                                        screenId,
                                        "수정 요청"
                                ),
                        BusinessException.class
                );

        // then
        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.SCREEN_NOT_FOUND);

        verifyNoInteractions(
                wireframeRepository,
                aiDocumentService
        );
    }

    @Test
    @DisplayName(
            "기존 와이어프레임이 없으면 재생성할 수 없다"
    )
    void wireframeNotFound() {
        // given
        Long projectId = 1L;
        Long screenId = 10L;

        when(screenRepository.findById(screenId))
                .thenReturn(Optional.of(screen));

        when(screen.getProjectId())
                .thenReturn(projectId);

        when(wireframeRepository.findByScreenId(screenId))
                .thenReturn(Optional.empty());

        // when
        BusinessException exception =
                catchThrowableOfType(
                        () -> wireframeService
                                .regenerateWireframe(
                                        projectId,
                                        screenId,
                                        "수정 요청"
                                ),
                        BusinessException.class
                );

        // then
        assertThat(exception.getErrorCode())
                .isEqualTo(
                        ErrorCode.WIREFRAME_NOT_FOUND
                );

        verifyNoInteractions(aiDocumentService);
    }

    @Test
    @DisplayName(
            "AI 재생성 실패 시 기존 와이어프레임은 변경되지 않는다"
    )
    void aiFailureDoesNotChangeWireframe() {
        // given
        Long projectId = 1L;
        Long screenId = 10L;

        String screenSpecJson =
                "{\"name\":\"로그인 화면\"}";

        String reason =
                "버튼 위치를 변경해주세요.";

        String oldJsonDsl =
                "{\"type\":\"screen\",\"width\":375,"
                        + "\"height\":812,\"elements\":[]}";

        Wireframe wireframe =
                new Wireframe(
                        projectId,
                        screenId,
                        oldJsonDsl
                );

        when(screenRepository.findById(screenId))
                .thenReturn(Optional.of(screen));

        when(screen.getProjectId())
                .thenReturn(projectId);

        when(screen.getSpecJson())
                .thenReturn(screenSpecJson);

        when(wireframeRepository.findByScreenId(screenId))
                .thenReturn(Optional.of(wireframe));

        when(aiDocumentService.regenerateWireframe(
                screenSpecJson,
                reason
        )).thenThrow(
                new BusinessException(
                        ErrorCode.AI_RESPONSE_INVALID
                )
        );

        // when
        BusinessException exception =
                catchThrowableOfType(
                        () -> wireframeService
                                .regenerateWireframe(
                                        projectId,
                                        screenId,
                                        reason
                                ),
                        BusinessException.class
                );

        // then
        assertThat(exception.getErrorCode())
                .isEqualTo(
                        ErrorCode.AI_RESPONSE_INVALID
                );

        assertThat(wireframe.getVersion())
                .isEqualTo(1);

        assertThat(wireframe.getJsonDsl())
                .isEqualTo(oldJsonDsl);
    }

    private void mockGeneratedContent() {
        when(generatedContent.getType())
                .thenReturn("screen");

        when(generatedContent.getWidth())
                .thenReturn(375);

        when(generatedContent.getHeight())
                .thenReturn(812);

        when(generatedContent.getElements())
                .thenReturn(List.of(generatedElement));

        when(generatedElement.getId())
                .thenReturn("login-button");

        when(generatedElement.getType())
                .thenReturn("button");

        when(generatedElement.getText())
                .thenReturn("로그인");

        when(generatedElement.getX())
                .thenReturn(24);

        when(generatedElement.getY())
                .thenReturn(700);

        when(generatedElement.getW())
                .thenReturn(327);

        when(generatedElement.getH())
                .thenReturn(48);
    }
}