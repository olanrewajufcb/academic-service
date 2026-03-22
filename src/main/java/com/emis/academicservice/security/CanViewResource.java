package com.emis.academicservice.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize(
        "@schoolAuth.authorize(authentication, #p0, " +
                "T(com.emis.academicservice.enums.ResourceAction).VIEW_RESOURCE)"
)
public @interface CanViewResource {}
