package com.shootersplatform.backend.shared.config.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Configuration
class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        CsrfTokenRequestAttributeHandler csrfTokenRequestHandler = new CsrfTokenRequestAttributeHandler();

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(csrfTokenRequestHandler)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/api/health", "/api/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> writeProblem(
                                response,
                                objectMapper,
                                HttpStatus.UNAUTHORIZED,
                                "Authentication required",
                                "Authentication is required to access this resource"
                        ))
                        .accessDeniedHandler((request, response, exception) -> writeProblem(
                                response,
                                objectMapper,
                                HttpStatus.FORBIDDEN,
                                "Access denied",
                                "You do not have permission to access this resource"
                        ))
                );

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        String encodingId = "argon2id";
        return new DelegatingPasswordEncoder(
                encodingId,
                Map.of(encodingId, Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8())
        );
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException(username);
        };
    }

    private void writeProblem(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            HttpStatus status,
            String title,
            String detail
    ) throws java.io.IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
