package com.emis.academicservice.service.client;

import com.emis.academicservice.dto.response.SchoolDetailsResponse;
import reactor.core.publisher.Mono;

public interface SchoolClientService {

    Mono<SchoolDetailsResponse> getSchoolDetails(String schoolCode);

    Mono<Boolean> validateSchoolExists(Long schoolId);

    Mono<Boolean> validateSchoolExistsByCode(String schoolCode);
}
