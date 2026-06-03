package com.example.vezba.web;

import com.example.vezba.auth.AuthService;
import com.example.vezba.model.AppUser;
import com.example.vezba.model.GameMatch;
import com.example.vezba.repository.AppUserRepository;
import com.example.vezba.repository.GameMatchRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AuthService authService;
    private final AppUserRepository users;
    private final GameMatchRepository matches;

    public AdminController(AuthService authService, AppUserRepository users, GameMatchRepository matches) {
        this.authService = authService;
        this.users = users;
        this.matches = matches;
    }

    @GetMapping("/users")
    public List<ApiDtos.UserDto> users(@RequestHeader("X-Auth-Token") String token) {
        authService.requireAdmin(token);
        return users.findAll().stream().map(ApiDtos.UserDto::from).toList();
    }

    @PatchMapping("/users/{id}/ban")
    public ApiDtos.UserDto banUser(@RequestHeader("X-Auth-Token") String token, @PathVariable Long id) {
        authService.requireAdmin(token);
        AppUser user = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setBanned(true);
        return ApiDtos.UserDto.from(users.save(user));
    }

    @DeleteMapping("/matches/{id}")
    public void deleteMatch(@RequestHeader("X-Auth-Token") String token, @PathVariable Long id) {
        authService.requireAdmin(token);
        GameMatch match = matches.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));
        matches.delete(match);
    }
}
