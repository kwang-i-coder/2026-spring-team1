package com.team1.__spring_team1.domain.wireframe.service;

import com.team1.__spring_team1.domain.project.service.ProjectPermissionService;
import com.team1.__spring_team1.domain.stage.entity.Screen;
import com.team1.__spring_team1.domain.stage.repository.ScreenRepository;
import com.team1.__spring_team1.domain.user.entity.User;
import com.team1.__spring_team1.domain.user.repository.UserRepository;
import com.team1.__spring_team1.domain.wireframe.dto.request.WireframeRegenerationCreateRequest;
import com.team1.__spring_team1.domain.wireframe.dto.response.WireframeRegenerationCreateResponse;
import com.team1.__spring_team1.domain.wireframe.dto.response.WireframeRegenerationListResponse;
import com.team1.__spring_team1.domain.wireframe.dto.response.WireframeRegenerationResponse;
import com.team1.__spring_team1.domain.wireframe.entity.WireframeRegenerationRequest;
import com.team1.__spring_team1.domain.wireframe.entity.WireframeRegenerationRequestStatus;
import com.team1.__spring_team1.domain.wireframe.repository.WireframeRegenerationRequestRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WireframeRegenerationServiceTest {

    @Mock
    private ScreenRepository screenRepository;

    @Mock
    private WireframeRepository wireframeRepository;

    @Mock
    private WireframeRegenerationRequestRepository
            regenerationRequestRepository;

    @Mock
    private ProjectPermissionService projectPermissionService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Screen screen;

    @Mock
    private User requester;

    private WireframeRegenerationService
            regenerationService;

    private LoginUser loginUser;

    @BeforeEach
    void setUp() {
        regenerationService =
                new WireframeRegenerationService(
                        screenRepository,
                        wireframeRepository,
                        regenerationRequestRepository,
                        projectPermissionService,
                        userRepository
                );

        loginUser = new LoginUser(
                100L,
                "test-user",
                "테스트 사용자"
        );
    }

    @Test
    @DisplayName("프로젝트 멤버가 와이어프레임 재생성을 요청한다")
    void createRegenerationRequest() {
        // given
        Long screenId = 10L;
        Long projectId = 1L;

        WireframeRegenerationCreateRequest request =
                new WireframeRegenerationCreateRequest(
                        "버튼 배치를 변경해주세요."
                );

        when(screen.getProjectId())
                .thenReturn(projectId);

        when(screenRepository.findById(screenId))
                .thenReturn(Optional.of(screen));

        when(wireframeRepository.existsByScreenId(screenId))
                .thenReturn(true);

        when(regenerationRequestRepository
                .existsByScreenIdAndStatus(
                        screenId,
                        WireframeRegenerationRequestStatus.PENDING
                ))
                .thenReturn(false);

        doAnswer(invocation -> {
            WireframeRegenerationRequest savedRequest =
                    invocation.getArgument(0);

            ReflectionTestUtils.setField(
                    savedRequest,
                    "id",
                    500L
            );

            return savedRequest;
        }).when(regenerationRequestRepository)
                .save(
                        any(WireframeRegenerationRequest.class)
                );

        // when
        WireframeRegenerationCreateResponse response =
                regenerationService.createRegenerationRequest(
                        screenId,
                        request,
                        loginUser
                );

        // then
        assertThat(response.requestId())
                .isEqualTo(500L);

        assertThat(response.screenId())
                .isEqualTo(screenId);

        assertThat(response.status())
                .isEqualTo("PENDING");

        assertThat(response.message())
                .isEqualTo(
                        "와이어프레임 재생성 요청이 등록되었습니다."
                );

        verify(projectPermissionService)
                .validateProjectMember(
                        projectId,
                        loginUser.userId()
                );

        ArgumentCaptor<WireframeRegenerationRequest> captor =
                ArgumentCaptor.forClass(
                        WireframeRegenerationRequest.class
                );

        verify(regenerationRequestRepository)
                .save(captor.capture());

        WireframeRegenerationRequest savedRequest =
                captor.getValue();

        assertThat(savedRequest.getProjectId())
                .isEqualTo(projectId);

        assertThat(savedRequest.getScreenId())
                .isEqualTo(screenId);

        assertThat(savedRequest.getRequesterId())
                .isEqualTo(loginUser.userId());

        assertThat(savedRequest.getReason())
                .isEqualTo("버튼 배치를 변경해주세요.");

        assertThat(savedRequest.getStatus())
                .isEqualTo(
                        WireframeRegenerationRequestStatus.PENDING
                );
    }

    @Test
    @DisplayName("존재하지 않는 화면에는 재생성을 요청할 수 없다")
    void createRegenerationRequestScreenNotFound() {
        // given
        Long screenId = 999L;

        WireframeRegenerationCreateRequest request =
                new WireframeRegenerationCreateRequest(
                        "수정해주세요."
                );

        when(screenRepository.findById(screenId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                regenerationService.createRegenerationRequest(
                        screenId,
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

        verifyNoInteractions(
                projectPermissionService,
                wireframeRepository,
                regenerationRequestRepository,
                userRepository
        );
    }

    @Test
    @DisplayName("와이어프레임이 없는 화면에는 재생성을 요청할 수 없다")
    void createRegenerationRequestWireframeNotFound() {
        // given
        Long screenId = 10L;
        Long projectId = 1L;

        WireframeRegenerationCreateRequest request =
                new WireframeRegenerationCreateRequest(
                        "수정해주세요."
                );

        when(screen.getProjectId())
                .thenReturn(projectId);

        when(screenRepository.findById(screenId))
                .thenReturn(Optional.of(screen));

        when(wireframeRepository.existsByScreenId(screenId))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(() ->
                regenerationService.createRegenerationRequest(
                        screenId,
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
                            ErrorCode.WIREFRAME_NOT_FOUND
                    );
                });

        verify(projectPermissionService)
                .validateProjectMember(
                        projectId,
                        loginUser.userId()
                );

        verifyNoInteractions(
                regenerationRequestRepository,
                userRepository
        );
    }

    @Test
    @DisplayName("대기 중인 재생성 요청이 있으면 중복 요청할 수 없다")
    void createRegenerationRequestAlreadyExists() {
        // given
        Long screenId = 10L;
        Long projectId = 1L;

        WireframeRegenerationCreateRequest request =
                new WireframeRegenerationCreateRequest(
                        "다시 수정해주세요."
                );

        when(screen.getProjectId())
                .thenReturn(projectId);

        when(screenRepository.findById(screenId))
                .thenReturn(Optional.of(screen));

        when(wireframeRepository.existsByScreenId(screenId))
                .thenReturn(true);

        when(regenerationRequestRepository
                .existsByScreenIdAndStatus(
                        screenId,
                        WireframeRegenerationRequestStatus.PENDING
                ))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() ->
                regenerationService.createRegenerationRequest(
                        screenId,
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
                            ErrorCode.REGENERATION_REQUEST_ALREADY_EXISTS
                    );
                });

        verify(regenerationRequestRepository, never())
                .save(
                        any(WireframeRegenerationRequest.class)
                );

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("프로젝트 멤버가 아니면 재생성을 요청할 수 없다")
    void createRegenerationRequestNotProjectMember() {
        // given
        Long screenId = 10L;
        Long projectId = 1L;

        WireframeRegenerationCreateRequest request =
                new WireframeRegenerationCreateRequest(
                        "수정해주세요."
                );

        when(screen.getProjectId())
                .thenReturn(projectId);

        when(screenRepository.findById(screenId))
                .thenReturn(Optional.of(screen));

        doThrow(
                new BusinessException(
                        ErrorCode.NOT_PROJECT_MEMBER
                )
        ).when(projectPermissionService)
                .validateProjectMember(
                        projectId,
                        loginUser.userId()
                );

        // when & then
        assertThatThrownBy(() ->
                regenerationService.createRegenerationRequest(
                        screenId,
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
                            ErrorCode.NOT_PROJECT_MEMBER
                    );
                });

        verifyNoInteractions(
                wireframeRepository,
                regenerationRequestRepository,
                userRepository
        );
    }

    @Test
    @DisplayName("프로젝트 리더가 재생성 요청 목록을 조회한다")
    void getRegenerationRequests() {
        // given
        Long projectId = 1L;
        Long screenId = 10L;
        Long requesterId = 200L;

        LocalDateTime createdAt =
                LocalDateTime.of(
                        2026,
                        7,
                        16,
                        15,
                        30
                );

        WireframeRegenerationRequest regenerationRequest =
                new WireframeRegenerationRequest(
                        projectId,
                        screenId,
                        requesterId,
                        "버튼 위치를 수정해주세요."
                );

        ReflectionTestUtils.setField(
                regenerationRequest,
                "id",
                500L
        );

        ReflectionTestUtils.setField(
                regenerationRequest,
                "createdAt",
                createdAt
        );

        when(regenerationRequestRepository
                .findAllByProjectIdOrderByCreatedAtDesc(
                        projectId
                ))
                .thenReturn(
                        List.of(regenerationRequest)
                );

        when(screenRepository.findAllById(
                Set.of(screenId)
        )).thenReturn(
                List.of(screen)
        );

        when(screen.getId())
                .thenReturn(screenId);

        when(screen.getName())
                .thenReturn("로그인 화면");

        when(userRepository.findByIdIn(
                Set.of(requesterId)
        )).thenReturn(
                List.of(requester)
        );

        when(requester.getId())
                .thenReturn(requesterId);

        when(requester.getName())
                .thenReturn("요청자");

        // when
        WireframeRegenerationListResponse response =
                regenerationService.getRegenerationRequests(
                        projectId,
                        loginUser
                );

        // then
        assertThat(response.requests())
                .hasSize(1);

        WireframeRegenerationResponse result =
                response.requests().get(0);

        assertThat(result.requestId())
                .isEqualTo(500L);

        assertThat(result.screenId())
                .isEqualTo(screenId);

        assertThat(result.screenName())
                .isEqualTo("로그인 화면");

        assertThat(result.requestedBy().userId())
                .isEqualTo(requesterId);

        assertThat(result.requestedBy().name())
                .isEqualTo("요청자");

        assertThat(result.reason())
                .isEqualTo("버튼 위치를 수정해주세요.");

        assertThat(result.status())
                .isEqualTo("PENDING");

        assertThat(result.createdAt())
                .isEqualTo(createdAt);

        verify(projectPermissionService)
                .validateProjectLeader(
                        projectId,
                        loginUser.userId()
                );
    }

    @Test
    @DisplayName("재생성 요청이 없으면 빈 목록을 반환한다")
    void getRegenerationRequestsEmpty() {
        // given
        Long projectId = 1L;

        when(regenerationRequestRepository
                .findAllByProjectIdOrderByCreatedAtDesc(
                        projectId
                ))
                .thenReturn(List.of());

        // when
        WireframeRegenerationListResponse response =
                regenerationService.getRegenerationRequests(
                        projectId,
                        loginUser
                );

        // then
        assertThat(response.requests())
                .isEmpty();

        verify(projectPermissionService)
                .validateProjectLeader(
                        projectId,
                        loginUser.userId()
                );

        verifyNoInteractions(
                screenRepository,
                userRepository
        );
    }

    @Test
    @DisplayName("프로젝트 리더가 아니면 재생성 요청 목록을 조회할 수 없다")
    void getRegenerationRequestsNotProjectLeader() {
        // given
        Long projectId = 1L;

        doThrow(
                new BusinessException(
                        ErrorCode.PROJECT_LEADER_ONLY
                )
        ).when(projectPermissionService)
                .validateProjectLeader(
                        projectId,
                        loginUser.userId()
                );

        // when & then
        assertThatThrownBy(() ->
                regenerationService.getRegenerationRequests(
                        projectId,
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
                            ErrorCode.PROJECT_LEADER_ONLY
                    );
                });

        verifyNoInteractions(
                screenRepository,
                wireframeRepository,
                regenerationRequestRepository,
                userRepository
        );
    }
}