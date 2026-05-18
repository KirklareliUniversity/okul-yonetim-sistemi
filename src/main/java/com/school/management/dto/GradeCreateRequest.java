package com.school.management.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GradeCreateRequest(
        @NotBlank @Size(max = 20) String studentNumber,
        @NotBlank @Size(max = 32) String courseCode,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal grade
) {
}
