package com.school.management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school.management.domain.Enrollment;
import com.school.management.domain.EnrollmentId;

public interface EnrollmentRepository extends JpaRepository<Enrollment, EnrollmentId> {

    List<Enrollment> findByStudentNumberOrderByEnrolledAtDesc(String studentNumber);

    boolean existsByStudentNumberAndCourseCode(String studentNumber, String courseCode);
}
