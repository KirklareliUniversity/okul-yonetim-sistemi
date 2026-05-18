package com.school.management.domain;

import java.io.Serializable;
import java.util.Objects;

public class EnrollmentId implements Serializable {

    private String studentNumber;
    private String courseCode;

    public EnrollmentId() {
    }

    public EnrollmentId(String studentNumber, String courseCode) {
        this.studentNumber = studentNumber;
        this.courseCode = courseCode;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EnrollmentId that = (EnrollmentId) o;
        return Objects.equals(studentNumber, that.studentNumber) && Objects.equals(courseCode, that.courseCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentNumber, courseCode);
    }
}
