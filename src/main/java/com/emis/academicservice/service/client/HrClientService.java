package com.emis.academicservice.service.client;

import reactor.core.publisher.Mono;

public interface HrClientService {
    Mono<Boolean> validateTeacherExists(Long teacherId);
}
