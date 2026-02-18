package com.emis.academicservice.event.config;

import com.emis.academicservice.event.domain.DomainEvent;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
@Slf4j
public class HrEventConsumer {

    private final HrEventRouter router;

    public HrEventConsumer(HrEventRouter router) {
        this.router = router;
    }

  @Bean
  public Function<DomainEvent<JsonNode>, Mono<Void>> hrEvents() {
    return event -> {
      log.info(
          "Received HR event: type={}, id={}, correlationId={}",
          event.getEventType(),
          event.getEventId(),
          event.getCorrelationId());

      return router
          .route(event)
          .doOnError(ex -> log.error("Error processing HR event {}", event.getEventId(), ex))
          .then();
    };
}
}


