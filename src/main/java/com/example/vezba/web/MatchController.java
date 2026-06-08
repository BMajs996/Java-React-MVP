package com.example.vezba.web;

import com.example.vezba.auth.AuthService;
import com.example.vezba.model.AppUser;
import com.example.vezba.service.MatchService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final AuthService authService;
    private final MatchService matchService;

    public MatchController(AuthService authService, MatchService matchService) {
        this.authService = authService;
        this.matchService = matchService;
    }

    @GetMapping
    public List<ApiDtos.MatchDto> list(@RequestParam(defaultValue = "all") String filter,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return matchService.list(filter, date).stream().map(ApiDtos.MatchDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiDtos.MatchDto create(@RequestHeader("X-Auth-Token") String token, @RequestBody CreateMatchRequest request) {
        AppUser organizer = authService.requireUser(token);
        return ApiDtos.MatchDto.from(matchService.create(organizer, request.title(), request.startTime(), request.playerAId(),
            request.playerBId(), request.courtId()));
    }

    @PatchMapping("/{id}/score")
    public ApiDtos.MatchDto submitScore(@RequestHeader("X-Auth-Token") String token, @PathVariable Long id, @RequestBody ScoreRequest request) {
        AppUser submittedBy = authService.requireUser(token);
        return ApiDtos.MatchDto.from(matchService.submitScore(submittedBy, id, request.score(), request.winnerId()));
    }

    public record CreateMatchRequest(String title, LocalDateTime startTime, Long playerAId, Long playerBId, Long courtId) {
    }

    public record ScoreRequest(String score, Long winnerId) {
    }
}
