package com.emis.academicservice.dto.request;

import com.emis.academicservice.enums.ProgressionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StudentPromotionRequest(
        @NotBlank(message = "studentNumber is required")
        String academicYear,
        @NotNull(message = "currentClassId is required")
        Long currentClassId,
        @NotNull(message = "nextClassId is required")
        Long nextClassId,
        String className,
        @NotBlank(message = "schoolCode is required")
        String schoolCode,
        ProgressionStatus progressionStatus,
        String remarks
) {}