package com.shootersplatform.backend.identity;

import com.shootersplatform.backend.identity.domain.PasswordResetTokenGenerator;
import org.jspecify.annotations.NullMarked;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.HexFormat;
import java.util.Queue;

@NullMarked
public class FixedPasswordResetTokenGenerator implements PasswordResetTokenGenerator {

    private final Queue<String> tokens = new ArrayDeque<>();

    public FixedPasswordResetTokenGenerator(String... tokens) {
        this.tokens.addAll(java.util.List.of(tokens));
    }

    @Override
    public String generateToken() {
        return tokens.remove();
    }

    @Override
    public String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
