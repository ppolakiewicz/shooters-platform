package com.shootersplatform.backend.bookings.waitlist.web;

import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class WaitlistApiClient {

    private final MockMvc mockMvc;

    public WaitlistApiClient(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public ResultActions list(MockHttpSession session, UUID termId) throws Exception {
        return mockMvc.perform(post("/api/bookings/waitlist/list")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {"termId": "%s"}
                        """.formatted(termId)));
    }

    public ResultActions listWithoutSession(UUID termId) throws Exception {
        return mockMvc.perform(post("/api/bookings/waitlist/list")
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {"termId": "%s"}
                        """.formatted(termId)));
    }

    public ResultActions cancelByParticipant(String cancellationToken) throws Exception {
        return mockMvc.perform(post("/api/bookings/waitlist/cancel-by-participant")
                .contentType("application/json")
                .content("""
                        {"token": "%s"}
                        """.formatted(cancellationToken)));
    }

    public ResultActions removeByOwner(MockHttpSession session, UUID termId, UUID waitlistEntryId) throws Exception {
        return mockMvc.perform(post("/api/bookings/waitlist/remove-by-owner")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {"termId": "%s", "waitlistEntryId": "%s"}
                        """.formatted(termId, waitlistEntryId)));
    }
}
