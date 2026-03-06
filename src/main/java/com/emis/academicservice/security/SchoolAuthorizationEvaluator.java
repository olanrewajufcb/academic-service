package com.emis.academicservice.security;

import com.emis.academicservice.enums.ResourceAction;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component("schoolAuth")
@RequiredArgsConstructor
public class SchoolAuthorizationEvaluator {

    private final ActorContextFactory actorContextFactory;
    private final AuthorizationPolicy policy;

    public Mono<Boolean> authorize(Authentication authentication,
                                   String schoolCode, ResourceAction action) {
        return actorContextFactory
                .fromAuthentication(authentication)
                .flatMap(actor -> policy
                        .isAuthorized(actor, schoolCode, action));
    }
}