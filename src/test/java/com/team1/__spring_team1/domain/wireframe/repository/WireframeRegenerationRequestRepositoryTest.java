package com.team1.__spring_team1.domain.wireframe.repository;

import com.team1.__spring_team1.domain.wireframe.entity.WireframeRegenerationRequest;
import com.team1.__spring_team1.domain.wireframe.entity.WireframeRegenerationRequestStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@ActiveProfiles({"local", "test"})
@Transactional
class WireframeRegenerationRequestRepositoryTest {

    @Autowired
    private WireframeRegenerationRequestRepository repository;

    @Test
    @DisplayName("재생성 요청을 저장하고 ID로 조회")
    void saveAndFindById() {
        // given
        WireframeRegenerationRequest request = new WireframeRegenerationRequest(1L, 10L, 100L, "와이어프레임 수정이 필요합니다.");
        WireframeRegenerationRequest savedRequest = repository.saveAndFlush(request);

        // when
        WireframeRegenerationRequest foundRequest = repository.findById(savedRequest.getId()).orElseThrow();

        // then
        Assertions.assertThat(foundRequest.getId()).isEqualTo(savedRequest.getId());
        Assertions.assertThat(foundRequest.getProjectId()).isEqualTo(1L);
        Assertions.assertThat(foundRequest.getScreenId()).isEqualTo(10L);
        Assertions.assertThat(foundRequest.getRequesterId()).isEqualTo(100L);
        Assertions.assertThat(foundRequest.getReason()).isEqualTo("와이어프레임 수정이 필요합니다.");
        Assertions.assertThat(foundRequest.getStatus()).isEqualTo(WireframeRegenerationRequestStatus.PENDING);
    }

    @Test
    @DisplayName("프로젝트ID로 재생성 요청 목록을 조회")
    void findAllByProjectId() {
        // given
        repository.save(new WireframeRegenerationRequest(1L, 10L, 100L, "첫 번째 재생성 요청"));
        repository.save(new WireframeRegenerationRequest(1L, 11L, 101L, "두 번째 재생성 요청"));
        repository.save(new WireframeRegenerationRequest(2L, 20L, 200L, "다른 프로젝트 재생성 요청"));
        repository.flush();

        // when
        List<WireframeRegenerationRequest> requests = repository.findAllByProjectId(1L);

        // then
        Assertions.assertThat(requests).hasSize(2);
        Assertions.assertThat(requests).extracting(WireframeRegenerationRequest::getProjectId).containsOnly(1L);
        Assertions.assertThat(requests).extracting(WireframeRegenerationRequest::getScreenId).containsExactlyInAnyOrder(10L, 11L);
    }

    @Test
    @DisplayName("화면에 PENDING 재생성 요청이 존재하는지 확인")
    void returnsTrueWhenPendingRequestExists() {
        // given
        repository.saveAndFlush(new WireframeRegenerationRequest(1L, 10L, 100L, "재생성 요청"));

        // when
        boolean exists = repository.existsByScreenIdAndStatus(10L, WireframeRegenerationRequestStatus.PENDING);

        // then
        Assertions.assertThat(exists).isTrue();
    }
}