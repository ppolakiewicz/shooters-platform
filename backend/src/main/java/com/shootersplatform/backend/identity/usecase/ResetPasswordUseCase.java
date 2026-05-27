package com.shootersplatform.backend.identity.usecase;

import com.shootersplatform.backend.identity.domain.PasswordResetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResetPasswordUseCase {

    private final PasswordResetService passwordReset;

    ResetPasswordUseCase(PasswordResetService passwordReset) {
        this.passwordReset = passwordReset;
    }

    @Transactional
    public void reset(String token, String password) {
        passwordReset.resetPassword(token, password);
    }
}
