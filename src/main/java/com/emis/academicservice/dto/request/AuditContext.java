package com.emis.academicservice.dto.request;

public record AuditContext(
        String userId,
        String role,
        String source
) {}