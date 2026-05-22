package com.shootersplatform.backend.training.web;

import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class TrainingApiClient {

    private final MockMvc mockMvc;

    public TrainingApiClient(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public ResultActions list(MockHttpSession session) throws Exception {
        return mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/trainings").session(session));
    }

    public ResultActions get(MockHttpSession session, UUID trainingId) throws Exception {
        return mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/trainings/{trainingId}", trainingId).session(session));
    }

    public ResultActions create(MockHttpSession session, String name, String place, String description, LocalDate performedOn) throws Exception {
        return mockMvc.perform(post("/api/trainings")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                          "name": "%s",
                          "place": "%s",
                          "description": "%s",
                          "performedOn": "%s",
                          "weaponType": "PISTOL",
                          "scoringType": "IDPA"
                        }
                        """.formatted(name, place, description, performedOn)));
    }

    public ResultActions createWithoutCsrf(MockHttpSession session) throws Exception {
        return mockMvc.perform(post("/api/trainings")
                .session(session)
                .contentType("application/json")
                .content("""
                        {
                          "name": "Practice",
                          "place": "Range A",
                          "description": "",
                          "performedOn": "2026-06-01",
                          "weaponType": "PISTOL",
                          "scoringType": "IDPA"
                        }
                        """));
    }

    public ResultActions update(MockHttpSession session, UUID trainingId, String name, String place, String description) throws Exception {
        return mockMvc.perform(put("/api/trainings/{trainingId}", trainingId)
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                          "name": "%s",
                          "place": "%s",
                          "description": "%s",
                          "performedOn": "2026-06-02",
                          "weaponType": "RIFLE",
                          "scoringType": "TARGET"
                        }
                        """.formatted(name, place, description)));
    }

    public ResultActions deleteTraining(MockHttpSession session, UUID trainingId) throws Exception {
        return mockMvc.perform(delete("/api/trainings/{trainingId}", trainingId).session(session).with(csrf()));
    }

    public ResultActions addIdpaTask(MockHttpSession session, UUID trainingId, int alpha, int charlie, int delta, int miss, int durationTenths) throws Exception {
        return mockMvc.perform(post("/api/trainings/{trainingId}/tasks", trainingId)
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                          "weaponType": "PISTOL",
                          "scoringType": "IDPA",
                          "durationTenths": %d,
                          "score": {
                            "alpha": %d,
                            "charlie": %d,
                            "delta": %d,
                            "miss": %d
                          }
                        }
                        """.formatted(durationTenths, alpha, charlie, delta, miss)));
    }

    public ResultActions addTargetTaskWithZero(MockHttpSession session, UUID trainingId) throws Exception {
        return mockMvc.perform(post("/api/trainings/{trainingId}/tasks", trainingId)
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                          "weaponType": "RIFLE",
                          "scoringType": "TARGET",
                          "durationTenths": 455,
                          "score": {
                            "0": 1,
                            "10": 2
                          }
                        }
                        """));
    }

    public ResultActions updateIdpaTask(MockHttpSession session, UUID trainingId, UUID taskId) throws Exception {
        return mockMvc.perform(put("/api/trainings/{trainingId}/tasks/{taskId}", trainingId, taskId)
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                          "weaponType": "SHOTGUN",
                          "scoringType": "IDPA",
                          "durationTenths": 510,
                          "score": {
                            "alpha": 1,
                            "charlie": 1,
                            "delta": 0,
                            "miss": 0
                          }
                        }
                        """));
    }

    public ResultActions deleteTask(MockHttpSession session, UUID trainingId, UUID taskId) throws Exception {
        return mockMvc.perform(delete("/api/trainings/{trainingId}/tasks/{taskId}", trainingId, taskId).session(session).with(csrf()));
    }
}
