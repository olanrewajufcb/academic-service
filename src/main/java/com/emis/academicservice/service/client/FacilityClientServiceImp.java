package com.emis.academicservice.service.client;

import com.emis.academicservice.config.ServiceConfigurationProperties;
import com.emis.academicservice.exception.ResourceNotFoundException;
import com.emis.academicservice.exception.ServiceUnavailableException;
import com.emis.academicservice.utils.ClientHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Service
public class FacilityClientServiceImp implements FacilityClientService {

    private final ClientHelper client;
    private final ServiceConfigurationProperties properties;

    @Override
    public Mono<Boolean> validateFacility(Long facilityId) {
        String url = properties.getFacilityConfiguration().getValidateFacilityExistsUrl();
        var pathVariable = new ConcurrentHashMap<String, Long>();
        pathVariable.put("facilityId", facilityId);

    return client
        .getRequestWithPathVariables(url, pathVariable, ClientHelper.getHeaders(), Boolean.class)
        .map(
            response -> {
              log.info("Facility Service Response: {}", response);
              return response;
            })
        .onErrorResume(ex -> {
            if (ex instanceof WebClientResponseException.NotFound err) {
                 return Mono.error(new ResourceNotFoundException
                         ("Facility not found :::" + err.getStatusCode().value()));
                 }
                else if (ex instanceof WebClientResponseException.ServiceUnavailable) {
                    return Mono.error(new ServiceUnavailableException("Facility Service is unavailable", ex));
                }
           return Mono.error(ex);
            });
    }

}
