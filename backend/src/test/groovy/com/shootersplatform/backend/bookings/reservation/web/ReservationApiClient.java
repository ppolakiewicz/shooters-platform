package com.shootersplatform.backend.bookings.reservation.web;

import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class ReservationApiClient {

    private final MockMvc mockMvc;

    public ReservationApiClient(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
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

    public ResultActions list(MockHttpSession session, UUID termId) throws Exception {
        return mockMvc.perform(post("/api/bookings/reservations/list")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {"termId": "%s"}
                        """.formatted(termId)));
    }

    public ResultActions listWithoutSession(UUID termId) throws Exception {
        return mockMvc.perform(post("/api/bookings/reservations/list")
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {"termId": "%s"}
                        """.formatted(termId)));
    }
}
