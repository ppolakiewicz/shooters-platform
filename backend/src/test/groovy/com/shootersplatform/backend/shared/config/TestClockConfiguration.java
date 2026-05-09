package com.shootersplatform.backend.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import spock.util.time.MutableClock;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

@Configuration
public class TestClockConfiguration {

    private static final Instant BASE_TIME = Instant.parse("2026-05-08T10:00:00Z");

    @Bean
    @Primary
    static Clock mutableClock() {
        return new MutableClock(BASE_TIME, ZoneOffset.UTC);
    }
}
