package com.team1.__spring_team1.domain.wireframe.service;

import com.team1.__spring_team1.domain.project.service.ProjectPermissionService;
import com.team1.__spring_team1.domain.stage.entity.Screen;
import com.team1.__spring_team1.domain.stage.repository.ScreenRepository;
import com.team1.__spring_team1.domain.wireframe.dto.request.WireframeRegenerationCreateRequest;
import com.team1.__spring_team1.domain.wireframe.dto.response.WireframeRegenerationCreateResponse;
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

@Service
@RequiredArgsConstructor
public class WireframeRegenerationService {

    private final ScreenRepository screenRepository;
    private final WireframeRepository wireframeRepository;
    private final WireframeRegenerationRequestRepository wireframeRegenerationRequestRepository;
    private final ProjectPermissionService projectPermissionService;

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
}
