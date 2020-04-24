package com.studyolle.study.event;

import com.studyolle.domain.Study;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StudyUpdatedEvent {

    private final Study study;

    private final String message;
}
