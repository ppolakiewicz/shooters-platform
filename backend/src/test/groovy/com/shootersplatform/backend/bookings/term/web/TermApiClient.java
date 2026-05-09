package com.shootersplatform.backend.bookings.term.web;

import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public class TermApiClient {

    private final MockMvc mockMvc;

    public TermApiClient(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public ResultActions publicTerms() throws Exception {
        return mockMvc.perform(get("/api/bookings/public/terms"));
    }

    public ResultActions publicTerm(UUID termId) throws Exception {
        return mockMvc.perform(get("/api/bookings/public/terms/{termId}", termId));
    }

    public ResultActions list(MockHttpSession session) throws Exception {
        return mockMvc.perform(get("/api/bookings/terms").session(session));
    }

    public ResultActions create(MockHttpSession session, String name, int capacity, LocalDateTime startsAt) throws Exception {
        return create(session, termBody(name, capacity, startsAt));
    }

    public ResultActions create(MockHttpSession session, String body) throws Exception {
        return mockMvc.perform(post("/api/bookings/terms")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content(body));
    }

    public ResultActions createWithoutCsrf(MockHttpSession session) throws Exception {
        return mockMvc.perform(post("/api/bookings/terms")
                .session(session)
                .contentType("application/json")
                .content(termBody("No csrf", 1, LocalDateTime.parse("2026-06-01T12:00:00"))));
    }

    public ResultActions update(MockHttpSession session, UUID termId, String name, int capacity, LocalDateTime startsAt) throws Exception {
        return update(session, termId, termBody(name, capacity, startsAt));
    }

    public ResultActions update(MockHttpSession session, UUID termId, String body) throws Exception {
        return mockMvc.perform(put("/api/bookings/terms/{termId}", termId)
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content(body));
    }

    public static String termBody(String name, int capacity, LocalDateTime startsAt) {
        return termBody(name, "", "Range A", "Range Street 1", 52.2297d, 21.0122d, capacity, 1, 60, startsAt);
    }

    public static String termBody(
            String name,
            String description,
            String placeName,
            String address,
            double latitude,
            double longitude,
            int capacity,
            int cancellationDeadlineDays,
            int durationMinutes,
            LocalDateTime startsAt
    ) {
        return """
                {
                  "name": "%s",
                  "description": "%s",
                  "location": {
                    "placeName": "%s",
                    "address": "%s",
                    "latitude": %s,
                    "longitude": %s
                  },
                  "capacity": %d,
                  "cancellationDeadlineDays": %d,
                  "durationMinutes": %d,
                  "startsAt": "%s"
                }
                """.formatted(name, description, placeName, address, latitude, longitude, capacity, cancellationDeadlineDays, durationMinutes, startsAt);
    }
}
