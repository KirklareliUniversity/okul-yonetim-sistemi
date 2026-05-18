package com.school.management.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseCreateRequest(
        @NotBlank @Size(max = 32) String courseCode,
        @NotBlank @Size(max = 200) String name,
        @NotNull @Min(1) @Max(30) Integer credit
) {
}
