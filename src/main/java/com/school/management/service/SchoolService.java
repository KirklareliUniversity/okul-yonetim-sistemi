package com.school.management.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.management.domain.Course;
import com.school.management.domain.Enrollment;
import com.school.management.domain.Grade;
import com.school.management.domain.GradeNotification;
import com.school.management.domain.Student;
import com.school.management.dto.CourseCreateRequest;
import com.school.management.dto.EnrollmentCreateRequest;
import com.school.management.dto.GradeCreateRequest;
import com.school.management.dto.StudentCreateRequest;
import com.school.management.exception.BadRequestException;
import com.school.management.exception.ResourceNotFoundException;
import com.school.management.observer.GradeEvent;
import com.school.management.observer.GradeSubject;
import com.school.management.repository.CourseRepository;
import com.school.management.repository.EnrollmentRepository;
import com.school.management.repository.GradeNotificationRepository;
import com.school.management.repository.GradeRepository;
import com.school.management.repository.StudentRepository;

@Service
public class SchoolService {

    private static final DateTimeFormatter CHART_TIME = DateTimeFormatter.ofPattern("dd.MM HH:mm");

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final GradeNotificationRepository gradeNotificationRepository;
    private final GradeSubject gradeSubject;

    public SchoolService(
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            GradeRepository gradeRepository,
            GradeNotificationRepository gradeNotificationRepository,
            GradeSubject gradeSubject
    ) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.gradeRepository = gradeRepository;
        this.gradeNotificationRepository = gradeNotificationRepository;
        this.gradeSubject = gradeSubject;
    }

    public List<Student> listStudents() {
        return studentRepository.findAll();
    }

    public Student getStudent(String studentNumber) {
        return studentRepository.findById(studentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ogrenci bulunamadi: " + studentNumber));
    }

    @Transactional
    public Student createStudent(StudentCreateRequest request) {
        if (studentRepository.existsById(request.studentNumber())) {
            throw new BadRequestException("Bu ogrenci numarasi zaten kayitli: " + request.studentNumber());
        }
        if (studentRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("Bu e-posta adresi baska bir ogrenciye ait.");
        }
        Student student = new Student.Builder()
                .studentNumber(request.studentNumber())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .department(request.department())
                .email(request.email())
                .build();
        return studentRepository.save(student);
    }

    public List<Course> listCourses() {
        return courseRepository.findAll();
    }

    @Transactional
    public Course createCourse(CourseCreateRequest request) {
        if (courseRepository.existsById(request.courseCode())) {
            throw new BadRequestException("Ders kodu zaten tanimli: " + request.courseCode());
        }
        Course course = new Course(request.courseCode(), request.name(), request.credit());
        return courseRepository.save(course);
    }

    @Transactional
    public Enrollment enroll(EnrollmentCreateRequest request) {
        if (!studentRepository.existsById(request.studentNumber())) {
            throw new ResourceNotFoundException("Ogrenci bulunamadi: " + request.studentNumber());
        }
        if (!courseRepository.existsById(request.courseCode())) {
            throw new ResourceNotFoundException("Ders bulunamadi: " + request.courseCode());
        }
        if (enrollmentRepository.existsByStudentNumberAndCourseCode(request.studentNumber(), request.courseCode())) {
            throw new BadRequestException("Ogrenci bu derse zaten kayitli.");
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentNumber(request.studentNumber());
        enrollment.setCourseCode(request.courseCode());
        return enrollmentRepository.save(enrollment);
    }

    public List<Enrollment> listEnrollments(String studentNumber) {
        if (!studentRepository.existsById(studentNumber)) {
            throw new ResourceNotFoundException("Ogrenci bulunamadi: " + studentNumber);
        }
        return enrollmentRepository.findByStudentNumberOrderByEnrolledAtDesc(studentNumber);
    }

    @Transactional
    public Grade createGrade(GradeCreateRequest request) {
        if (!studentRepository.existsById(request.studentNumber())) {
            throw new ResourceNotFoundException("Ogrenci bulunamadi: " + request.studentNumber());
        }
        if (!courseRepository.existsById(request.courseCode())) {
            throw new ResourceNotFoundException("Ders bulunamadi: " + request.courseCode());
        }
        if (!enrollmentRepository.existsByStudentNumberAndCourseCode(request.studentNumber(), request.courseCode())) {
            throw new BadRequestException("Not girebilmek icin ogrencinin derse kayitli olmasi gerekir.");
        }
        Grade grade = new Grade();
        grade.setStudentNumber(request.studentNumber());
        grade.setCourseCode(request.courseCode());
        grade.setGrade(request.grade());
        Grade saved = gradeRepository.save(grade);
        gradeSubject.notifyGradeAdded(
                new GradeEvent(request.studentNumber(), request.courseCode(), request.grade())
        );
        return saved;
    }

    public Map<String, Object> studentAnalysis(String studentNumber) {
        Student student = studentRepository.findById(studentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ogrenci bulunamadi: " + studentNumber));

        List<Grade> gradesDesc = gradeRepository.findByStudentNumberOrderByCreatedAtDesc(studentNumber);
        List<Grade> gradesAsc = gradeRepository.findByStudentNumberOrderByCreatedAtAsc(studentNumber);
        List<GradeNotification> notifications = gradeNotificationRepository.findByStudentNumberOrderByCreatedAtDesc(
                studentNumber
        );
        List<Enrollment> enrollments = enrollmentRepository.findByStudentNumberOrderByEnrolledAtDesc(studentNumber);

        double simpleAverage = gradesDesc.stream()
                .map(Grade::getGrade)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        Map<String, Grade> latestByCourse = new LinkedHashMap<>();
        for (Grade g : gradesDesc) {
            latestByCourse.putIfAbsent(g.getCourseCode(), g);
        }

        List<Map<String, Object>> courseInsights = new ArrayList<>();
        BigDecimal weightedSum = BigDecimal.ZERO;
        int creditSum = 0;

        for (Map.Entry<String, Grade> e : latestByCourse.entrySet()) {
            String code = e.getKey();
            Grade latest = e.getValue();
            double gval = latest.getGrade().doubleValue();
            Optional<Course> courseOpt = courseRepository.findById(code);
            int credit = courseOpt.map(Course::getCredit).orElse(0);
            String name = courseOpt.map(Course::getName).orElse("?");
            String letter = toLetterGrade(gval);

            Map<String, Object> row = new HashMap<>();
            row.put("courseCode", code);
            row.put("courseName", name);
            row.put("credit", credit);
            row.put("latestGrade", Math.round(gval * 100.0) / 100.0);
            row.put("letterGrade", letter);
            courseInsights.add(row);

            if (credit > 0) {
                weightedSum = weightedSum.add(
                        latest.getGrade().multiply(BigDecimal.valueOf(credit))
                );
                creditSum += credit;
            }
        }

        double creditWeightedAverage = 0.0;
        if (creditSum > 0) {
            creditWeightedAverage = weightedSum
                    .divide(BigDecimal.valueOf(creditSum), 4, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        List<String> missingGrades = new ArrayList<>();
        for (Enrollment en : enrollments) {
            if (!latestByCourse.containsKey(en.getCourseCode())) {
                missingGrades.add(en.getCourseCode());
            }
        }

        List<String> chartLabels = new ArrayList<>();
        List<Double> chartValues = new ArrayList<>();
        for (Grade g : gradesAsc) {
            chartLabels.add(g.getCreatedAt() == null ? "" : CHART_TIME.format(g.getCreatedAt()));
            chartValues.add(g.getGrade().doubleValue());
        }

        String riskLevel;
        if (gradesDesc.isEmpty()) {
            riskLevel = "not_yok";
        } else if (creditSum > 0) {
            riskLevel = creditWeightedAverage >= 60.0 ? "normal" : "riskli";
        } else {
            riskLevel = simpleAverage >= 60.0 ? "normal" : "riskli";
        }

        Map<String, Object> response = new HashMap<>();
        response.put("studentNumber", studentNumber);
        response.put("studentName", student.getFirstName() + " " + student.getLastName());
        response.put("department", student.getDepartment());
        response.put("gradeCount", gradesDesc.size());
        response.put("enrollmentCount", enrollments.size());
        response.put("averageGrade", Math.round(simpleAverage * 100.0) / 100.0);
        response.put("creditWeightedAverage", Math.round(creditWeightedAverage * 100.0) / 100.0);
        response.put("totalCreditsInAverage", creditSum);
        response.put("courseInsights", courseInsights);
        response.put("coursesWithoutGrade", missingGrades);
        response.put("riskLevel", riskLevel);
        response.put("grades", gradesDesc);
        response.put("gradeNotifications", notifications);
        response.put("gradeChart", Map.of(
                "labels", chartLabels,
                "values", chartValues
        ));
        return response;
    }

    private static String toLetterGrade(double value) {
        if (value >= 90) {
            return "AA";
        }
        if (value >= 85) {
            return "BA";
        }
        if (value >= 80) {
            return "BB";
        }
        if (value >= 75) {
            return "CB";
        }
        if (value >= 70) {
            return "CC";
        }
        if (value >= 65) {
            return "DC";
        }
        if (value >= 60) {
            return "DD";
        }
        return "FF";
    }
}
