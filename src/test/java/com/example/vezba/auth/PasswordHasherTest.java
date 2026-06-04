package com.example.vezba.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {
    private final PasswordHasher passwordHasher = new PasswordHasher();

    @Test
    void hashVerifiesOriginalPasswordOnly() {
        String hash = passwordHasher.hash("password");

        assertTrue(passwordHasher.verify("password", hash));
        assertFalse(passwordHasher.verify("wrong-password", hash));
    }

    @Test
    void hashUsesDifferentSaltForSamePassword() {
        String first = passwordHasher.hash("password");
        String second = passwordHasher.hash("password");

        assertNotEquals(first, second);
        assertTrue(passwordHasher.verify("password", first));
        assertTrue(passwordHasher.verify("password", second));
    }

    @Test
    void malformedStoredHashDoesNotVerify() {
        assertFalse(passwordHasher.verify("password", "not-a-valid-hash"));
    }
}
