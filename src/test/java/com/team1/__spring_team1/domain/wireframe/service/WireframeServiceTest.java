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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
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
    private StageDocument confirmedScreenSpec;

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
    @DisplayName("선택한 화면의 와이어프레임을 새로 생성한다")
    void generateWireframe() throws Exception {
        // given
        Long projectId = 1L;
        Long stageDocumentId = 20L;
        Long screenId = 10L;

        String screenSpecJson =
                "{\"name\":\"로그인 화면\"}";

        Screen screen = createScreen(
                screenId,
                projectId,
                stageDocumentId,
                "로그인 화면",
                1,
                screenSpecJson
        );

        WireframeGenerateRequest request =
                new WireframeGenerateRequest(
                        List.of(screenId)
                );

        WireframeContent generatedContent =
                createWireframeContent(
                        "login-button",
                        "로그인"
                );

        when(stageService.getConfirmedScreenSpec(projectId))
                .thenReturn(confirmedScreenSpec);

        when(confirmedScreenSpec.getId())
                .thenReturn(stageDocumentId);

        when(screenRepository
                .findByStageDocumentIdOrderByScreenOrder(
                        stageDocumentId
                ))
                .thenReturn(List.of(screen));

        when(wireframeRepository
                .findAllByProjectId(projectId))
                .thenReturn(List.of());

        when(aiDocumentService
                .generateWireframe(screenSpecJson))
                .thenReturn(generatedContent);

        // when
        List<ScreenWireframeResponse> responses =
                wireframeService.generateWireframe(
                        projectId,
                        request,
                        loginUser
                );

        // then
        assertThat(responses).hasSize(1);

        ScreenWireframeResponse response =
                responses.getFirst();

        assertThat(response.screenId())
                .isEqualTo(screenId);

        assertThat(response.screenName())
                .isEqualTo("로그인 화면");

        assertThat(response.wireframe())
                .isNotNull();

        assertThat(response.wireframe().type())
                .isEqualTo("screen");

        assertThat(response.wireframe().width())
                .isEqualTo(375);

        assertThat(response.wireframe().height())
                .isEqualTo(812);

        assertThat(response.wireframe().elements())
                .hasSize(1);

        assertThat(
                response.wireframe()
                        .elements()
                        .getFirst()
                        .id()
        ).isEqualTo("login-button");

        verify(projectPermissionService)
                .validateProjectMember(
                        projectId,
                        loginUser.userId()
                );

        verify(aiDocumentService)
                .generateWireframe(screenSpecJson);

        ArgumentCaptor<List<Wireframe>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(wireframeRepository)
                .saveAll(captor.capture());

        List<Wireframe> savedWireframes =
                captor.getValue();

        assertThat(savedWireframes).hasSize(1);

        Wireframe savedWireframe =
                savedWireframes.getFirst();

        assertThat(savedWireframe.getProjectId())
                .isEqualTo(projectId);

        assertThat(savedWireframe.getScreenId())
                .isEqualTo(screenId);

        assertThat(savedWireframe.getVersion())
                .isEqualTo(1);

        assertThat(savedWireframe.getJsonDsl())
                .contains("\"id\":\"login-button\"");
    }

    @Test
    @DisplayName("이미 생성된 와이어프레임은 AI를 다시 호출하지 않고 반환한다")
    void returnExistingWireframeWithoutGeneration() {
        // given
        Long projectId = 1L;
        Long stageDocumentId = 20L;
        Long screenId = 10L;

        Screen screen = createScreen(
                screenId,
                projectId,
                stageDocumentId,
                "로그인 화면",
                1,
                "{\"name\":\"로그인 화면\"}"
        );

        Wireframe existingWireframe =
                new Wireframe(
                        projectId,
                        screenId,
                        createWireframeJson(
                                "existing-button",
                                "기존 버튼"
                        )
                );

        WireframeGenerateRequest request =
                new WireframeGenerateRequest(
                        List.of(screenId)
                );

        when(stageService.getConfirmedScreenSpec(projectId))
                .thenReturn(confirmedScreenSpec);

        when(confirmedScreenSpec.getId())
                .thenReturn(stageDocumentId);

        when(screenRepository
                .findByStageDocumentIdOrderByScreenOrder(
                        stageDocumentId
                ))
                .thenReturn(List.of(screen));

        when(wireframeRepository
                .findAllByProjectId(projectId))
                .thenReturn(List.of(existingWireframe));

        // when
        List<ScreenWireframeResponse> responses =
                wireframeService.generateWireframe(
                        projectId,
                        request,
                        loginUser
                );

        // then
        assertThat(responses).hasSize(1);

        assertThat(
                responses.getFirst()
                        .wireframe()
                        .elements()
                        .getFirst()
                        .id()
        ).isEqualTo("existing-button");

        verifyNoInteractions(aiDocumentService);

        verify(wireframeRepository, never())
                .saveAll(anyList());
    }

    @Test
    @DisplayName("중복된 화면 ID는 한 번만 처리한다")
    void removeDuplicatedScreenIds() throws Exception {
        // given
        Long projectId = 1L;
        Long stageDocumentId = 20L;
        Long screenId = 10L;

        String screenSpecJson =
                "{\"name\":\"로그인 화면\"}";

        Screen screen = createScreen(
                screenId,
                projectId,
                stageDocumentId,
                "로그인 화면",
                1,
                screenSpecJson
        );

        WireframeGenerateRequest request =
                new WireframeGenerateRequest(
                        List.of(
                                screenId,
                                screenId
                        )
                );

        when(stageService.getConfirmedScreenSpec(projectId))
                .thenReturn(confirmedScreenSpec);

        when(confirmedScreenSpec.getId())
                .thenReturn(stageDocumentId);

        when(screenRepository
                .findByStageDocumentIdOrderByScreenOrder(
                        stageDocumentId
                ))
                .thenReturn(List.of(screen));

        when(wireframeRepository
                .findAllByProjectId(projectId))
                .thenReturn(List.of());

        when(aiDocumentService
                .generateWireframe(screenSpecJson))
                .thenReturn(
                        createWireframeContent(
                                "login-button",
                                "로그인"
                        )
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
                .generateWireframe(screenSpecJson);

        verify(wireframeRepository, times(1))
                .saveAll(anyList());
    }

    @Test
    @DisplayName("확정된 화면 명세에 없는 화면은 생성할 수 없다")
    void requestedScreenNotFound() {
        // given
        Long projectId = 1L;
        Long stageDocumentId = 20L;

        Screen existingScreen = createScreen(
                10L,
                projectId,
                stageDocumentId,
                "로그인 화면",
                1,
                "{\"name\":\"로그인 화면\"}"
        );

        WireframeGenerateRequest request =
                new WireframeGenerateRequest(
                        List.of(999L)
                );

        when(stageService.getConfirmedScreenSpec(projectId))
                .thenReturn(confirmedScreenSpec);

        when(confirmedScreenSpec.getId())
                .thenReturn(stageDocumentId);

        when(screenRepository
                .findByStageDocumentIdOrderByScreenOrder(
                        stageDocumentId
                ))
                .thenReturn(List.of(existingScreen));

        // when
        BusinessException exception =
                catchThrowableOfType(
                        () -> wireframeService
                                .generateWireframe(
                                        projectId,
                                        request,
                                        loginUser
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
    @DisplayName("프로젝트 멤버가 아니면 와이어프레임을 생성할 수 없다")
    void generateWireframeMemberOnly() {
        // given
        Long projectId = 1L;

        WireframeGenerateRequest request =
                new WireframeGenerateRequest(
                        List.of(10L)
                );

        doThrow(
                new BusinessException(
                        ErrorCode.NOT_PROJECT_MEMBER
                )
        ).when(projectPermissionService)
                .validateProjectMember(
                        projectId,
                        loginUser.userId()
                );

        // when
        BusinessException exception =
                catchThrowableOfType(
                        () -> wireframeService
                                .generateWireframe(
                                        projectId,
                                        request,
                                        loginUser
                                ),
                        BusinessException.class
                );

        // then
        assertThat(exception.getErrorCode())
                .isEqualTo(
                        ErrorCode.NOT_PROJECT_MEMBER
                );

        verifyNoInteractions(
                stageService,
                screenRepository,
                wireframeRepository,
                aiDocumentService
        );
    }

    @Test
    @DisplayName("프로젝트 화면 목록과 생성된 와이어프레임을 조회한다")
    void getScreens() {
        // given
        Long projectId = 1L;
        Long stageDocumentId = 20L;

        Screen firstScreen = createScreen(
                10L,
                projectId,
                stageDocumentId,
                "로그인 화면",
                1,
                "{\"name\":\"로그인 화면\"}"
        );

        Screen secondScreen = createScreen(
                11L,
                projectId,
                stageDocumentId,
                "회원가입 화면",
                2,
                "{\"name\":\"회원가입 화면\"}"
        );

        Wireframe firstWireframe =
                new Wireframe(
                        projectId,
                        firstScreen.getId(),
                        createWireframeJson(
                                "login-button",
                                "로그인"
                        )
                );

        when(stageService.getConfirmedScreenSpec(projectId))
                .thenReturn(confirmedScreenSpec);

        when(confirmedScreenSpec.getId())
                .thenReturn(stageDocumentId);

        when(screenRepository
                .findByStageDocumentIdOrderByScreenOrder(
                        stageDocumentId
                ))
                .thenReturn(
                        List.of(
                                firstScreen,
                                secondScreen
                        )
                );

        when(wireframeRepository
                .findAllByProjectId(projectId))
                .thenReturn(List.of(firstWireframe));

        // when
        List<ScreenWireframeResponse> responses =
                wireframeService.getScreens(
                        projectId,
                        loginUser
                );

        // then
        assertThat(responses).hasSize(2);

        ScreenWireframeResponse firstResponse =
                responses.get(0);

        assertThat(firstResponse.screenId())
                .isEqualTo(firstScreen.getId());

        assertThat(firstResponse.screenName())
                .isEqualTo("로그인 화면");

        assertThat(firstResponse.wireframe())
                .isNotNull();

        assertThat(
                firstResponse.wireframe()
                        .elements()
                        .getFirst()
                        .id()
        ).isEqualTo("login-button");

        ScreenWireframeResponse secondResponse =
                responses.get(1);

        assertThat(secondResponse.screenId())
                .isEqualTo(secondScreen.getId());

        assertThat(secondResponse.screenName())
                .isEqualTo("회원가입 화면");

        assertThat(secondResponse.wireframe())
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
        Long projectId = 1L;
        Long screenId = 10L;

        Screen screen = createScreen(
                screenId,
                projectId,
                20L,
                "로그인 화면",
                1,
                "{\"name\":\"로그인 화면\"}"
        );

        Wireframe wireframe =
                new Wireframe(
                        projectId,
                        screenId,
                        createWireframeJson(
                                "login-button",
                                "로그인"
                        )
                );

        when(screenRepository.findById(screenId))
                .thenReturn(Optional.of(screen));

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

        assertThat(
                response.elements()
                        .getFirst()
                        .id()
        ).isEqualTo("login-button");

        verify(projectPermissionService)
                .validateProjectMember(
                        projectId,
                        loginUser.userId()
                );
    }

    @Test
    @DisplayName("존재하지 않는 화면의 와이어프레임은 조회할 수 없다")
    void getWireframeScreenNotFound() {
        // given
        Long screenId = 999L;

        when(screenRepository.findById(screenId))
                .thenReturn(Optional.empty());

        // when
        BusinessException exception =
                catchThrowableOfType(
                        () -> wireframeService
                                .getWireframe(
                                        screenId,
                                        loginUser
                                ),
                        BusinessException.class
                );

        // then
        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.SCREEN_NOT_FOUND);

        verifyNoInteractions(
                projectPermissionService,
                wireframeRepository
        );
    }

    @Test
    @DisplayName("화면에 생성된 와이어프레임이 없으면 조회할 수 없다")
    void getWireframeNotFound() {
        // given
        Long projectId = 1L;
        Long screenId = 10L;

        Screen screen = createScreen(
                screenId,
                projectId,
                20L,
                "로그인 화면",
                1,
                "{\"name\":\"로그인 화면\"}"
        );

        when(screenRepository.findById(screenId))
                .thenReturn(Optional.of(screen));

        when(wireframeRepository.findByScreenId(screenId))
                .thenReturn(Optional.empty());

        // when
        BusinessException exception =
                catchThrowableOfType(
                        () -> wireframeService
                                .getWireframe(
                                        screenId,
                                        loginUser
                                ),
                        BusinessException.class
                );

        // then
        assertThat(exception.getErrorCode())
                .isEqualTo(
                        ErrorCode.WIREFRAME_NOT_FOUND
                );

        verify(projectPermissionService)
                .validateProjectMember(
                        projectId,
                        loginUser.userId()
                );
    }

    @Test
    @DisplayName("재생성된 JSON DSL로 기존 와이어프레임을 교체하고 버전을 증가시킨다")
    void regenerateWireframe() throws Exception {
        // given
        Long projectId = 1L;
        Long screenId = 10L;

        String screenSpecJson =
                "{\"name\":\"로그인 화면\"}";

        String reason =
                "로그인 버튼을 화면 하단에 배치해주세요.";

        String oldJsonDsl =
                createWireframeJson(
                        "old-button",
                        "기존 버튼"
                );

        Screen screen = createScreen(
                screenId,
                projectId,
                20L,
                "로그인 화면",
                1,
                screenSpecJson
        );

        Wireframe wireframe =
                new Wireframe(
                        projectId,
                        screenId,
                        oldJsonDsl
                );

        when(screenRepository.findById(screenId))
                .thenReturn(Optional.of(screen));

        when(wireframeRepository.findByScreenId(screenId))
                .thenReturn(Optional.of(wireframe));

        when(aiDocumentService.regenerateWireframe(
                screenSpecJson,
                reason
        )).thenReturn(
                createWireframeContent(
                        "login-button",
                        "로그인"
                )
        );

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

        assertThat(
                response.elements()
                        .getFirst()
                        .id()
        ).isEqualTo("login-button");

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
    @DisplayName("존재하지 않는 화면은 재생성할 수 없다")
    void regenerateScreenNotFound() {
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
    @DisplayName("요청 프로젝트와 화면의 프로젝트가 다르면 재생성할 수 없다")
    void regenerateScreenProjectMismatch() {
        // given
        Long requestProjectId = 1L;
        Long screenId = 10L;

        Screen screen = createScreen(
                screenId,
                2L,
                20L,
                "로그인 화면",
                1,
                "{\"name\":\"로그인 화면\"}"
        );

        when(screenRepository.findById(screenId))
                .thenReturn(Optional.of(screen));

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
    @DisplayName("기존 와이어프레임이 없으면 재생성할 수 없다")
    void regenerateWireframeNotFound() {
        // given
        Long projectId = 1L;
        Long screenId = 10L;

        Screen screen = createScreen(
                screenId,
                projectId,
                20L,
                "로그인 화면",
                1,
                "{\"name\":\"로그인 화면\"}"
        );

        when(screenRepository.findById(screenId))
                .thenReturn(Optional.of(screen));

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
    @DisplayName("요청 프로젝트와 와이어프레임의 프로젝트가 다르면 재생성할 수 없다")
    void regenerateWireframeProjectMismatch() {
        // given
        Long requestProjectId = 1L;
        Long screenId = 10L;

        Screen screen = createScreen(
                screenId,
                requestProjectId,
                20L,
                "로그인 화면",
                1,
                "{\"name\":\"로그인 화면\"}"
        );

        Wireframe otherProjectWireframe =
                new Wireframe(
                        2L,
                        screenId,
                        createWireframeJson(
                                "other-button",
                                "다른 프로젝트 버튼"
                        )
                );

        when(screenRepository.findById(screenId))
                .thenReturn(Optional.of(screen));

        when(wireframeRepository.findByScreenId(screenId))
                .thenReturn(
                        Optional.of(otherProjectWireframe)
                );

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
                .isEqualTo(
                        ErrorCode.WIREFRAME_NOT_FOUND
                );

        verifyNoInteractions(aiDocumentService);
    }

    @Test
    @DisplayName("AI 재생성 실패 시 기존 와이어프레임은 변경되지 않는다")
    void aiFailureDoesNotChangeWireframe() {
        // given
        Long projectId = 1L;
        Long screenId = 10L;

        String screenSpecJson =
                "{\"name\":\"로그인 화면\"}";

        String reason =
                "버튼 위치를 변경해주세요.";

        String oldJsonDsl =
                createWireframeJson(
                        "old-button",
                        "기존 버튼"
                );

        Screen screen = createScreen(
                screenId,
                projectId,
                20L,
                "로그인 화면",
                1,
                screenSpecJson
        );

        Wireframe wireframe =
                new Wireframe(
                        projectId,
                        screenId,
                        oldJsonDsl
                );

        when(screenRepository.findById(screenId))
                .thenReturn(Optional.of(screen));

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

    private Screen createScreen(
            Long id,
            Long projectId,
            Long stageDocumentId,
            String name,
            Integer screenOrder,
            String specJson
    ) {
        Screen screen = Screen.create(
                projectId,
                stageDocumentId,
                name,
                name + "의 목적",
                screenOrder,
                specJson
        );

        ReflectionTestUtils.setField(
                screen,
                "id",
                id
        );

        return screen;
    }

    private WireframeContent createWireframeContent(
            String elementId,
            String text
    ) throws Exception {
        return objectMapper.readValue(
                createWireframeJson(
                        elementId,
                        text
                ),
                WireframeContent.class
        );
    }

    private String createWireframeJson(
            String elementId,
            String text
    ) {
        return """
                {
                  "type": "screen",
                  "width": 375,
                  "height": 812,
                  "elements": [
                    {
                      "id": "%s",
                      "type": "button",
                      "text": "%s",
                      "x": 24,
                      "y": 700,
                      "w": 327,
                      "h": 48
                    }
                  ]
                }
                """.formatted(
                elementId,
                text
        );
    }
}