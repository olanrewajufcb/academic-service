package com.emis.academicservice.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
@Component
public class ClientHelper {

    private final WebClient webClient;

    public <R> Mono<R> getRequestWithPathVariables(String url,
                                                   Map<String, ?> pathVariables,
                                                   MultiValueMap<String, String> headers,
                                                   Class<R> responseType){
        Map<String, String> safePath = pathVariables.entrySet().stream()
                        .filter(e -> e.getValue() != null)
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                        e -> String.valueOf(e.getValue()),
        (a, b) -> b, LinkedHashMap::new));

        log.info("Making request to: {} with path vars: {}", url, safePath);

        return webClient
                .get()
                .uri(url, safePath)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .bodyToMono(responseType);

    }
    public static MultiValueMap<String, String> getHeaders(){
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(1);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    public <R> Flux<R> getRequestWithPathVariablesFlux(String url,
                                                   Map<String, ?> pathVariables,
                                                   MultiValueMap<String, String> headers,
                                                   Class<R> responseType){
        Map<String, String> safePath = pathVariables.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> String.valueOf(e.getValue()),
                        (a, b) -> b, LinkedHashMap::new));

        log.info("Making request to: {} with path vars: {}", url, safePath);

        return webClient
                .get()
                .uri(url, safePath)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .bodyToFlux(responseType);

    }

    public <T> Flux<T> post(String url, Object body, MultiValueMap<String, String> headers, Class<T> responseType) {

        return webClient.post()
                .uri(url)
                .headers(h -> h.addAll(headers))
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(responseType);
    }

}
