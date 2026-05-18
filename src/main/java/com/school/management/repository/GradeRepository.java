package com.school.management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school.management.domain.Grade;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByStudentNumberOrderByCreatedAtDesc(String studentNumber);

    List<Grade> findByStudentNumberOrderByCreatedAtAsc(String studentNumber);
}
