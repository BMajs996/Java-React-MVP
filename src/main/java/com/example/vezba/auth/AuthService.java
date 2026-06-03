package com.example.vezba.auth;

import com.example.vezba.model.AccountType;
import com.example.vezba.model.AppUser;
import com.example.vezba.model.UserRole;
import com.example.vezba.repository.AppUserRepository;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final AppUserRepository users;
    private final PasswordHasher passwordHasher;
    private final TotpService totpService;
    private final Map<String, Long> sessions = new ConcurrentHashMap<>();
    private final Map<String, PendingLogin> pendingLogins = new ConcurrentHashMap<>();

    public AuthService(AppUserRepository users, PasswordHasher passwordHasher, TotpService totpService) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.totpService = totpService;
    }

    public AppUser register(String email, String displayName, String password, AccountType accountType) {
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        AppUser user = new AppUser(email.trim().toLowerCase(), displayName, passwordHasher.hash(password), accountType, UserRole.USER);
        return users.save(user);
    }

    public LoginResult login(String email, String password) {
        AppUser user = users.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (user.isBanned() || !passwordHasher.verify(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        if (user.isTwoFactorEnabled()) {
            String challengeId = UUID.randomUUID().toString();
            pendingLogins.put(challengeId, new PendingLogin(user.getId(), Instant.now().plusSeconds(300)));
            return LoginResult.requiresTwoFactor(challengeId);
        }
        return LoginResult.authenticated(issueToken(user));
    }

    public LoginResult verifyTwoFactor(String challengeId, String code) {
        PendingLogin pending = pendingLogins.remove(challengeId);
        if (pending == null || pending.expiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "2FA challenge expired");
        }
        AppUser user = users.findById(pending.userId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (!totpService.verify(user.getTwoFactorSecret(), code)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid 2FA code");
        }
        return LoginResult.authenticated(issueToken(user));
    }

    public TwoFactorSetup enableTwoFactor(String token) {
        AppUser user = requireUser(token);
        String secret = totpService.newSecret();
        user.setTwoFactorSecret(secret);
        user.setTwoFactorEnabled(true);
        users.save(user);
        return new TwoFactorSetup(secret, totpService.provisioningUri("Rekreativni mecevi", user.getEmail(), secret));
    }

    public AppUser requireUser(String token) {
        Long id = sessions.get(token);
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
        return users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public AppUser requireAdmin(String token) {
        AppUser user = requireUser(token);
        if (user.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role is required");
        }
        return user;
    }

    private String issueToken(AppUser user) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, user.getId());
        return token;
    }

    private record PendingLogin(Long userId, Instant expiresAt) {
    }

    public record LoginResult(boolean requiresTwoFactor, String challengeId, String token) {
        public static LoginResult requiresTwoFactor(String challengeId) {
            return new LoginResult(true, challengeId, null);
        }

        public static LoginResult authenticated(String token) {
            return new LoginResult(false, null, token);
        }
    }

    public record TwoFactorSetup(String secret, String provisioningUri) {
    }
}
