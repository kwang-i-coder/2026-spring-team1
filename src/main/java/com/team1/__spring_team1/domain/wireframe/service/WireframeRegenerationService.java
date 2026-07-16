package com.team1.__spring_team1.domain.wireframe.service;

import com.team1.__spring_team1.domain.project.service.ProjectPermissionService;
import com.team1.__spring_team1.domain.stage.entity.Screen;
import com.team1.__spring_team1.domain.stage.repository.ScreenRepository;
import com.team1.__spring_team1.domain.user.entity.User;
import com.team1.__spring_team1.domain.user.repository.UserRepository;
import com.team1.__spring_team1.domain.wireframe.dto.request.WireframeRegenerationCreateRequest;
import com.team1.__spring_team1.domain.wireframe.dto.response.WireframeRegenerationCreateResponse;
import com.team1.__spring_team1.domain.wireframe.dto.response.WireframeRegenerationListResponse;
import com.team1.__spring_team1.domain.wireframe.dto.response.WireframeRegenerationRequesterResponse;
import com.team1.__spring_team1.domain.wireframe.dto.response.WireframeRegenerationResponse;
import com.team1.__spring_team1.domain.wireframe.entity.WireframeRegenerationRequest;
import com.team1.__spring_team1.domain.wireframe.entity.WireframeRegenerationRequestStatus;
import com.team1.__spring_team1.domain.wireframe.repository.WireframeRegenerationRequestRepository;
import com.team1.__spring_team1.domain.wireframe.repository.WireframeRepository;
import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import com.team1.__spring_team1.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WireframeRegenerationService {

    private final ScreenRepository screenRepository;
    private final WireframeRepository wireframeRepository;
    private final WireframeRegenerationRequestRepository wireframeRegenerationRequestRepository;
    private final ProjectPermissionService projectPermissionService;
    private final UserRepository userRepository;

    // 와이어프래임 재생성 요청
    @Transactional
    public WireframeRegenerationCreateResponse createRegenerationRequest(Long screenId, WireframeRegenerationCreateRequest request, LoginUser loginUser) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCREEN_NOT_FOUND));

        projectPermissionService.validateProjectMember(screen.getProjectId() ,loginUser.userId());

        if (!wireframeRepository.existsByScreenId(screenId)) {
            throw new BusinessException(ErrorCode.WIREFRAME_NOT_FOUND);
        }
        boolean pendingRequestExists = wireframeRegenerationRequestRepository.existsByScreenIdAndStatus(screenId, WireframeRegenerationRequestStatus.PENDING);
        if (pendingRequestExists) {
            throw new BusinessException(ErrorCode.REGENERATION_REQUEST_ALREADY_EXISTS);
        }

        WireframeRegenerationRequest regenerationRequest = new WireframeRegenerationRequest(screen.getProjectId(), screenId, loginUser.userId(), request.reason());
        WireframeRegenerationRequest savedRequest = wireframeRegenerationRequestRepository.save(regenerationRequest);

        return new WireframeRegenerationCreateResponse(savedRequest.getId(), savedRequest.getScreenId(), savedRequest.getStatus().name(), "와이어프레임 재생성 요청이 등록되었습니다.");
    }

    // 와이어프레임 재생성 요청 조회
    public WireframeRegenerationListResponse getRegenerationRequests(Long projectId, LoginUser loginUser) {
        projectPermissionService.validateProjectLeader(projectId, loginUser.userId());

        List<WireframeRegenerationRequest> requests = wireframeRegenerationRequestRepository.findAllByProjectIdOrderByCreatedAtDesc(projectId);
        if(requests.isEmpty()) {
            return new WireframeRegenerationListResponse(List.of());
        }

        Set<Long> screenIds = requests.stream().map(WireframeRegenerationRequest::getScreenId).collect(Collectors.toSet());
        Map<Long, Screen> screenMap = screenRepository.findAllById(screenIds).stream().collect(Collectors.toMap(Screen::getId, Function.identity()));
        Set<Long> requesterIds = requests.stream().map(WireframeRegenerationRequest::getRequesterId).collect(Collectors.toSet());
        Map<Long, User> requesterMap = userRepository.findByIdIn(requesterIds).stream().collect(Collectors.toMap(User::getId, Function.identity()));

        List<WireframeRegenerationResponse> responses = requests.stream().map(request -> toRegenerationResponse(request, screenMap, requesterMap)).toList();

        return new WireframeRegenerationListResponse(responses);
    }
    private WireframeRegenerationResponse toRegenerationResponse(WireframeRegenerationRequest request, Map<Long, Screen> screenMap, Map<Long, User> requesterMap) {
        Screen screen = screenMap.get(request.getScreenId());
        if (screen == null) {
            throw new BusinessException(ErrorCode.SCREEN_NOT_FOUND);
        }

        User requester = requesterMap.get(request.getRequesterId());

        if (requester == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        WireframeRegenerationRequesterResponse requestedBy = new WireframeRegenerationRequesterResponse(requester.getId(), requester.getName());

        return new WireframeRegenerationResponse(request.getId(), request.getScreenId(), screen.getName(), requestedBy, request.getReason(), request.getStatus().name(), request.getCreatedAt());
    }
}
