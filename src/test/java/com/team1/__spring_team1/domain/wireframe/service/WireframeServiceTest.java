package com.team1.__spring_team1.domain.wireframe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.__spring_team1.domain.ai.dto.WireframeContent;
import com.team1.__spring_team1.domain.ai.service.AiDocumentService;
import com.team1.__spring_team1.domain.project.service.ProjectPermissionService;
import com.team1.__spring_team1.domain.stage.entity.Screen;
import com.team1.__spring_team1.domain.stage.entity.StageDocument;
import com.team1.__spring_team1.domain.stage.repository.ScreenRepository;
import com.team1.__spring_team1.domain.stage.service.StageService;
import com.team1.__spring_team1.domain.wireframe.dto.request.WireframeGenerateRequest;
import com.team1.__spring_team1.domain.wireframe.dto.response.ScreenWireframeResponse;
import com.team1.__spring_team1.domain.wireframe.dto.response.WireframeDslResponse;
import com.team1.__spring_team1.domain.wireframe.entity.Wireframe;
import com.team1.__spring_team1.domain.wireframe.repository.WireframeRepository;
import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import com.team1.__spring_team1.global.security.LoginUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    private ProjectPermissionService projectPermissionService;

    @Mock
    private AiDocumentService aiDocumentService;

    @Mock
    private StageDocument stageDocument;

    @Mock
    private Screen firstScreen;

    @Mock
    private Screen secondScreen;

    @Mock
    private Wireframe wireframe;

    private WireframeService wireframeService;

    private ObjectMapper objectMapper;

    private LoginUser loginUser;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        wireframeService = new WireframeService(
                wireframeRepository,
                screenRepository,
                stageService,
                projectPermissionService,
                aiDocumentService,
                objectMapper
        );

        loginUser = new LoginUser(
                100L,
                "test-user",
                "테스트 사용자"
        );
    }

    @Test
    @DisplayName("선택한 화면들의 와이어프레임을 생성한다")
    void generateWireframes() throws Exception {
        // given
        Long projectId = 1L;

        WireframeGenerateRequest request =
                new WireframeGenerateRequest(
                        List.of(10L, 11L)
                );

        when(stageDocument.getId())
                .thenReturn(50L);

        when(firstScreen.getId())
                .thenReturn(10L);

        when(firstScreen.getName())
                .thenReturn("프로젝트 목록 화면");

        when(firstScreen.getSpecJson())
                .thenReturn(
                        "{\"name\":\"프로젝트 목록 화면\"}"
                );

        when(secondScreen.getId())
                .thenReturn(11L);

        when(secondScreen.getName())
                .thenReturn("프로젝트 생성 화면");

        when(secondScreen.getSpecJson())
                .thenReturn(
                        "{\"name\":\"프로젝트 생성 화면\"}"
                );

        when(stageService.getConfirmedScreenSpec(projectId))
                .thenReturn(stageDocument);

        when(screenRepository
                .findByStageDocumentIdOrderByScreenOrder(50L))
                .thenReturn(List.of(
                        firstScreen,
                        secondScreen
                ));

        when(wireframeRepository.findAllByProjectId(projectId))
                .thenReturn(List.of());

        when(aiDocumentService.generateWireframe(
                firstScreen.getSpecJson()
        )).thenReturn(
                createWireframeContent("first-title")
        );

        when(aiDocumentService.generateWireframe(
                secondScreen.getSpecJson()
        )).thenReturn(
                createWireframeContent("second-title")
        );

        // when
        List<ScreenWireframeResponse> responses =
                wireframeService.generateWireframe(
                        projectId,
                        request,
                        loginUser
                );

        // then
        assertThat(responses).hasSize(2);

        assertThat(responses.get(0).screenId())
                .isEqualTo(10L);

        assertThat(responses.get(0).screenName())
                .isEqualTo("프로젝트 목록 화면");

        assertThat(responses.get(0).wireframe())
                .isNotNull();

        assertThat(responses.get(0).wireframe().width())
                .isEqualTo(375);

        assertThat(responses.get(0).wireframe().height())
                .isEqualTo(812);

        assertThat(responses.get(0)
                .wireframe()
                .elements()
                .get(0)
                .id())
                .isEqualTo("first-title");

        assertThat(responses.get(1).screenId())
                .isEqualTo(11L);

        assertThat(responses.get(1).screenName())
                .isEqualTo("프로젝트 생성 화면");

        assertThat(responses.get(1)
                .wireframe()
                .elements()
                .get(0)
                .id())
                .isEqualTo("second-title");

        verify(projectPermissionService)
                .validateProjectMember(
                        projectId,
                        loginUser.userId()
                );

        verify(aiDocumentService)
                .generateWireframe(
                        firstScreen.getSpecJson()
                );

        verify(aiDocumentService)
                .generateWireframe(
                        secondScreen.getSpecJson()
                );

        verify(wireframeRepository)
                .saveAll(anyList());
    }

    @Test
    @DisplayName("이미 와이어프레임이 존재하면 AI를 다시 호출하지 않는다")
    void generateWireframesReturnsExistingWireframe() {
        // given
        Long projectId = 1L;

        WireframeGenerateRequest request =
                new WireframeGenerateRequest(
                        List.of(10L)
                );

        when(stageDocument.getId())
                .thenReturn(50L);

        when(firstScreen.getId())
                .thenReturn(10L);

        when(firstScreen.getName())
                .thenReturn("프로젝트 목록 화면");

        when(stageService.getConfirmedScreenSpec(projectId))
                .thenReturn(stageDocument);

        when(screenRepository
                .findByStageDocumentIdOrderByScreenOrder(50L))
                .thenReturn(List.of(firstScreen));

        when(wireframe.getScreenId())
                .thenReturn(10L);

        when(wireframe.getJsonDsl())
                .thenReturn(
                        validWireframeJson("existing-title")
                );

        when(wireframeRepository.findAllByProjectId(projectId))
                .thenReturn(List.of(wireframe));

        // when
        List<ScreenWireframeResponse> responses =
                wireframeService.generateWireframe(
                        projectId,
                        request,
                        loginUser
                );

        // then
        assertThat(responses).hasSize(1);

        assertThat(responses.get(0).screenId())
                .isEqualTo(10L);

        assertThat(responses.get(0).wireframe())
                .isNotNull();

        assertThat(responses.get(0)
                .wireframe()
                .elements()
                .get(0)
                .id())
                .isEqualTo("existing-title");

        verifyNoInteractions(aiDocumentService);

        verify(wireframeRepository, never())
                .saveAll(anyList());
    }

    @Test
    @DisplayName("최신 확정 화면 명세에 없는 화면은 생성할 수 없다")
    void generateWireframesScreenNotFound() {
        // given
        Long projectId = 1L;

        WireframeGenerateRequest request =
                new WireframeGenerateRequest(
                        List.of(999L)
                );

        when(stageDocument.getId())
                .thenReturn(50L);

        when(firstScreen.getId())
                .thenReturn(10L);

        when(stageService.getConfirmedScreenSpec(projectId))
                .thenReturn(stageDocument);

        when(screenRepository
                .findByStageDocumentIdOrderByScreenOrder(50L))
                .thenReturn(List.of(firstScreen));

        // when & then
        assertThatThrownBy(() ->
                wireframeService.generateWireframe(
                        projectId,
                        request,
                        loginUser
                )
        )
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException =
                            (BusinessException) exception;

                    assertThat(
                            businessException.getErrorCode()
                    ).isEqualTo(
                            ErrorCode.SCREEN_NOT_FOUND
                    );
                });

        verifyNoInteractions(aiDocumentService);

        verify(wireframeRepository, never())
                .saveAll(anyList());
    }

    @Test
    @DisplayName("중복된 화면 ID는 한 번만 생성한다")
    void generateWireframesRemovesDuplicateScreenIds()
            throws Exception {
        // given
        Long projectId = 1L;

        WireframeGenerateRequest request =
                new WireframeGenerateRequest(
                        List.of(10L, 10L)
                );

        when(stageDocument.getId())
                .thenReturn(50L);

        when(firstScreen.getId())
                .thenReturn(10L);

        when(firstScreen.getName())
                .thenReturn("프로젝트 목록 화면");

        when(firstScreen.getSpecJson())
                .thenReturn(
                        "{\"name\":\"프로젝트 목록 화면\"}"
                );

        when(stageService.getConfirmedScreenSpec(projectId))
                .thenReturn(stageDocument);

        when(screenRepository
                .findByStageDocumentIdOrderByScreenOrder(50L))
                .thenReturn(List.of(firstScreen));

        when(wireframeRepository.findAllByProjectId(projectId))
                .thenReturn(List.of());

        when(aiDocumentService.generateWireframe(
                firstScreen.getSpecJson()
        )).thenReturn(
                createWireframeContent("title")
        );

        // when
        List<ScreenWireframeResponse> responses =
                wireframeService.generateWireframe(
                        projectId,
                        request,
                        loginUser
                );

        // then
        assertThat(responses).hasSize(1);

        verify(aiDocumentService, times(1))
                .generateWireframe(
                        firstScreen.getSpecJson()
                );

        verify(wireframeRepository, times(1))
                .saveAll(anyList());
    }

    @Test
    @DisplayName("AI 생성 중 하나라도 실패하면 새 와이어프레임을 저장하지 않는다")
    void generateWireframesDoesNotSaveWhenAiFails()
            throws Exception {
        // given
        Long projectId = 1L;

        WireframeGenerateRequest request =
                new WireframeGenerateRequest(
                        List.of(10L, 11L)
                );

        when(stageDocument.getId())
                .thenReturn(50L);

        when(firstScreen.getId())
                .thenReturn(10L);

        when(firstScreen.getSpecJson())
                .thenReturn(
                        "{\"name\":\"첫 번째 화면\"}"
                );

        when(secondScreen.getId())
                .thenReturn(11L);

        when(secondScreen.getSpecJson())
                .thenReturn(
                        "{\"name\":\"두 번째 화면\"}"
                );

        when(stageService.getConfirmedScreenSpec(projectId))
                .thenReturn(stageDocument);

        when(screenRepository
                .findByStageDocumentIdOrderByScreenOrder(50L))
                .thenReturn(List.of(
                        firstScreen,
                        secondScreen
                ));

        when(wireframeRepository.findAllByProjectId(projectId))
                .thenReturn(List.of());

        when(aiDocumentService.generateWireframe(
                firstScreen.getSpecJson()
        )).thenReturn(
                createWireframeContent("first-title")
        );

        when(aiDocumentService.generateWireframe(
                secondScreen.getSpecJson()
        )).thenThrow(
                new BusinessException(
                        ErrorCode.AI_GENERATION_FAILED
                )
        );

        // when & then
        assertThatThrownBy(() ->
                wireframeService.generateWireframe(
                        projectId,
                        request,
                        loginUser
                )
        )
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException =
                            (BusinessException) exception;

                    assertThat(
                            businessException.getErrorCode()
                    ).isEqualTo(
                            ErrorCode.AI_GENERATION_FAILED
                    );
                });

        verify(wireframeRepository, never())
                .saveAll(anyList());
    }

    @Test
    @DisplayName("프로젝트 화면 목록과 생성된 와이어프레임을 함께 조회한다")
    void getScreens() {
        // given
        Long projectId = 1L;

        when(stageDocument.getId())
                .thenReturn(50L);

        when(firstScreen.getId())
                .thenReturn(10L);

        when(firstScreen.getName())
                .thenReturn("프로젝트 목록 화면");

        when(secondScreen.getId())
                .thenReturn(11L);

        when(secondScreen.getName())
                .thenReturn("프로젝트 생성 화면");

        when(wireframe.getScreenId())
                .thenReturn(10L);

        when(wireframe.getJsonDsl())
                .thenReturn(
                        validWireframeJson("title")
                );

        when(stageService.getConfirmedScreenSpec(projectId))
                .thenReturn(stageDocument);

        when(screenRepository
                .findByStageDocumentIdOrderByScreenOrder(50L))
                .thenReturn(List.of(
                        firstScreen,
                        secondScreen
                ));

        when(wireframeRepository.findAllByProjectId(projectId))
                .thenReturn(List.of(wireframe));

        // when
        List<ScreenWireframeResponse> responses =
                wireframeService.getScreens(
                        projectId,
                        loginUser
                );

        // then
        assertThat(responses).hasSize(2);

        assertThat(responses.get(0).screenId())
                .isEqualTo(10L);

        assertThat(responses.get(0).screenName())
                .isEqualTo("프로젝트 목록 화면");

        assertThat(responses.get(0).wireframe())
                .isNotNull();

        assertThat(responses.get(0).wireframe().width())
                .isEqualTo(375);

        assertThat(responses.get(0).wireframe().height())
                .isEqualTo(812);

        assertThat(responses.get(1).screenId())
                .isEqualTo(11L);

        assertThat(responses.get(1).wireframe())
                .isNull();

        verify(projectPermissionService)
                .validateProjectMember(
                        projectId,
                        loginUser.userId()
                );
    }

    @Test
    @DisplayName("화면 ID로 와이어프레임을 조회한다")
    void getWireframe() {
        // given
        Long screenId = 10L;
        Long projectId = 1L;

        when(firstScreen.getProjectId())
                .thenReturn(projectId);

        when(wireframe.getJsonDsl())
                .thenReturn(
                        validWireframeJson("title")
                );

        when(screenRepository.findById(screenId))
                .thenReturn(Optional.of(firstScreen));

        when(wireframeRepository.findByScreenId(screenId))
                .thenReturn(Optional.of(wireframe));

        // when
        WireframeDslResponse response =
                wireframeService.getWireframe(
                        screenId,
                        loginUser
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

        verify(projectPermissionService)
                .validateProjectMember(
                        projectId,
                        loginUser.userId()
                );
    }

    @Test
    @DisplayName("존재하지 않는 화면이면 SCREEN_NOT_FOUND 예외가 발생한다")
    void getWireframeScreenNotFound() {
        // given
        Long screenId = 999L;

        when(screenRepository.findById(screenId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                wireframeService.getWireframe(
                        screenId,
                        loginUser
                )
        )
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException =
                            (BusinessException) exception;

                    assertThat(
                            businessException.getErrorCode()
                    ).isEqualTo(
                            ErrorCode.SCREEN_NOT_FOUND
                    );
                });
    }

    @Test
    @DisplayName("화면에 와이어프레임이 없으면 WIREFRAME_NOT_FOUND 예외가 발생한다")
    void getWireframeNotFound() {
        // given
        Long screenId = 10L;
        Long projectId = 1L;

        when(firstScreen.getProjectId())
                .thenReturn(projectId);

        when(screenRepository.findById(screenId))
                .thenReturn(Optional.of(firstScreen));

        when(wireframeRepository.findByScreenId(screenId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                wireframeService.getWireframe(
                        screenId,
                        loginUser
                )
        )
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException =
                            (BusinessException) exception;

                    assertThat(
                            businessException.getErrorCode()
                    ).isEqualTo(
                            ErrorCode.WIREFRAME_NOT_FOUND
                    );
                });

        verify(projectPermissionService)
                .validateProjectMember(
                        projectId,
                        loginUser.userId()
                );
    }

    private WireframeContent createWireframeContent(
            String elementId
    ) throws Exception {
        return objectMapper.readValue(
                validWireframeJson(elementId),
                WireframeContent.class
        );
    }

    private String validWireframeJson(
            String elementId
    ) {
        return """
                {
                  "type": "screen",
                  "width": 375,
                  "height": 812,
                  "elements": [
                    {
                      "id": "%s",
                      "type": "text",
                      "text": "화면 제목",
                      "x": 20,
                      "y": 20,
                      "w": 200,
                      "h": 40
                    }
                  ]
                }
                """.formatted(elementId);
    }
}