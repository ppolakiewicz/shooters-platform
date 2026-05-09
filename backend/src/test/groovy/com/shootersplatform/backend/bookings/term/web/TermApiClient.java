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

    public ResultActions create(MockHttpSession session, String name, int capacity, LocalDateTime startsAt) throws Exception {
        return mockMvc.perform(post("/api/bookings/terms")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content(termBody(name, capacity, startsAt)));
    }

    public ResultActions createWithoutCsrf(MockHttpSession session) throws Exception {
        return mockMvc.perform(post("/api/bookings/terms")
                .session(session)
                .contentType("application/json")
                .content(termBody("No csrf", 1, LocalDateTime.parse("2026-06-01T12:00:00"))));
    }

    public ResultActions update(MockHttpSession session, UUID termId, String name, int capacity, LocalDateTime startsAt) throws Exception {
        return mockMvc.perform(put("/api/bookings/terms/{termId}", termId)
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content(termBody(name, capacity, startsAt)));
    }

    private static String termBody(String name, int capacity, LocalDateTime startsAt) {
        return """
                {
                  "name": "%s",
                  "description": "",
                  "location": {
                    "placeName": "Range A",
                    "address": "Range Street 1",
                    "latitude": 52.2297,
                    "longitude": 21.0122
                  },
                  "capacity": %d,
                  "cancellationDeadlineDays": 1,
                  "durationMinutes": 60,
                  "startsAt": "%s"
                }
                """.formatted(name, capacity, startsAt);
    }
}
