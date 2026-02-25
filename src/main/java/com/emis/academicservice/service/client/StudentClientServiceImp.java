package com.emis.academicservice.service.client;

import com.emis.academicservice.config.ServiceConfigurationProperties;
import com.emis.academicservice.dto.response.StudentDetailsResponse;
import com.emis.academicservice.dto.response.StudentEnrollmentResponse;
import com.emis.academicservice.exception.SchoolServiceUnavailableException;
import com.emis.academicservice.exception.ResourceNotFoundException;
import com.emis.academicservice.exception.StudentServiceException;
import com.emis.academicservice.utils.ClientHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Slf4j
@Service
public class StudentClientServiceImp implements StudentClientService {

    private final ClientHelper client;
    private final ServiceConfigurationProperties properties;

    @Override
    public Mono<StudentDetailsResponse> getStudentDetails(String studentNumber, String schoolCode) {
        var url = properties.getStudentConfiguration().getGetStudentDetailsUrl();
        var pathVariable = new ConcurrentHashMap<String, String>();
        pathVariable.put("studentNumber", studentNumber);
        pathVariable.put("schoolCode", schoolCode);
    return client
        .getRequestWithPathVariables(
            url, pathVariable, ClientHelper.getHeaders(), StudentDetailsResponse.class)
        .map(
            response -> {
              log.info("Student Details Response: {}", response);
              return response;
            })
        .onErrorMap(
            WebClientResponseException.NotFound.class,
            err -> {
              log.error(
                  "Exception occurred while trying to get school details for schoolId: {}",
                  studentNumber,
                  err);
              return new ResourceNotFoundException("Student not found for : " + studentNumber);
            })
        .onErrorMap(
            WebClientResponseException.class,
            err -> {
              log.error(
                  "Exception occurred while trying to get student details for schoolId: {}",
                  studentNumber,
                  err);
              return new SchoolServiceUnavailableException(
                  "Student service error: " + err.getStatusCode(), err.getResponseBodyAsString());
            })
        .onErrorMap(
            Exception.class,
            err -> {
              log.error(
                  "Unexpected exception occurred while trying to get student details for schoolId: {}",
                  studentNumber,
                  err);
              return new SchoolServiceUnavailableException(
                  "Unexpected error occurred while fetching student details" + err.getMessage(),
                  err);
            });
    }

    @Override
    public Flux<StudentDetailsResponse> getStudentDetails(Long studentId) {
        var url = properties.getStudentConfiguration().getGetStudentDetailsUrl();
        var pathVariable = new ConcurrentHashMap<String, Long>();
        pathVariable.put("studentId", studentId);
        return client
                .getRequestWithPathVariablesFlux(
                        url, pathVariable, ClientHelper.getHeaders(), StudentDetailsResponse.class)
                .map(
                        response -> {
                            log.info("Student Details Response: {}", response);
                            return response;
                        })
                .onErrorMap(
                        WebClientResponseException.NotFound.class,
                        err -> {
                            log.error(
                                    "Exception occurred while trying to get school details for schoolId: {}",
                                    studentId,
                                    err);
                            return new ResourceNotFoundException("Student not found for : " + studentId);
                        })
                .onErrorMap(
                        WebClientResponseException.class,
                        err -> {
                            log.error(
                                    "Exception occurred while trying to get student details for schoolId: {}",
                                    studentId,
                                    err);
                            return new SchoolServiceUnavailableException(
                                    "Student service error: " + err.getStatusCode(), err.getResponseBodyAsString());
                        })
                .onErrorMap(
                        Exception.class,
                        err -> {
                            log.error(
                                    "Unexpected exception occurred while trying to get student details for schoolId: {}",
                                    studentId,
                                    err);
                            return new SchoolServiceUnavailableException(
                                    "Unexpected error occurred while fetching student details" + err.getMessage(),
                                    err);
                        });
    }

    @Override
    public Flux<StudentDetailsResponse> getStudentDetailsBatch(List<Long> studentIds) {
        String url = properties.getStudentConfiguration().getBatchStudentDetailsUrl();
        return client.post(url,studentIds, ClientHelper.getHeaders(), StudentDetailsResponse.class)
                .map(response -> {
                    log.info("Batch Student Details Response: {}", response);
                    return response;
                });
    }

    @Override
    public Mono<StudentDetailsResponse> getStudentByIdAndSchoolId(Long studentId, String schoolId) {
        var url = properties.getStudentConfiguration().getGetStudentDetailsUrl();
        var pathVariable = new ConcurrentHashMap<String, String>();
        pathVariable.put("studentId", String.valueOf(studentId));
        pathVariable.put("schoolId", schoolId);
        return client
                .getRequestWithPathVariables(
                        url, pathVariable, ClientHelper.getHeaders(), StudentDetailsResponse.class)
                .map(
                        response -> {
                            log.info("Student Details Response: {}", response);
                            return response;
                        })
                .onErrorMap(
                        WebClientResponseException.NotFound.class,
                        err -> {
                            log.error(
                                    "Exception occurred while trying to get school details for schoolId: {}",
                                    studentId,
                                    err);
                            return new ResourceNotFoundException("Student not found for : " + studentId);
                        })
                .onErrorMap(
                        WebClientResponseException.class,
                        err -> {
                            log.error(
                                    "Exception occurred while trying to get student details for schoolId: {}",
                                    studentId,
                                    err);
                            return new SchoolServiceUnavailableException(
                                    "Student service error: " + err.getStatusCode(), err.getResponseBodyAsString());
                        })
                .onErrorMap(
                        Exception.class,
                        err -> {
                            log.error(
                                    "Unexpected exception occurred while trying to get student details for schoolId: {}",
                                    studentId,
                                    err);
                            return new SchoolServiceUnavailableException(
                                    "Unexpected error occurred while fetching student details" + err.getMessage(),
                                    err);
                        });    }

    @Override
    public Mono<StudentEnrollmentResponse> getActiveEnrollment(String studentNumber,
                                                               String schoolCode,
                                                               String academicYear) {

            var url = properties.getStudentConfiguration().getGetActiveEnrollmentUrl();
            var pathVariable = new ConcurrentHashMap<String, String>();
            var queryParams = new ConcurrentHashMap<String, String>();
            pathVariable.put("studentNumber", studentNumber);
            queryParams.put("schoolCode", schoolCode);
            queryParams.put("academicYear", academicYear);

            return client
                    .getRequestWithParameters(
                            url,
                            pathVariable,
                            queryParams,
                            ClientHelper.getHeaders(),
                            StudentEnrollmentResponse.class)
                    .map(
                            response -> {
                                log.info("student enrollment Response: {}", response);
                                return response;
                            })
                    .onErrorMap(
                            err -> {
                                log.error(
                                        "Exception occurred while trying to get active student enrollment: {}",
                                        schoolCode,
                                        err);
                                if (err instanceof WebClientResponseException.NotFound ex) {
                                    return new ResourceNotFoundException(
                                            "active student enrollment not found: " + studentNumber + ex.getMessage());
                                } else if (err instanceof WebClientResponseException.ServiceUnavailable ex) {
                                    log.error("Student service unavailable: {}", schoolCode, ex);
                                    return new SchoolServiceUnavailableException(
                                            "Student service error: " + ex.getStatusCode(), ex.getResponseBodyAsString());
                                }

                                return new StudentServiceException("Student service error: ", err);
                            });
        }
    }

