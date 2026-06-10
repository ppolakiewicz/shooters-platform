package com.shootersplatform.backend.bookings.web;

import com.shootersplatform.backend.bookings.traininglevel.domain.TrainingLevel;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public class TrainingTemplateApiClient {

    private final MockMvc mockMvc;

    public TrainingTemplateApiClient(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public ResultActions listWithoutSession() throws Exception {
        return mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/bookings/training-templates"));
    }

    public ResultActions list(MockHttpSession session) throws Exception {
        return mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/bookings/training-templates").session(session));
    }

    public ResultActions get(MockHttpSession session, UUID templateId) throws Exception {
        return mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                "/api/bookings/training-templates/{templateId}",
                templateId
        ).session(session));
    }

    public ResultActions create(MockHttpSession session, String name) throws Exception {
        return create(session, name, "", TrainingLevel.BASIC, 8, 1, 90, "09:15");
    }

    public ResultActions create(
            MockHttpSession session,
            String name,
            TrainingLevel trainingLevel,
            int capacity,
            int durationMinutes,
            String defaultStartTime
    ) throws Exception {
        return create(session, name, "", trainingLevel, capacity, 1, durationMinutes, defaultStartTime);
    }

    public ResultActions create(
            MockHttpSession session,
            String name,
            String description,
            TrainingLevel trainingLevel,
            int capacity,
            int cancellationDeadlineDays,
            int durationMinutes,
            String defaultStartTime
    ) throws Exception {
        return mockMvc.perform(post("/api/bookings/training-templates")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content(templateBody(
                        name,
                        description,
                        trainingLevel,
                        capacity,
                        cancellationDeadlineDays,
                        durationMinutes,
                        defaultStartTime
                )));
    }

    public ResultActions createRaw(MockHttpSession session, String body) throws Exception {
        return mockMvc.perform(post("/api/bookings/training-templates")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content(body));
    }

    public ResultActions update(MockHttpSession session, UUID templateId, String name) throws Exception {
        return update(session, templateId, name, "", TrainingLevel.ADVANCED, 10, 1, 120, "10:30");
    }

    public ResultActions update(
            MockHttpSession session,
            UUID templateId,
            String name,
            String description,
            TrainingLevel trainingLevel,
            int capacity,
            int cancellationDeadlineDays,
            int durationMinutes,
            String defaultStartTime
    ) throws Exception {
        return mockMvc.perform(put("/api/bookings/training-templates/{templateId}", templateId)
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content(templateBody(
                        name,
                        description,
                        trainingLevel,
                        capacity,
                        cancellationDeadlineDays,
                        durationMinutes,
                        defaultStartTime
                )));
    }

    public ResultActions delete(MockHttpSession session, UUID templateId) throws Exception {
        return mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                        "/api/bookings/training-templates/{templateId}",
                        templateId
                )
                .session(session)
                .with(csrf()));
    }

    private static String templateBody(
            String name,
            String description,
            TrainingLevel trainingLevel,
            int capacity,
            int cancellationDeadlineDays,
            int durationMinutes,
            String defaultStartTime
    ) {
        return """
                {
                  "name": "%s",
                  "description": "%s",
                  "trainingLevel": "%s",
                  "location": {
                    "placeName": "Range A",
                    "address": "Range Street 1",
                    "latitude": 52.2297,
                    "longitude": 21.0122
                  },
                  "capacity": %d,
                  "cancellationDeadlineDays": %d,
                  "durationMinutes": %d,
                  "defaultStartTime": "%s"
                }
                """.formatted(
                name,
                description,
                trainingLevel,
                capacity,
                cancellationDeadlineDays,
                durationMinutes,
                defaultStartTime
        );
    }
}
