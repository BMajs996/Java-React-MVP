package com.example.vezba.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TotpServiceTest {
    private final TotpService totpService = new TotpService();

    @Test
    void newSecretIsBase32Encoded() {
        String secret = totpService.newSecret();

        assertTrue(secret.matches("[A-Z2-7]{32}"));
    }

    @Test
    void provisioningUriContainsIssuerEmailAndSecret() {
        String uri = totpService.provisioningUri("Rekreativni mecevi", "ana@demo.rs", "ABCDEF234567");

        assertTrue(uri.startsWith("otpauth://totp/Rekreativni+mecevi%3Aana%40demo.rs"));
        assertTrue(uri.contains("secret=ABCDEF234567"));
        assertTrue(uri.contains("issuer=Rekreativni+mecevi"));
        assertTrue(uri.contains("digits=6"));
        assertTrue(uri.contains("period=30"));
    }

    @Test
    void verifyRejectsInvalidInputs() {
        assertFalse(totpService.verify(null, "123456"));
        assertFalse(totpService.verify("ABCDEF234567", null));
        assertFalse(totpService.verify("ABCDEF234567", "12345"));
        assertFalse(totpService.verify("not-valid-base32", "123456"));
    }
}
