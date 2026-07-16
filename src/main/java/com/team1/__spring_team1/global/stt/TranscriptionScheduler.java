package com.team1.__spring_team1.global.stt;

import com.team1.__spring_team1.domain.meeting.entity.MeetingFile;
import com.team1.__spring_team1.domain.meeting.entity.MeetingFileStatus;
import com.team1.__spring_team1.domain.meeting.repository.MeetingFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.transcribe.model.TranscriptionJob;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
//STT에 주기적 상태 확인 스케줄러. 추후 SNS + SQS 구독 방식으로 AWS 리소스에서 먼저 변환 성공 알려주는 방식으로 전환 가능
public class TranscriptionScheduler {

    private final MeetingFileRepository meetingFileRepository;
    private final TranscribeStatusChecker transcribeStatusChecker;

    @Scheduled(fixedDelay = 10000) // 10초마다 실행
    @Transactional
    public void checkTranscriptionJobs() {
        List<MeetingFile> transcribingFiles =
                meetingFileRepository.findAllByStatus(MeetingFileStatus.TRANSCRIBING);

        for (MeetingFile file : transcribingFiles) {
            checkAndUpdate(file);
        }
    }

    private void checkAndUpdate(MeetingFile file) {
        try {
            TranscriptionJob job = transcribeStatusChecker.getJobStatus(file.getId());

            if (transcribeStatusChecker.isCompleted(job)) {
                String transcript = transcribeStatusChecker.readTranscriptText(file.getId());
                file.completeTranscription(transcript);
                log.info("STT 완료, meetingFileId={}", file.getId());
            } else if (transcribeStatusChecker.isFailed(job)) {
                file.fail("STT 변환에 실패했습니다.");
                log.warn("STT 실패, meetingFileId={}", file.getId());
            }
            // IN_PROGRESS, QUEUED면 아무것도 안 하고 다음 스케줄에서 다시 확인

        } catch (Exception e) {
            log.error("STT 상태 확인 중 오류, meetingFileId={}", file.getId(), e);
        }
    }
}