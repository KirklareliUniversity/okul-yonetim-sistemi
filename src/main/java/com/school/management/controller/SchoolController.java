package com.school.management.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school.management.domain.Course;
import com.school.management.domain.Enrollment;
import com.school.management.domain.Grade;
import com.school.management.domain.Student;
import com.school.management.dto.CourseCreateRequest;
import com.school.management.dto.EnrollmentCreateRequest;
import com.school.management.dto.GradeCreateRequest;
import com.school.management.dto.StudentCreateRequest;
import com.school.management.service.SchoolService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class SchoolController {

    private final SchoolService schoolService;

    public SchoolController(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @GetMapping("/students")
    public List<Student> listStudents() {
        return schoolService.listStudents();
    }

    @GetMapping("/students/{studentNumber}")
    public Student getStudent(@PathVariable String studentNumber) {
        return schoolService.getStudent(studentNumber);
    }

    @PostMapping("/students")
    public Map<String, Object> createStudent(@Valid @RequestBody StudentCreateRequest request) {
        return Map.of(
                "message", "Ogrenci kaydedildi",
                "student", schoolService.createStudent(request)
        );
    }

    @GetMapping("/courses")
    public List<Course> listCourses() {
        return schoolService.listCourses();
    }

    @PostMapping("/courses")
    public Map<String, Object> createCourse(@Valid @RequestBody CourseCreateRequest request) {
        return Map.of(
                "message", "Ders tanimlandi",
                "course", schoolService.createCourse(request)
        );
    }

    @PostMapping("/enrollments")
    public Map<String, Object> enroll(@Valid @RequestBody EnrollmentCreateRequest request) {
        return Map.of(
                "message", "Derse kayit tamamlandi",
                "enrollment", schoolService.enroll(request)
        );
    }

    @GetMapping("/students/{studentNumber}/enrollments")
    public List<Enrollment> listEnrollments(@PathVariable String studentNumber) {
        return schoolService.listEnrollments(studentNumber);
    }

    @PostMapping("/grades")
    public Map<String, Object> createGrade(@Valid @RequestBody GradeCreateRequest request) {
        Grade saved = schoolService.createGrade(request);
        return Map.of("message", "Not kaydedildi", "grade", saved);
    }

    @GetMapping("/analysis/{studentNumber}")
    public Map<String, Object> analysis(@PathVariable String studentNumber) {
        return schoolService.studentAnalysis(studentNumber);
    }
}
