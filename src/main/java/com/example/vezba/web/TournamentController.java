package com.example.vezba.web;

import com.example.vezba.auth.AuthService;
import com.example.vezba.model.AppUser;
import com.example.vezba.service.TournamentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {
    private final AuthService authService;
    private final TournamentService tournamentService;

    public TournamentController(AuthService authService, TournamentService tournamentService) {
        this.authService = authService;
        this.tournamentService = tournamentService;
    }

    @GetMapping
    public List<ApiDtos.TournamentDto> list() {
        return tournamentService.list().stream().map(ApiDtos.TournamentDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiDtos.TournamentDto create(@RequestHeader("X-Auth-Token") String token, @Valid @RequestBody TournamentRequest request) {
        AppUser organizer = authService.requireUser(token);
        return ApiDtos.TournamentDto.from(tournamentService.create(organizer, request.name(), request.city(), request.maxPlayers(),
            request.startsOn(), request.endsOn()));
    }

    public record TournamentRequest(@NotBlank String name, @NotBlank String city, @Positive int maxPlayers, @NotNull LocalDate startsOn,
                                    @NotNull LocalDate endsOn) {
    }
}
