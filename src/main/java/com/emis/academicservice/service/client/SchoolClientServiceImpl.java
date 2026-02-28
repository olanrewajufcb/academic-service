package com.emis.academicservice.service.client;


import java.util.concurrent.ConcurrentHashMap;

import com.emis.academicservice.config.ServiceConfigurationProperties;
import com.emis.academicservice.dto.response.SchoolDetailsResponse;
import com.emis.academicservice.exception.SchoolNotFoundException;
import com.emis.academicservice.exception.ServiceException;
import com.emis.academicservice.exception.ServiceUnavailableException;
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
        var url = properties.getSchoolConfiguration().getGetSchoolDetailsUrl();
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
            err -> {
                log.error(
                        "Exception occurred while trying to get school details for schoolId: {}",
                        schoolCode,
                        err);
                if (err instanceof WebClientResponseException.NotFound ex) {
                    log.error("School with code : {} does not exist ", schoolCode, ex);
                    return new SchoolNotFoundException(
                            "School with code : " + schoolCode + " does not exist");
                } else if (err instanceof WebClientResponseException.ServiceUnavailable ex) {
                    log.error("School service unavailable: {}", schoolCode, ex);
                    return new ServiceUnavailableException(
                            "School service error: " + ex.getStatusCode(), ex.getResponseBodyAsString());
                }
                log.error("School service error:::: {}", schoolCode, err);
                return new ServiceException("School service error: ", err);
            });

    }

    @Override
    public Mono<Boolean> validateSchoolExists(Long schoolId) {
        var url = properties.getSchoolConfiguration().getValidateSchoolExistsUrl();
        var pathVariable = new ConcurrentHashMap<String, Long>();
        pathVariable.put("schoolId", schoolId);
    return client
        .getRequestWithPathVariables(url, pathVariable, ClientHelper.getHeaders(), Boolean.class)
        .map(
            response -> {
              log.info("School Details Response: {}", response);
              return response;
            })
        .onErrorMap(
            err -> {
              log.error("Error validating school existence for schoolId: {}", schoolId, err);
              if (err instanceof WebClientResponseException.NotFound ex) {
                return new SchoolNotFoundException(
                    "School not found: " + schoolId + ex.getMessage());
              } else if (err instanceof WebClientResponseException.ServiceUnavailable ex) {
                return new ServiceUnavailableException("School not found: " + schoolId, ex);
              }
              log.error("School service error:::: {}", schoolId, err);
              return new ServiceException("School service error: ", err);
            });
    }

    @Override
    public Mono<Boolean> validateSchoolExistsByCode(String schoolCode) {
        var url = properties.getSchoolConfiguration().getValidateSchoolExistsUrl();
        log.info("Logging validation url {}", url);
        var pathVariable = new ConcurrentHashMap<String, String>();
        pathVariable.put("schoolCode", schoolCode);
        return client
                .getRequestWithPathVariables(
                        url, pathVariable, ClientHelper.getHeaders(), Boolean.class)
                .map(
                        response -> {
                            log.info("School Validation Response: {}", response);
                            return response;
                        })

                .onErrorMap(err -> {
                    log.error("Error validating school existence for schoolCode: {}", schoolCode, err);
                    if (err instanceof WebClientResponseException.NotFound ex) {
                        log.error("School not found with code : {}", schoolCode, ex);
                        return new SchoolNotFoundException(
                                "School not found with the given school code: " + schoolCode);
                    } else if (err instanceof WebClientResponseException.ServiceUnavailable ex) {
                        return new ServiceUnavailableException("School not found: " + schoolCode, ex);
                    }
                    log.error("School service error:::: {}", schoolCode, err);
                    return new ServiceException("School service error ",  err);
                });
    }
}
