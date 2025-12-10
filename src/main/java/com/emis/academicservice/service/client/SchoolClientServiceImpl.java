package com.emis.academicservice.service.client;


import java.util.concurrent.ConcurrentHashMap;

import com.emis.academicservice.config.ServiceConfigurationProperties;
import com.emis.academicservice.dto.response.SchoolDetailsResponse;
import com.emis.academicservice.exception.SchoolNotFoundException;
import com.emis.academicservice.exception.SchoolServiceUnavailableException;
import com.emis.academicservice.utils.ClientHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchoolClientServiceImpl implements SchoolClientService {

    private final ClientHelper client;
    private final ServiceConfigurationProperties properties;

    @Override
    public Mono<SchoolDetailsResponse> getSchoolDetails(String schoolCode) {
        var url = properties.getConfiguration().getGetSchoolDetailsUrl();
        var pathVariable = new ConcurrentHashMap<String, String>();
        pathVariable.put("schoolCode", schoolCode);
    return client
        .getRequestWithPathVariables(
            url, pathVariable, ClientHelper.getHeaders(), SchoolDetailsResponse.class)
        .map(
            response -> {
              log.info("School Details Response: {}", response);
              return response;
            })
        .onErrorMap(
            WebClientResponseException.NotFound.class,
            err -> {
              log.error(
                  "Exception occurred while trying to get school details for schoolId: {}",
                  schoolCode,
                  err);
              return new SchoolNotFoundException("School not found: " + schoolCode);
            })
        .onErrorMap(
            WebClientResponseException.class,
            err -> {
              log.error(
                  "Exception occurred while trying to get school details for schoolId: {}",
                  schoolCode,
                  err);
              return new SchoolServiceUnavailableException(
                  "School service error: " + err.getStatusCode(), err.getResponseBodyAsString());
            })
        .onErrorMap(
            Exception.class,
            err -> {
              log.error(
                  "Unexpected exception occurred while trying to get school details for schoolId: {}",
                  schoolCode,
                  err);
              return new SchoolServiceUnavailableException(
                  "Unexpected error occurred while fetching school details" + err.getMessage(),
                  err);
            });
    }

    @Override
    public Mono<Boolean> validateSchoolExists(Long schoolId) {
        var url = properties.getConfiguration().getValidateSchoolExistsUrl();
        var pathVariable = new ConcurrentHashMap<String, Long>();
        pathVariable.put("schoolId", schoolId);
        return client
                .getRequestWithPathVariables(
                        url, pathVariable, ClientHelper.getHeaders(), Boolean.class)
                .map(
                        response -> {
                            log.info("School Details Response: {}", response);
                            return response;
                        })
         .onErrorResume(
             SchoolServiceUnavailableException.class,
             err -> {
               log.warn("Service is currently not available.");
               return Mono.error(new SchoolServiceUnavailableException("School service is unavailable", err));
             })
             .onErrorMap(err -> {
               log.error("Error validating school existence for schoolId: {}", schoolId, err);
               return new SchoolNotFoundException("School not found: " + schoolId + err.getMessage());
             });
    }

    @Override
    public Mono<Boolean> validateSchoolExistsByCode(String schoolCode) {
        var url = properties.getConfiguration().getValidateSchoolExistsUrl();
        var pathVariable = new ConcurrentHashMap<String, String>();
        pathVariable.put("schoolCode", schoolCode);
        return client
                .getRequestWithPathVariables(
                        url, pathVariable, ClientHelper.getHeaders(), Boolean.class)
                .map(
                        response -> {
                            log.info("School Details Response: {}", response);
                            return response;
                        })
                .onErrorResume(
                        SchoolServiceUnavailableException.class,
                        err -> {
                            log.warn("Service is currently not available.");
                            return Mono.error(new SchoolServiceUnavailableException("School service is unavailable", err));
                        })
                .onErrorMap(err -> {
                    log.error("Error validating school existence for schoolId: {}", schoolCode, err);
                    return new SchoolNotFoundException("School not found: " + schoolCode + err.getMessage());
                });
    }
}
