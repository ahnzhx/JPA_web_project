package com.studyolle.event;

import com.studyolle.domain.Enrollment;
import org.springframework.context.ApplicationEvent;

public class EnrollmentRejectedEvent extends EnrollmentEvent {
    public EnrollmentRejectedEvent(Enrollment enrollment) {
        super(enrollment, "모임 참가 신청을 거절했습니다.");
    }
}
