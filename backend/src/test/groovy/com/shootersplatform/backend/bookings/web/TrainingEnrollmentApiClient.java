package com.shootersplatform.backend.bookings.web;

import com.shootersplatform.backend.bookings.traininglevel.domain.TrainingLevel;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class TrainingEnrollmentApiClient {

    private final MockMvc mockMvc;

    public TrainingEnrollmentApiClient(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public ResultActions create(MockHttpSession session, String name, int capacity) throws Exception {
        return create(session, name, TrainingLevel.BASIC, capacity);
    }

    public ResultActions create(MockHttpSession session, String name, TrainingLevel trainingLevel, int capacity) throws Exception {
        return mockMvc.perform(post("/api/bookings/training-enrollments")
            .session(session)
            .with(csrf())
            .contentType("application/json")
                .content(enrollmentBody(name, trainingLevel, capacity)));
    }

    public ResultActions list(MockHttpSession session) throws Exception {
        return mockMvc.perform(get("/api/bookings/training-enrollments").session(session));
    }

    private static String enrollmentBody(String name, TrainingLevel trainingLevel, int capacity) {
        return """
            {
              "name": "%s",
              "description": "",
                  "trainingLevel": "%s",
              "location": {
                "placeName": "Range A",
                "address": "Range Street 1",
                "latitude": 52.2297,
                "longitude": 21.0122
              },
              "capacity": %d,
              "cancellationDeadlineDays": 1,
              "durationMinutes": 60
            }
                """.formatted(name, trainingLevel, capacity);
    }
}
