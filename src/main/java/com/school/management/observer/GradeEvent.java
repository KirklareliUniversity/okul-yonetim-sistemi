package com.school.management.observer;

import java.math.BigDecimal;

public record GradeEvent(
        String studentNumber,
        String courseCode,
        BigDecimal grade
) {
}
