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
        return reserve(null, termId, firstName, lastName, email, false, null, null);
    }

    public ResultActions reserve(
            MockHttpSession session,
            UUID termId,
            String firstName,
            String lastName,
            String email,
            boolean createAccount,
            String username,
            String password
    ) throws Exception {
        return reserve(session, reserveBody(termId, firstName, lastName, email, createAccount, username, password));
    }

    public ResultActions reserve(MockHttpSession session, String body) throws Exception {
        var request = post("/api/bookings/reservations/reserve")
                .with(csrf())
                .contentType("application/json")
                .content(body);
        if (session != null) {
            request.session(session);
        }
        return mockMvc.perform(request);
    }

    public ResultActions reserve(String body) throws Exception {
        return reserve(null, body);
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

    public ResultActions cancelByParticipant(String cancellationToken) throws Exception {
        return mockMvc.perform(post("/api/bookings/reservations/cancel-by-participant")
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {"token": "%s"}
                        """.formatted(cancellationToken)));
    }

    public ResultActions confirmWaitlistOffer(String confirmationToken) throws Exception {
        return mockMvc.perform(post("/api/bookings/reservations/confirm-waitlist-offer")
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {"token": "%s"}
                        """.formatted(confirmationToken)));
    }

    public ResultActions cancelByInstructor(MockHttpSession session, UUID termId, UUID reservationId) throws Exception {
        return mockMvc.perform(post("/api/bookings/reservations/cancel-by-instructor")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {"termId": "%s", "reservationId": "%s"}
                        """.formatted(termId, reservationId)));
    }

    public ResultActions expireWaitlistOffers(MockHttpSession session, UUID termId) throws Exception {
        return mockMvc.perform(post("/api/bookings/reservations/expire-waitlist-offers")
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

    public static String reserveBody(
            UUID termId,
            String firstName,
            String lastName,
            String email,
            boolean createAccount,
            String username,
            String password
    ) {
        return """
                {
                  "termId": "%s",
                  "firstName": "%s",
                  "lastName": "%s",
                  "email": "%s",
                  "phoneNumber": "+48111111111",
                  "createAccount": %s,
                  "username": %s,
                  "password": %s
                }
                """.formatted(termId, firstName, lastName, email, createAccount, jsonStringOrNull(username), jsonStringOrNull(password));
    }

    private static String jsonStringOrNull(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }
}
