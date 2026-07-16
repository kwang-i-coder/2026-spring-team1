package com.team1.__spring_team1.domain.wireframe.service;

import com.team1.__spring_team1.domain.project.service.ProjectPermissionService;
import com.team1.__spring_team1.domain.wireframe.dto.response.WireframeDslResponse;
import com.team1.__spring_team1.domain.wireframe.dto.response.WireframeElementResponse;
import com.team1.__spring_team1.domain.wireframe.dto.response.WireframeRegenerationAcceptResponse;
import com.team1.__spring_team1.domain.wireframe.entity.WireframeRegenerationRequest;
import com.team1.__spring_team1.domain.wireframe.entity.WireframeRegenerationRequestStatus;
import com.team1.__spring_team1.domain.wireframe.repository.WireframeRegenerationRequestRepository;
import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import com.team1.__spring_team1.global.security.LoginUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WireframeRegenerationAcceptServiceTest {

    @Mock
    private WireframeRegenerationRequestRepository
            regenerationRequestRepository;

    @Mock
    private ProjectPermissionService
            projectPermissionService;

    @Mock
    private WireframeService wireframeService;

    private WireframeRegenerationAcceptService
            acceptService;

    private LoginUser loginUser;

    @BeforeEach
    void setUp() {
        acceptService =
                new WireframeRegenerationAcceptService(
                        regenerationRequestRepository,
                        projectPermissionService,
                        wireframeService
                );

        loginUser = new LoginUser(
                100L,
                "leader",
                "프로젝트 리더"
        );
    }

    @Test
    @DisplayName(
            "프로젝트 리더가 재생성 요청을 승인한다"
    )
    void acceptRegenerationRequest() {
        // given
        Long requestId = 50L;
        Long projectId = 1L;
        Long screenId = 10L;

        WireframeRegenerationRequest regenerationRequest =
                createPendingRequest(
                        requestId,
                        projectId,
                        screenId
                );

        WireframeDslResponse regeneratedWireframe =
                createWireframeResponse();

        when(regenerationRequestRepository
                .findById(requestId))
                .thenReturn(
                        Optional.of(regenerationRequest)
                );

        when(wireframeService.regenerateWireframe(
                projectId,
                screenId,
                regenerationRequest.getReason()
        )).thenReturn(regeneratedWireframe);

        // when
        WireframeRegenerationAcceptResponse response =
                acceptService.acceptRegenerationRequest(
                        requestId,
                        loginUser
                );

        // then
        assertThat(response.requestId())
                .isEqualTo(requestId);

        assertThat(response.screenId())
                .isEqualTo(screenId);

        assertThat(response.status())
                .isEqualTo("APPROVED");

        assertThat(response.message())
                .isEqualTo(
                        "와이어프레임 재생성 요청이 승인되었습니다."
                );

        assertThat(response.wireframe())
                .isEqualTo(regeneratedWireframe);

        assertThat(regenerationRequest.getStatus())
                .isEqualTo(
                        WireframeRegenerationRequestStatus.APPROVED
                );

        assertThat(regenerationRequest.getReviewerId())
                .isEqualTo(loginUser.userId());

        assertThat(regenerationRequest.getReviewedAt())
                .isNotNull();

        verify(projectPermissionService)
                .validateProjectLeader(
                        projectId,
                        loginUser.userId()
                );

        verify(wireframeService)
                .regenerateWireframe(
                        projectId,
                        screenId,
                        regenerationRequest.getReason()
                );
    }

    @Test
    @DisplayName(
            "존재하지 않는 재생성 요청은 승인할 수 없다"
    )
    void requestNotFound() {
        // given
        Long requestId = 999L;

        when(regenerationRequestRepository
                .findById(requestId))
                .thenReturn(Optional.empty());

        // when
        BusinessException exception =
                catchThrowableOfType(
                        () -> acceptService
                                .acceptRegenerationRequest(
                                        requestId,
                                        loginUser
                                ),
                        BusinessException.class
                );

        // then
        assertThat(exception.getErrorCode())
                .isEqualTo(
                        ErrorCode
                                .REGENERATION_REQUEST_NOT_FOUND
                );

        verifyNoInteractions(
                projectPermissionService,
                wireframeService
        );
    }

    @Test
    @DisplayName(
            "프로젝트 리더가 아니면 재생성 요청을 승인할 수 없다"
    )
    void projectLeaderOnly() {
        // given
        Long requestId = 50L;
        Long projectId = 1L;
        Long screenId = 10L;

        WireframeRegenerationRequest regenerationRequest =
                createPendingRequest(
                        requestId,
                        projectId,
                        screenId
                );

        when(regenerationRequestRepository
                .findById(requestId))
                .thenReturn(
                        Optional.of(regenerationRequest)
                );

        doThrow(
                new BusinessException(
                        ErrorCode.PROJECT_LEADER_ONLY
                )
        ).when(projectPermissionService)
                .validateProjectLeader(
                        projectId,
                        loginUser.userId()
                );

        // when
        BusinessException exception =
                catchThrowableOfType(
                        () -> acceptService
                                .acceptRegenerationRequest(
                                        requestId,
                                        loginUser
                                ),
                        BusinessException.class
                );

        // then
        assertThat(exception.getErrorCode())
                .isEqualTo(
                        ErrorCode.PROJECT_LEADER_ONLY
                );

        assertThat(regenerationRequest.getStatus())
                .isEqualTo(
                        WireframeRegenerationRequestStatus.PENDING
                );

        verifyNoInteractions(wireframeService);
    }

    @Test
    @DisplayName(
            "이미 처리된 재생성 요청은 다시 승인할 수 없다"
    )
    void alreadyHandledRequest() {
        // given
        Long requestId = 50L;
        Long projectId = 1L;
        Long screenId = 10L;

        WireframeRegenerationRequest regenerationRequest =
                createPendingRequest(
                        requestId,
                        projectId,
                        screenId
                );

        regenerationRequest.reject(200L);

        when(regenerationRequestRepository
                .findById(requestId))
                .thenReturn(
                        Optional.of(regenerationRequest)
                );

        // when
        BusinessException exception =
                catchThrowableOfType(
                        () -> acceptService
                                .acceptRegenerationRequest(
                                        requestId,
                                        loginUser
                                ),
                        BusinessException.class
                );

        // then
        assertThat(exception.getErrorCode())
                .isEqualTo(
                        ErrorCode
                                .REGENERATION_REQUEST_ALREADY_HANDLED
                );

        assertThat(regenerationRequest.getStatus())
                .isEqualTo(
                        WireframeRegenerationRequestStatus.REJECTED
                );

        verify(wireframeService, never())
                .regenerateWireframe(
                        projectId,
                        screenId,
                        regenerationRequest.getReason()
                );
    }

    @Test
    @DisplayName(
            "AI 재생성 실패 시 요청은 PENDING 상태를 유지한다"
    )
    void regenerationFailureKeepsPendingStatus() {
        // given
        Long requestId = 50L;
        Long projectId = 1L;
        Long screenId = 10L;

        WireframeRegenerationRequest regenerationRequest =
                createPendingRequest(
                        requestId,
                        projectId,
                        screenId
                );

        when(regenerationRequestRepository
                .findById(requestId))
                .thenReturn(
                        Optional.of(regenerationRequest)
                );

        when(wireframeService.regenerateWireframe(
                projectId,
                screenId,
                regenerationRequest.getReason()
        )).thenThrow(
                new BusinessException(
                        ErrorCode.AI_RESPONSE_INVALID
                )
        );

        // when
        BusinessException exception =
                catchThrowableOfType(
                        () -> acceptService
                                .acceptRegenerationRequest(
                                        requestId,
                                        loginUser
                                ),
                        BusinessException.class
                );

        // then
        assertThat(exception.getErrorCode())
                .isEqualTo(
                        ErrorCode.AI_RESPONSE_INVALID
                );

        assertThat(regenerationRequest.getStatus())
                .isEqualTo(
                        WireframeRegenerationRequestStatus.PENDING
                );

        assertThat(regenerationRequest.getReviewerId())
                .isNull();

        assertThat(regenerationRequest.getReviewedAt())
                .isNull();
    }

    private WireframeRegenerationRequest
    createPendingRequest(
            Long requestId,
            Long projectId,
            Long screenId
    ) {
        WireframeRegenerationRequest request =
                new WireframeRegenerationRequest(
                        projectId,
                        screenId,
                        300L,
                        "로그인 버튼을 화면 하단에 배치해주세요."
                );

        ReflectionTestUtils.setField(
                request,
                "id",
                requestId
        );

        return request;
    }

    private WireframeDslResponse
    createWireframeResponse() {
        WireframeElementResponse button =
                new WireframeElementResponse(
                        "login-button",
                        "button",
                        "로그인",
                        24,
                        700,
                        327,
                        48
                );

        return new WireframeDslResponse(
                "screen",
                375,
                812,
                List.of(button)
        );
    }
}