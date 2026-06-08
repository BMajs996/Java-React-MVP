package com.example.vezba.service;

import com.example.vezba.model.AccountType;
import com.example.vezba.model.AppUser;
import com.example.vezba.model.Court;
import com.example.vezba.model.GameMatch;
import com.example.vezba.model.MatchStatus;
import com.example.vezba.model.UserRole;
import com.example.vezba.repository.AppUserRepository;
import com.example.vezba.repository.CourtRepository;
import com.example.vezba.repository.GameMatchRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MatchService {
    private final GameMatchRepository matches;
    private final AppUserRepository users;
    private final CourtRepository courts;
    private final NotificationService notificationService;

    public MatchService(GameMatchRepository matches, AppUserRepository users, CourtRepository courts,
                        NotificationService notificationService) {
        this.matches = matches;
        this.users = users;
        this.courts = courts;
        this.notificationService = notificationService;
    }

    public List<GameMatch> list(String filter, LocalDate date) {
        LocalDate target = date == null ? LocalDate.now() : date;
        return switch (filter) {
            case "previous" -> matches.findByStartTimeBetweenOrderByStartTimeAsc(
                LocalDate.now().minusYears(10).atStartOfDay(), LocalDate.now().atStartOfDay());
            case "today" -> matches.findByStartTimeBetweenOrderByStartTimeAsc(target.atStartOfDay(), target.plusDays(1).atStartOfDay());
            case "upcoming" -> matches.findByStartTimeBetweenOrderByStartTimeAsc(
                LocalDate.now().plusDays(1).atStartOfDay(), LocalDate.now().plusYears(10).atStartOfDay());
            default -> matches.findAll();
        };
    }

    public GameMatch create(AppUser organizer, String title, LocalDateTime startTime, Long playerAId, Long playerBId, Long courtId) {
        Court court = courts.findById(courtId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Court not found"));
        AppUser playerA = playerAId == null ? organizer : player(playerAId);
        AppUser playerB = playerBId == null ? null : player(playerBId);

        if (playerA.getAccountType() != AccountType.PLAYER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player A must be a player account");
        }
        if (playerB != null && playerA.getId().equals(playerB.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Players must be different");
        }

        GameMatch match = matches.save(new GameMatch(title, startTime, playerA, playerB, organizer, court));
        if (playerB != null) {
            notificationService.create(playerB, "Zakazan je mec: " + match.getTitle());
        }
        return match;
    }

    public GameMatch submitScore(AppUser submittedBy, Long matchId, String score, Long winnerId) {
        if (score == null || score.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Score is required");
        }
        GameMatch match = matches.findById(matchId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));
        if (!canSubmitResult(submittedBy, match)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only organizer, participants or admin can submit result");
        }

        AppUser winner = user(winnerId);
        ensureWinnerIsParticipant(match, winner);

        match.setScore(score);
        match.setWinner(winner);
        match.setStatus(MatchStatus.PLAYED);
        match.setResultSubmittedAt(LocalDateTime.now());
        notificationService.create(match.getOrganizer(), "Upisan je rezultat za mec: " + match.getTitle());
        return matches.save(match);
    }

    private boolean canSubmitResult(AppUser user, GameMatch match) {
        return user.getRole() == UserRole.ADMIN
            || sameUser(user, match.getOrganizer())
            || sameUser(user, match.getPlayerA())
            || sameUser(user, match.getPlayerB());
    }

    private void ensureWinnerIsParticipant(GameMatch match, AppUser winner) {
        if (!sameUser(winner, match.getPlayerA()) && !sameUser(winner, match.getPlayerB())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Winner must be one of the match participants");
        }
    }

    private boolean sameUser(AppUser left, AppUser right) {
        return left != null && right != null && left.getId().equals(right.getId());
    }

    private AppUser user(Long id) {
        return users.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private AppUser player(Long id) {
        AppUser user = user(id);
        if (user.getAccountType() != AccountType.PLAYER || user.isBanned()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User must be an active player");
        }
        return user;
    }
}
