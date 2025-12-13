package com.emis.academicservice.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

@Configuration
@Getter
@Setter
@EnableR2dbcAuditing
@ConfigurationProperties(prefix = "emis.services")
public class ServiceConfigurationProperties {

    @NestedConfigurationProperty
    private SchoolServiceProperties configuration;

    @NestedConfigurationProperty
    private HrServiceProperties hrConfiguration;

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
    public static class HrServiceProperties{
        private String baseUrl;
        private String validateTeacherExistsUrl;
    }


    @Getter
    @Setter
    public static class StudentServiceProperties{
        private String baseUrl;
        private String getStudentDetailsUrl;
        private String batchStudentDetailsUrl;
    }

}
