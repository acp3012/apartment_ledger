package com.yesh.apartmentledger.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.zalando.logbook.HeaderFilter;
import org.zalando.logbook.core.HeaderFilters;

import java.util.Set;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary // Forces Spring to prioritize this instance over any other fallback
    public ObjectMapper logbookObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Essential module for handling dates/times cleanly in modern Java
        mapper.registerModule(new JavaTimeModule());

        return mapper;
    }

    @Bean
    public HeaderFilter customHeaderFilter() {
        // Explicitly target the case-insensitive security headers you want to mask
        return HeaderFilters.replaceHeaders(
                Set.of("Authorization", "X-Api-Key", "token", "secret"),
                "XXX"
        );
    }
}