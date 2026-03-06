package com.emis.academicservice.config;


import com.emis.academicservice.enums.ResourceAction;
import com.emis.academicservice.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

import java.util.*;

@Configuration
@Getter
@Setter
@EnableR2dbcAuditing
@ConfigurationProperties(prefix = "emis.services")
public class ServiceConfigurationProperties {

    @NestedConfigurationProperty
    private SchoolServiceProperties schoolConfiguration;

    @NestedConfigurationProperty
    private FacilityServiceProperties facilityConfiguration;

    @NestedConfigurationProperty
    private StudentServiceProperties studentConfiguration;


    @Getter
    @Setter
    public static class SchoolServiceProperties {
        private String baseUrl;
        private String getSchoolDetailsUrl;
        private String validateSchoolExistsUrl;

    }


    @Getter
    @Setter
    public static class FacilityServiceProperties{
        private String baseUrl;
        private String validateFacilityExistsUrl;
    }


    @Getter
    @Setter
    public static class StudentServiceProperties{
        private String baseUrl;
        private String getStudentDetailsUrl;
        private String getActiveEnrollmentUrl;
        private String batchStudentDetailsUrl;
    }

    private int timeout;

    @NotNull
    private Map<ResourceAction, ActionPolicy> actions = new EnumMap<>(ResourceAction.class);


    @Getter
    @Setter
    public static class ActionPolicy {
        private final Set<UserRole> roles = EnumSet.noneOf(UserRole.class);
        private final Set<String> serviceAuthorities = new HashSet<>();
    }

}
