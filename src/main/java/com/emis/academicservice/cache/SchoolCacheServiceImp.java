package com.emis.academicservice.cache;

import com.emis.academicservice.service.client.SchoolClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Component
public class SchoolCacheServiceImp implements  SchoolCacheService {

    private final Map<String, Long> schoolCodeToIdCache = new ConcurrentHashMap<>();
    private final SchoolClientService schoolClientService;

    public Mono<Long> getSchoolIdByCode(String schoolCode) {
        Long cachedId = schoolCodeToIdCache.get(schoolCode);
        if (cachedId != null) {
            return Mono.just(cachedId);
        }

        return schoolClientService.getSchoolDetails(schoolCode)
                .flatMap(schoolDetailsResponse -> {
                    Long schoolId = schoolDetailsResponse.schoolId();
                    schoolCodeToIdCache.put(schoolCode, schoolId);
                    return Mono.just(schoolId);
                });
    }

    @Scheduled(fixedRate = 300000)
    public void clearSchoolCache() {
        schoolCodeToIdCache.clear();
    }
}
