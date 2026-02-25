package com.emis.academicservice.event.config;

import com.emis.academicservice.event.domain.DomainEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
@Slf4j
public class HrEventConsumer {

    private final HrEventRouter router;
    private final ObjectMapper objectMapper;

    public HrEventConsumer(HrEventRouter router, ObjectMapper objectMapper) {
        this.router = router;
        this.objectMapper = objectMapper;
    }

  @Bean
  public Consumer<Flux<Message<String>>> hrEvents() {
    return flux -> flux.concatMap(message -> {
        try {
            String payload = message.getPayload();
            DomainEvent<JsonNode> event = objectMapper.readValue(payload, new TypeReference<>() {});
            log.info("Received HR event type::::: {}", event.getEventType());
            log.info("Received HR event data::::: {}", event.getData());
            return router.route(event)
                .doOnSubscribe(s -> log.info("Subscribed to the Mono returned from router.route for eventId: {}", event.getEventId()))
                .doOnTerminate(() -> log.info("Finished execution of the Mono from router.route for eventId: {}", event.getEventId()))
                .onErrorResume(e -> {
                    log.error("Error processing eventId: {}", event.getEventId(), e);
                    return Mono.empty();
                });
        } catch (Exception e) {
            log.error("Failed to deserialize HR event", e);
            return Mono.empty();
        }
    }).subscribe(
        null,
        e -> log.error("Error in hrEvents flux", e),
        () -> log.info("hrEvents flux completed")
    );
}
}


