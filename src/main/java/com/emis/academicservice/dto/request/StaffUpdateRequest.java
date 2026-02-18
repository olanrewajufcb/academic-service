package com.emis.academicservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StaffUpdateRequest(
        @NotNull
        Long staffId,
        @NotBlank
        String staffCode,
        @NotBlank
        String name) {
}
