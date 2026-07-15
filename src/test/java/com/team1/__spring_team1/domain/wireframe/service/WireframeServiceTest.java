package com.team1.__spring_team1.domain.wireframe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.__spring_team1.domain.project.service.ProjectPermissionService;
import com.team1.__spring_team1.domain.stage.entity.Screen;
import com.team1.__spring_team1.domain.stage.entity.StageDocument;
import com.team1.__spring_team1.domain.stage.repository.ScreenRepository;
import com.team1.__spring_team1.domain.stage.service.StageService;
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
import static org.mockito.Mockito.verify;
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
    private StageDocument stageDocument;

    @Mock
    private Screen firstScreen;

    @Mock
    private Screen secondScreen;

    @Mock
    private Wireframe wireframe;

    private WireframeService wireframeService;

    private LoginUser loginUser;

    @BeforeEach
    void setUp() {
        wireframeService = new WireframeService(
                wireframeRepository,
                screenRepository,
                stageService,
                projectPermissionService,
                new ObjectMapper()
        );

        loginUser = new LoginUser(
                100L,
                "test-user",
                "테스트 사용자"
        );
    }

    @Test
    @DisplayName("프로젝트 화면 목록과 생성된 와이어프레임을 함께 조회한다")
    void getScreens() {
        // given
        Long projectId = 1L;

        when(stageDocument.getId()).thenReturn(50L);

        when(firstScreen.getId()).thenReturn(10L);
        when(firstScreen.getName()).thenReturn("프로젝트 목록 화면");

        when(secondScreen.getId()).thenReturn(11L);
        when(secondScreen.getName()).thenReturn("프로젝트 생성 화면");

        when(wireframe.getScreenId()).thenReturn(10L);
        when(wireframe.getJsonDsl()).thenReturn("""
                {
                  "type": "screen",
                  "width": 1440,
                  "height": 900,
                  "elements": []
                }
                """);

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
                .isEqualTo(1440);

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

        when(wireframe.getJsonDsl()).thenReturn("""
                {
                  "type": "screen",
                  "width": 1440,
                  "height": 900,
                  "elements": []
                }
                """);

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
                .isEqualTo(1440);

        assertThat(response.height())
                .isEqualTo(900);

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

                    assertThat(businessException.getErrorCode())
                            .isEqualTo(
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

                    assertThat(businessException.getErrorCode())
                            .isEqualTo(
                                    ErrorCode.WIREFRAME_NOT_FOUND
                            );
                });

        verify(projectPermissionService)
                .validateProjectMember(
                        projectId,
                        loginUser.userId()
                );
    }
}