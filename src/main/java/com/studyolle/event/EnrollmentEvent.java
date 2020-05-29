package com.studyolle.event;

import com.studyolle.domain.Enrollment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class EnrollmentEvent {
    protected final Enrollment enrollment;
    protected final String message;
}
