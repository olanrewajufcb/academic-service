package com.emis.academicservice.cache;

import reactor.core.publisher.Mono;

public interface SchoolCacheService {

    Mono<Long> getSchoolIdByCode(String schoolCode);
}
