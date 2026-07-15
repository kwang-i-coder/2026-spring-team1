package com.team1.__spring_team1.domain.wireframe.entity;

import com.team1.__spring_team1.global.exception.BusinessException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WireframeRepositoryTest {

    @Test
    @DisplayName("이미 승인된 요청은 다시 승인할 수 없다")
    void cannotApproveAlreadyApprovedRequest() {
        // given
        WireframeRegenerationRequest request = new WireframeRegenerationRequest(1L, 2L, 3L, "화면 수정이 필요합니다.");

        request.approve(10L);

        // when & then
        Assertions.assertThatThrownBy(() -> request.approve(11L)).isInstanceOf(BusinessException.class);
    }
}
