package com.shootersplatform.backend.health;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/health")
class HealthController {

    private final JdbcClient jdbcClient;

    HealthController(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @GetMapping
    HealthResponse health() {
        Integer databaseProbe = jdbcClient.sql("select 1").query(Integer.class).single();
        return new HealthResponse("ok", "ok", databaseProbe, Instant.now());
    }

    record HealthResponse(String backend, String database, Integer databaseProbe, Instant timestamp) {
    }
}
