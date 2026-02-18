package com.emis.academicservice.utils;

import java.net.ConnectException;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.emis.academicservice.config.ServiceConfigurationProperties;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@RequiredArgsConstructor
@Slf4j
@Component
public class ClientHelper {

    private final WebClient webClient;
    private final ServiceConfigurationProperties properties;


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
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
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

    public <R> Mono<R> getRequestWithParameters(
            String url,
            Map<String, ?> pathVariables,
            Map<String, ?> queryParams,
            MultiValueMap<String, String> headers,
            Class<R> responseType) {

        Map<String, String> safePath = pathVariables != null
                ? pathVariables.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> String.valueOf(e.getValue()),
                        (a, b) -> b,
                        LinkedHashMap::new))
                : new LinkedHashMap<>();

        log.info("Making GET request to: {} with path vars: {}, query params: {}",
                url, safePath, queryParams);

        URI uri = UriComponentsBuilder
                .fromUriString(url)
                .buildAndExpand(safePath)
                .toUri();

        if (queryParams != null && !queryParams.isEmpty()) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
            queryParams.forEach((key, value) -> {
                if (value != null) {
                    builder.queryParam(key, value);
                }
            });
            uri = builder.build(true).toUri(); // true = encode
        }

        return webClient
                .get()
                .uri(uri)
                .headers(httpHeaders -> {
                    if (headers != null && !headers.isEmpty()) {
                        httpHeaders.addAll(headers);
                    }
                })
                .retrieve()
                .bodyToMono(responseType)
                .timeout(Duration.ofSeconds(properties.getTimeout()))
                .retryWhen(
                        Retry.backoff(3, Duration.ofMillis(properties.getTimeout()))
                                .filter(this::isRetryable)
                                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure())
                );
    }

    private boolean isRetryable(Throwable throwable) {
        return throwable instanceof WebClientResponseException ex
                && (ex.getStatusCode().is5xxServerError()
                || ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS
                || ex.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE
                || ex.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT)
                || throwable instanceof ConnectException
                || throwable instanceof TimeoutException
                || throwable instanceof ReadTimeoutException;
    }

}
