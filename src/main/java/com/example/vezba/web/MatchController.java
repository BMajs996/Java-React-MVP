package com.example.vezba.web;

import com.example.vezba.auth.AuthService;
import com.example.vezba.model.AccountType;
import com.example.vezba.model.AppUser;
import com.example.vezba.model.Court;
import com.example.vezba.model.GameMatch;
import com.example.vezba.model.MatchStatus;
import com.example.vezba.model.Notification;
import com.example.vezba.repository.AppUserRepository;
import com.example.vezba.repository.CourtRepository;
import com.example.vezba.repository.GameMatchRepository;
import com.example.vezba.repository.NotificationRepository;
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
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final GameMatchRepository matches;
    private final AppUserRepository users;
    private final CourtRepository courts;
    private final NotificationRepository notifications;
    private final AuthService authService;

    public MatchController(GameMatchRepository matches, AppUserRepository users, CourtRepository courts,
                           NotificationRepository notifications, AuthService authService) {
        this.matches = matches;
        this.users = users;
        this.courts = courts;
        this.notifications = notifications;
        this.authService = authService;
    }

    @GetMapping
    public List<ApiDtos.MatchDto> list(@RequestParam(defaultValue = "all") String filter,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate target = date == null ? LocalDate.now() : date;
        List<GameMatch> result = switch (filter) {
            case "previous" -> matches.findByStartTimeBetweenOrderByStartTimeAsc(LocalDate.now().minusYears(10).atStartOfDay(), LocalDate.now().atStartOfDay());
            case "today" -> matches.findByStartTimeBetweenOrderByStartTimeAsc(target.atStartOfDay(), target.plusDays(1).atStartOfDay());
            case "upcoming" -> matches.findByStartTimeBetweenOrderByStartTimeAsc(LocalDate.now().plusDays(1).atStartOfDay(), LocalDate.now().plusYears(10).atStartOfDay());
            default -> matches.findAll();
        };
        return result.stream().map(ApiDtos.MatchDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiDtos.MatchDto create(@RequestHeader("X-Auth-Token") String token, @RequestBody CreateMatchRequest request) {
        AppUser organizer = authService.requireUser(token);
        Court court = courts.findById(request.courtId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Court not found"));
        AppUser playerA = request.playerAId() == null ? organizer : player(request.playerAId());
        AppUser playerB = request.playerBId() == null ? null : player(request.playerBId());
        if (playerA.getAccountType() != AccountType.PLAYER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player A must be a player account");
        }
        if (playerB != null && playerA.getId().equals(playerB.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Players must be different");
        }
        GameMatch match = matches.save(new GameMatch(request.title(), request.startTime(), playerA, playerB, organizer, court));
        if (playerB != null) {
            notifications.save(new Notification(playerB, "Zakazan je mec: " + match.getTitle()));
        }
        return ApiDtos.MatchDto.from(match);
    }

    @PatchMapping("/{id}/score")
    public ApiDtos.MatchDto submitScore(@RequestHeader("X-Auth-Token") String token, @PathVariable Long id, @RequestBody ScoreRequest request) {
        authService.requireUser(token);
        GameMatch match = matches.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));
        match.setScore(request.score());
        match.setWinner(user(request.winnerId()));
        match.setStatus(MatchStatus.PLAYED);
        match.setResultSubmittedAt(LocalDateTime.now());
        notifications.save(new Notification(match.getOrganizer(), "Upisan je rezultat za mec: " + match.getTitle()));
        return ApiDtos.MatchDto.from(matches.save(match));
    }

    private AppUser user(Long id) {
        return users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private AppUser player(Long id) {
        AppUser user = user(id);
        if (user.getAccountType() != AccountType.PLAYER || user.isBanned()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User must be an active player");
        }
        return user;
    }

    public record CreateMatchRequest(String title, LocalDateTime startTime, Long playerAId, Long playerBId, Long courtId) {
    }

    public record ScoreRequest(String score, Long winnerId) {
    }
}
