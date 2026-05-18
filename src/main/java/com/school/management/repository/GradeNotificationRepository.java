package com.school.management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school.management.domain.GradeNotification;

public interface GradeNotificationRepository extends JpaRepository<GradeNotification, Long> {

    List<GradeNotification> findByStudentNumberOrderByCreatedAtDesc(String studentNumber);
}
