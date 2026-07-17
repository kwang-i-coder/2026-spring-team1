package com.team1.__spring_team1.domain.wireframe.entity;

import org.assertj.core.api.Assert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WireframeRegenerationRequestTest {

    @Test
    @DisplayName("재생성 요청은 PENDING 상태로 생성")
    void pending() {
        // given
        // 생성 자체가 given

        // when
        WireframeRegenerationRequest request = new WireframeRegenerationRequest(1L, 2L, 3L, "화면 수정이 필요합니다.");

        // then
        Assertions.assertThat(request.getProjectId()).isEqualTo(1L);
        Assertions.assertThat(request.getScreenId()).isEqualTo(2L);
        Assertions.assertThat(request.getRequesterId()).isEqualTo(3L);
        Assertions.assertThat(request.getReason()).isEqualTo("화면 수정이 필요합니다.");
        Assertions.assertThat(request.getStatus()).isEqualTo(WireframeRegenerationRequestStatus.PENDING);
        Assertions.assertThat(request.getReviewerId()).isNull();
        Assertions.assertThat(request.getReviewedAt()).isNull();
    }

    @Test
    @DisplayName("대기중인 재생성 요청을 승인할 수 있다")
    void accept() {
        // given
        WireframeRegenerationRequest request = new WireframeRegenerationRequest(1L, 2L, 3L, "화면 수정이 필요합니다.");

        // when
        request.approve(10L);

        // then
        Assertions.assertThat(request.getStatus()).isEqualTo(WireframeRegenerationRequestStatus.APPROVED);
        Assertions.assertThat(request.getReviewerId()).isEqualTo(10L);
        Assertions.assertThat(request.getReviewedAt()).isNotNull();
    }

    @Test
    @DisplayName("대기중인 재생성 요청을 거절할 수 있다")
    void reject() {
        // given
        WireframeRegenerationRequest request = new WireframeRegenerationRequest(1L, 2L, 3L, "화면 수정이 필요합니다");

        // when
        request.reject(10L);

        // then
        Assertions.assertThat(request.getStatus()).isEqualTo(WireframeRegenerationRequestStatus.REJECTED);
        Assertions.assertThat(request.getReviewerId()).isEqualTo(10L);
        Assertions.assertThat(request.getReviewedAt()).isNotNull();
    }
}