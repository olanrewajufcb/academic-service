package com.emis.academicservice.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/swagger-ui/**").permitAll()
                        .pathMatchers("/webjars/**").permitAll()
                        .pathMatchers("/v3/api-docs/**").permitAll()

                        .pathMatchers("/favicon.ico").permitAll()
                        .pathMatchers("/index.html").permitAll()

                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(
                jwt -> {
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    if (jwt.hasClaim("roles")) {
                        Object roles = jwt.getClaim("roles");
                        if (roles instanceof Collection<?> rolesList) {
                            rolesList.stream()
                                    .map(Object::toString)
                                    .forEach(role -> authorities.add(
                                            new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));
                        }
                    }

                    if (isServiceToken(jwt)) {
                        authorities.add(new SimpleGrantedAuthority("ACTOR_SERVICE"));
                    } else {
                        authorities.add(new SimpleGrantedAuthority("ACTOR_USER"));
                    }
                    return Flux.fromIterable(authorities);
                });
        return converter;
    }


    private boolean isServiceToken(Jwt jwt) {
        String roles = jwt.getClaimAsString("roles");
        String preferredUsername =  jwt.getClaimAsString("preferred_username");
        return roles != null && roles.contains("INTERNAL_SERVICE")
                || preferredUsername != null && preferredUsername.startsWith("service-account");
    }
}
