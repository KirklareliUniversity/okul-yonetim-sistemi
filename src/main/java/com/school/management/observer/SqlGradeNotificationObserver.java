package com.school.management.observer;

import org.springframework.stereotype.Component;

import com.school.management.domain.GradeNotification;
import com.school.management.repository.GradeNotificationRepository;

@Component
public class SqlGradeNotificationObserver implements GradeObserver {

    private final GradeNotificationRepository repository;

    public SqlGradeNotificationObserver(GradeNotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void onGradeAdded(GradeEvent event) {
        GradeNotification row = new GradeNotification();
        row.setStudentNumber(event.studentNumber());
        row.setCourseCode(event.courseCode());
        row.setGrade(event.grade());
        row.setMessage(String.format(
                "Ogrenci %s dersi %s icin not aldi: %s",
                event.studentNumber(),
                event.courseCode(),
                event.grade()
        ));
        repository.save(row);
    }
}
