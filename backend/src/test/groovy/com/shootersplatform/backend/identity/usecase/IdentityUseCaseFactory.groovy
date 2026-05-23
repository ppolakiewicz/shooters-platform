package com.shootersplatform.backend.identity.usecase

import com.shootersplatform.backend.identity.domain.IdentityService
import com.shootersplatform.backend.identity.domain.LoginRateLimiter

final class IdentityUseCaseFactory {

    private IdentityUseCaseFactory() {
    }

    static RegisterUserUseCase registerUser(IdentityService identity, LoginRateLimiter rateLimiter) {
        new RegisterUserUseCase(identity, rateLimiter)
    }
}
