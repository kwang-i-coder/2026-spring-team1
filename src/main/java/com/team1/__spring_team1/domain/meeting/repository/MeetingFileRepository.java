package com.team1.__spring_team1.domain.meeting.repository;

import com.team1.__spring_team1.domain.meeting.entity.MeetingFile;
import com.team1.__spring_team1.domain.meeting.entity.MeetingFileStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingFileRepository extends JpaRepository<MeetingFile, Long> {
    List<MeetingFile> findAllByStatus(MeetingFileStatus status);
}