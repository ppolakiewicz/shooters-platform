package com.shootersplatform.backend.identity.web;

import com.shootersplatform.backend.identity.domain.AuthenticatedUser;
import com.shootersplatform.backend.identity.usecase.LoginUserUseCase;
import com.shootersplatform.backend.identity.usecase.RegisterUserUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
class AuthController {

    private final RegisterUserUseCase registerUser;
    private final LoginUserUseCase loginUser;
    private final SecuritySessionService sessions;
    private final ClientIpResolver clientIpResolver;

    AuthController(
            RegisterUserUseCase registerUser,
            LoginUserUseCase loginUser,
            SecuritySessionService sessions,
            ClientIpResolver clientIpResolver
    ) {
        this.registerUser = registerUser;
        this.loginUser = loginUser;
        this.sessions = sessions;
        this.clientIpResolver = clientIpResolver;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    AuthenticatedUserResponse register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        AuthenticatedUser user = registerUser.register(
                request.email(),
                request.username(),
                request.password(),
                clientIpResolver.resolve(servletRequest)
        );
        sessions.authenticate(user, servletRequest, servletResponse);
        return AuthenticatedUserResponse.from(user);
    }

    @PostMapping("/login")
    AuthenticatedUserResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        AuthenticatedUser user = loginUser.login(request.email(), request.password(), clientIpResolver.resolve(servletRequest));
        sessions.authenticate(user, servletRequest, servletResponse);
        return AuthenticatedUserResponse.from(user);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(HttpServletRequest request, HttpServletResponse response) {
        sessions.logout(request, response);
    }

    @GetMapping("/me")
    AuthenticatedUserResponse me() {
        return AuthenticatedUserResponse.from(sessions.currentUser());
    }

    @GetMapping("/csrf")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void csrf(CsrfToken csrfToken) {
        csrfToken.getToken();
    }
}
