package com.example.vezba.web;

import com.example.vezba.auth.AuthService;
import com.example.vezba.model.AccountType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiDtos.UserDto register(@Valid @RequestBody RegisterRequest request) {
        return ApiDtos.UserDto.from(authService.register(request.email(), request.displayName(), request.password(),
            request.accountType()));
    }

    @PostMapping("/login")
    public AuthService.LoginResult login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.email(), request.password());
    }

    @PostMapping("/2fa/verify")
    public AuthService.LoginResult verifyTwoFactor(@Valid @RequestBody TwoFactorVerifyRequest request) {
        return authService.verifyTwoFactor(request.challengeId(), request.code());
    }

    @PostMapping("/2fa/enable")
    public AuthService.TwoFactorSetup enableTwoFactor(@RequestHeader("X-Auth-Token") String token) {
        return authService.enableTwoFactor(token);
    }

    @GetMapping("/me")
    public ApiDtos.UserDto me(@RequestHeader("X-Auth-Token") String token) {
        return ApiDtos.UserDto.from(authService.requireUser(token));
    }

    public record RegisterRequest(@NotBlank @Email String email, @NotBlank String displayName, @NotBlank String password,
                                  @NotNull AccountType accountType) {
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }

    public record TwoFactorVerifyRequest(@NotBlank String challengeId, @NotBlank String code) {
    }
}
