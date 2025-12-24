package com.emis.academicservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.NonNull;

import java.math.BigDecimal;

public record RecordAssessmentRequest(@NotBlank String schoolCode,
                                      @NonNull Long assessmentId,
                                      @NotBlank String studentNumber,
                                      @Min(0) BigDecimal scoreObtained,
                                      @Size(max = 500) String remark ) {}
