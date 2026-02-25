package com.emis.academicservice.helper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

public class ReactivePageSupport {
    private ReactivePageSupport() {
    }

    public static <T, R> Mono<Page<R>> createPage(
            Flux<T> dataFlux,
            Mono<Long> countMono,
            Pageable pageable,
            Function<T, R> mapper,
            Duration timeout) {
        return Mono.zip(
            dataFlux.collectList(),
            countMono)
        .timeout(timeout)
        .map(
            tuple -> {
                List<T> data = tuple.getT1();
                long total = tuple.getT2();

               List<R> mapped = data.stream()
                .map(mapper)
                       .toList();
               return new PageImpl<>(mapped, pageable, total);
            }
        );
    }
}
