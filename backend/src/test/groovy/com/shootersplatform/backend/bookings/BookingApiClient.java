package com.shootersplatform.backend.bookings;

import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public class BookingApiClient {

    private final MockMvc mockMvc;

    public BookingApiClient(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public ResultActions publicTerms() throws Exception {
        return mockMvc.perform(get("/api/bookings/public/terms"));
    }

    public ResultActions createEnrollment(MockHttpSession session, String name, int capacity) throws Exception {
        return mockMvc.perform(post("/api/bookings/enrollments")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content(enrollmentBody(name, capacity)));
    }

    public ResultActions listEnrollments(MockHttpSession session) throws Exception {
        return mockMvc.perform(get("/api/bookings/enrollments").session(session));
    }

    public ResultActions createTerm(MockHttpSession session, String name, int capacity, LocalDateTime startsAt) throws Exception {
        return mockMvc.perform(post("/api/bookings/terms")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content(termBody(name, capacity, startsAt)));
    }

    public ResultActions createTermWithoutCsrf(MockHttpSession session) throws Exception {
        return mockMvc.perform(post("/api/bookings/terms")
                .session(session)
                .contentType("application/json")
                .content(termBody("No csrf", 1, LocalDateTime.parse("2026-06-01T12:00:00"))));
    }

    public ResultActions updateTerm(MockHttpSession session, UUID termId, String name, int capacity, LocalDateTime startsAt) throws Exception {
        return mockMvc.perform(put("/api/bookings/terms/{termId}", termId)
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content(termBody(name, capacity, startsAt)));
    }

    public ResultActions reserve(UUID termId, String firstName, String lastName, String email) throws Exception {
        return mockMvc.perform(post("/api/bookings/reservations/reserve")
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                          "termId": "%s",
                          "firstName": "%s",
                          "lastName": "%s",
                          "email": "%s",
                          "phoneNumber": "+48111111111",
                          "createAccount": false,
                          "username": null,
                          "password": null
                        }
                        """.formatted(termId, firstName, lastName, email)));
    }

    public ResultActions listReservations(MockHttpSession session, UUID termId) throws Exception {
        return mockMvc.perform(post("/api/bookings/reservations/list")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {"termId": "%s"}
                        """.formatted(termId)));
    }

    public ResultActions listReservationsWithoutSession(UUID termId) throws Exception {
        return mockMvc.perform(post("/api/bookings/reservations/list")
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {"termId": "%s"}
                        """.formatted(termId)));
    }

    private static String enrollmentBody(String name, int capacity) {
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
                  "durationMinutes": 60
                }
                """.formatted(name, capacity);
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
