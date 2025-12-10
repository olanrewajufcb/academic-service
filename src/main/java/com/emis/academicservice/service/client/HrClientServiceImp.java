package com.emis.academicservice.service.client;

import com.emis.academicservice.config.ServiceConfigurationProperties;
import com.emis.academicservice.exception.SchoolNotFoundException;
import com.emis.academicservice.exception.SchoolServiceUnavailableException;
import com.emis.academicservice.utils.ClientHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Service
public class HrClientServiceImp implements HrClientService {

    private final ClientHelper client;
    private final ServiceConfigurationProperties properties;

    @Override
    public Mono<Boolean> validateTeacherExists(Long teacherId) {
        String url = properties.getHrConfiguration().getValidateTeacherExistsUrl();
        var pathVariable = new ConcurrentHashMap<String, Long>();
        pathVariable.put("teacherId", teacherId);

        return client
                .getRequestWithPathVariables(
                        url, pathVariable, ClientHelper.getHeaders(), Boolean.class)
                .map(
                        response -> {
                            log.info("Hr Details Response: {}", response);
                            return response;
                        })
                .onErrorResume(
                        SchoolServiceUnavailableException.class,
                        err -> {
                            log.warn("Service is currently not available.");
                            return Mono.error(new SchoolServiceUnavailableException("School service is unavailable", err));
                        })
                .onErrorMap(err -> {
                    log.error("Error validating teacher existence for teacherId: {}", teacherId, err);
                    return new SchoolNotFoundException("Teacher not found: " + teacherId + err.getMessage());
                });
    }

}
