package com.school.management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentCreateRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @NotBlank @Size(max = 20) String studentNumber,
        @NotBlank @Size(max = 120) String department,
        @NotBlank @Email @Size(max = 120) String email
) {
}
