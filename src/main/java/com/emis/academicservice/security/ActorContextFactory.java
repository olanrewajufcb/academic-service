package com.emis.academicservice.security;




import java.util.*;

import com.emis.academicservice.enums.ActorType;
import com.emis.academicservice.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActorContextFactory {

    public Mono<ActorContext> fromAuthentication(Authentication authentication) {

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            return Mono.error(new IllegalStateException("Invalid authentication type"));
        }

        Jwt jwt = jwtAuth.getToken();

        return Mono.defer(() -> {

            String username = jwt.getClaimAsString("preferred_username");

            boolean isService = isServiceToken(jwt);

            if (isService) {
                return buildServiceActor(jwt, username);
            } else {
                return buildUserActor(jwt, username);
            }
        });
    }

    private boolean isServiceToken(Jwt jwt) {
        String preferredUsername = jwt.getClaimAsString("preferred_username");

        return preferredUsername != null &&
                         preferredUsername.startsWith("service-account");
    }

    private Mono<ActorContext> buildUserActor(Jwt jwt, String username) {

        return Mono.just(
                ActorContext.builder()
                        .type(ActorType.USER)
                        .username(username)
                        .schoolCode(extractSchoolCode(jwt))
                        .email(jwt.getClaimAsString("email"))
                        .userRoles(extractRealmRoles(jwt))
                        .build()
        );
    }

    private String extractSchoolCode(Jwt jwt) {
        if (jwt.hasClaim("schoolCode")) {
            return jwt.getClaimAsString("schoolCode");
        }
        return jwt.getClaimAsString("school_code");
    }

    private Set<UserRole> extractRealmRoles(Jwt jwt) {
        Set<UserRole> roles = new HashSet<>();

        if (jwt.hasClaim("roles")) {
            Object rolesClaim = jwt.getClaim("roles");
            if (rolesClaim instanceof Collection<?> rolesList) {
                log.info("JWT roles claim: {}", rolesList);
                rolesList.stream()
                        .map(Object::toString)
                        .map(UserRole::fromString)
                        .filter(Objects::nonNull)
                        .forEach(roles::add);
            }
            log.info("Mapped roles: {}", roles);
        }
        return roles;
    }



    private Mono<ActorContext> buildServiceActor(Jwt jwt, String serviceName) {

        return Mono.just(
                ActorContext.builder()
                        .type(ActorType.SERVICE)
                        .serviceName(serviceName)
                        .serviceAuthorities(extractClientRoles(jwt, serviceName))
                        .build()
        );
    }

    private Set<String> extractClientRoles(Jwt jwt, String serviceName) {
        if (jwt.hasClaim("roles")) {
            Object rolesClaim = jwt.getClaim("roles");
            if (rolesClaim instanceof Collection<?> rolesList) {
                return rolesList.stream()
                        .map(Object::toString)
                        .collect(java.util.stream.Collectors.toSet());
            } else if (rolesClaim instanceof String rolesStr) {
                return Set.of(rolesStr.split(","));
            }
        }
        return Set.of();
    }
}