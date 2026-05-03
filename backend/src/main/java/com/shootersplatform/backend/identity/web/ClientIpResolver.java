package com.shootersplatform.backend.identity.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
class ClientIpResolver {

    String resolve(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
