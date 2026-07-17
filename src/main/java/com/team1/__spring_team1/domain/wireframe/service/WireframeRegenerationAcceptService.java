package com.team1.__spring_team1.domain.wireframe.service;

import com.team1.__spring_team1.domain.project.service.ProjectPermissionService;
import com.team1.__spring_team1.domain.realtime.dto.RealtimeEventType;
import com.team1.__spring_team1.domain.realtime.handler.ProjectWebSocketHandler;
import com.team1.__spring_team1.domain.wireframe.dto.response.WireframeDslResponse;
import com.team1.__spring_team1.domain.wireframe.dto.response.WireframeRegenerationAcceptResponse;
import com.team1.__spring_team1.domain.wireframe.entity.WireframeRegenerationRequest;
import com.team1.__spring_team1.domain.wireframe.entity.WireframeRegenerationRequestStatus;
import com.team1.__spring_team1.domain.wireframe.repository.WireframeRegenerationRequestRepository;
import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import com.team1.__spring_team1.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WireframeRegenerationAcceptService {

    private final WireframeRegenerationRequestRepository
            wireframeRegenerationRequestRepository;

    private final ProjectPermissionService
            projectPermissionService;

    private final WireframeService wireframeService;
    private final ProjectWebSocketHandler projectWebSocketHandler;

    @Transactional
    public WireframeRegenerationAcceptResponse acceptRegenerationRequest(Long requestId, LoginUser loginUser) {
        WireframeRegenerationRequest regenerationRequest = wireframeRegenerationRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REGENERATION_REQUEST_NOT_FOUND));

        projectPermissionService.validateProjectLeader(regenerationRequest.getProjectId(), loginUser.userId());
        validatePending(regenerationRequest);

        WireframeDslResponse regeneratedWireframe = wireframeService.regenerateWireframe(
                regenerationRequest.getProjectId(),
                regenerationRequest.getScreenId(),
                regenerationRequest.getReason()
        );

        regenerationRequest.approve(loginUser.userId());

        publishEvent(
                regenerationRequest.getProjectId(),
                RealtimeEventType.WIREFRAME_UPDATED,
                loginUser.userId(),
                Map.of(
                        "screenId", regenerationRequest.getScreenId(),
                        "requestId", regenerationRequest.getId()
                )
        );

        return new WireframeRegenerationAcceptResponse(regenerationRequest.getId(), regenerationRequest.getScreenId(), regenerationRequest.getStatus().name(), "와이어프레임 재생성 요청이 승인되었습니다.", regeneratedWireframe);
    }

    private void validatePending(WireframeRegenerationRequest regenerationRequest) {
        if (regenerationRequest.getStatus() != WireframeRegenerationRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.REGENERATION_REQUEST_ALREADY_HANDLED);
        }
    }

    private void publishEvent(
            Long projectId,
            RealtimeEventType type,
            Long userId,
            Map<String, Object> payload
    ) {
        try {
            projectWebSocketHandler.publish(projectId, type, userId, payload);
        } catch (IOException e) {
            // 이벤트 전송 실패가 재생성 성공 자체를 실패시키지 않도록 한다.
        }
    }
}
