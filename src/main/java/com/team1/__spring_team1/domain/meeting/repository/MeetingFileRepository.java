package com.team1.__spring_team1.domain.meeting.repository;

import com.team1.__spring_team1.domain.meeting.entity.MeetingFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingFileRepository extends JpaRepository<MeetingFile, Long> {
}