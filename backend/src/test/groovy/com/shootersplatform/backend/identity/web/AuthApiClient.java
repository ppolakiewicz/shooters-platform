package com.shootersplatform.backend.identity.web;

import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class AuthApiClient {

    private static final AtomicInteger REGISTRATION_CLIENT_SEQUENCE = new AtomicInteger(1);

    private final MockMvc mockMvc;

    public AuthApiClient(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public ResultActions register(String email, String username, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .with(request -> {
                    request.setRemoteAddr(nextRegistrationClientIp());
                    return request;
                })
                .contentType("application/json")
                .content("""
                        {"email":"%s","username":"%s","password":"%s"}
                        """.formatted(email, username, password)));
    }

    public ResultActions registerWithoutCsrf(String email, String username, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content("""
                        {"email":"%s","username":"%s","password":"%s"}
                        """.formatted(email, username, password)));
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

    private static String nextRegistrationClientIp() {
        return "198.51.100.%d".formatted(REGISTRATION_CLIENT_SEQUENCE.getAndIncrement());
    }
}
