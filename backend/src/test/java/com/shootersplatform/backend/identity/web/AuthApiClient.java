package com.shootersplatform.backend.identity.web;

import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class AuthApiClient {

    private final MockMvc mockMvc;

    public AuthApiClient(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public ResultActions register(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {"email":"%s","password":"%s"}
                        """.formatted(email, password)));
    }

    public ResultActions registerWithoutCsrf(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content("""
                        {"email":"%s","password":"%s"}
                        """.formatted(email, password)));
    }

    public ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {"email":"%s","password":"%s"}
                        """.formatted(email, password)));
    }

    public ResultActions login(String email, String password, String clientIp) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .with(request -> {
                    request.setRemoteAddr(clientIp);
                    return request;
                })
                .contentType("application/json")
                .content("""
                        {"email":"%s","password":"%s"}
                        """.formatted(email, password)));
    }

    public ResultActions me(MockHttpSession session) throws Exception {
        return mockMvc.perform(get("/api/auth/me").session(session));
    }

    public ResultActions logout(MockHttpSession session) throws Exception {
        return mockMvc.perform(post("/api/auth/logout").session(session).with(csrf()));
    }

    public ResultActions health() throws Exception {
        return mockMvc.perform(get("/api/health"));
    }
}
