package com.team1.__spring_team1.global.s3;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum S3Directory {

    MEETING_FILE("meeting-file");

    private final String path;
}