package com.emis.academicservice.service.impl;

import com.emis.academicservice.dto.response.StudentEnrollmentResponse;
import com.emis.academicservice.service.ActiveStudentEnrollmentCache;
import com.emis.academicservice.service.client.StudentClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Component
public class ActiveStudentEnrollmentCacheImpl implements ActiveStudentEnrollmentCache {

    private final StudentClientService studentClientService;
    private final Map<String, StudentEnrollmentResponse> cache = new ConcurrentHashMap<>();
  @Override
  public Mono<StudentEnrollmentResponse> getStudentEnrollmentFromCache(
      String schoolCode, String studentNumber, String academicYear) {

      StudentEnrollmentResponse cachedDetails = cache.get(studentNumber);
      if (cachedDetails != null) {
        return Mono.just(cachedDetails);
      }
      return studentClientService
          .getActiveEnrollment(studentNumber,schoolCode, academicYear)
          .flatMap(studentEnrollmentResponse -> {
            cache.put(studentNumber, studentEnrollmentResponse);
            return Mono.just(studentEnrollmentResponse);
          });
    }

    @Scheduled(fixedRate = 300000)
    public void clearCache() {
      cache.clear();
    }
}
