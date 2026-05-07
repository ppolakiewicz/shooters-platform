package com.shootersplatform.backend.identity.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
class ClientIpResolver {

    String resolve(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            int firstSeparator = forwardedFor.indexOf(',');
            return (firstSeparator >= 0 ? forwardedFor.substring(0, firstSeparator) : forwardedFor).trim();
        }
        return request.getRemoteAddr();
    }
}
