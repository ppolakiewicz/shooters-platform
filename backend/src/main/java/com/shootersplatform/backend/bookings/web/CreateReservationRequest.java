package com.shootersplatform.backend.bookings.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

record CreateReservationRequest(
        @NotNull UUID termId,
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @NotBlank @Email @Size(max = 320) String email,
        @NotBlank @Size(max = 40) String phoneNumber,
        boolean createAccount,
        @Nullable @Size(min = 3, max = 32) String username,
        @Nullable @Size(min = 12, max = 128) String password
) {
}
