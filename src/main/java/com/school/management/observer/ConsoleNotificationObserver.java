package com.school.management.observer;

import org.springframework.stereotype.Component;

@Component
public class ConsoleNotificationObserver implements GradeObserver {
    @Override
    public void onGradeAdded(GradeEvent event) {
        System.out.printf(
                "[BILDIRIM] Ogrenci %s icin %s notu girildi: %s%n",
                event.studentNumber(),
                event.courseCode(),
                event.grade()
        );
    }
}
