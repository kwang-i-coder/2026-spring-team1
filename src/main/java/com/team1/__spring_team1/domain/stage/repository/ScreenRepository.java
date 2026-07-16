package com.team1.__spring_team1.domain.stage.repository;

import com.team1.__spring_team1.domain.stage.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenRepository extends JpaRepository<Screen, Long> {

    //SCREEN_SPEC confirm 시점에 추출된 화면 목록 조회
    // screen_order 기준 오름차순 정렬
    // 와이어프레임에서 screen_id 참조시에도 활용 가능
    List<Screen> findByStageDocumentIdOrderByScreenOrder (Long stageDocumentId);
}
