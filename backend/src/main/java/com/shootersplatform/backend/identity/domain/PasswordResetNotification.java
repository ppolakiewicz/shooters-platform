package com.shootersplatform.backend.identity.domain;

public record PasswordResetNotification(EmailAddress email, String resetLink) {
}
