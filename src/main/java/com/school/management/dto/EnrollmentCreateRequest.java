package com.school.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnrollmentCreateRequest(
        @NotBlank @Size(max = 20) String studentNumber,
        @NotBlank @Size(max = 32) String courseCode
) {
}
