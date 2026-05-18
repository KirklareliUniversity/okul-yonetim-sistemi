package com.school.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school.management.domain.Course;

public interface CourseRepository extends JpaRepository<Course, String> {
}
