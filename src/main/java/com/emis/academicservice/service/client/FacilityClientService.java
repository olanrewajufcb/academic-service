package com.emis.academicservice.service.client;

import reactor.core.publisher.Mono;

public interface FacilityClientService {
    Mono<Boolean> validateFacility(Long facilityId);
}
