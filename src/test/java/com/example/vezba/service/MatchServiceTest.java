package com.example.vezba.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.vezba.model.AccountType;
import com.example.vezba.model.AppUser;
import com.example.vezba.model.Court;
import com.example.vezba.model.GameMatch;
import com.example.vezba.model.MatchStatus;
import com.example.vezba.model.UserRole;
import com.example.vezba.repository.AppUserRepository;
import com.example.vezba.repository.CourtRepository;
import com.example.vezba.repository.GameMatchRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {
    @Mock
    private GameMatchRepository matches;

    @Mock
    private AppUserRepository users;

    @Mock
    private CourtRepository courts;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private MatchService service;

    @Test
    void createRejectsSamePlayers() {
        AppUser organizer = user(1L, AccountType.PLAYER);
        AppUser player = user(2L, AccountType.PLAYER);
        when(courts.findById(10L)).thenReturn(Optional.of(new Court("Court", "City", "Hard", null)));
        when(users.findById(2L)).thenReturn(Optional.of(player));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.create(organizer, "Match", LocalDateTime.now().plusDays(1), 2L, 2L, 10L));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(matches, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void submitScoreRejectsNonParticipantWinner() {
        AppUser organizer = user(1L, AccountType.PLAYER);
        AppUser playerA = user(2L, AccountType.PLAYER);
        AppUser outsider = user(3L, AccountType.PLAYER);
        GameMatch match = new GameMatch("Match", LocalDateTime.now().minusDays(1), playerA, null, organizer, null);
        when(matches.findById(99L)).thenReturn(Optional.of(match));
        when(users.findById(3L)).thenReturn(Optional.of(outsider));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.submitScore(organizer, 99L, "6:4 6:4", 3L));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(matches, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void submitScoreAllowsParticipantAndNotifiesOrganizer() {
        AppUser organizer = user(1L, AccountType.CLUB);
        AppUser playerA = user(2L, AccountType.PLAYER);
        AppUser playerB = user(3L, AccountType.PLAYER);
        GameMatch match = new GameMatch("Match", LocalDateTime.now().minusDays(1), playerA, playerB, organizer, null);
        when(matches.findById(99L)).thenReturn(Optional.of(match));
        when(users.findById(2L)).thenReturn(Optional.of(playerA));
        when(matches.save(match)).thenReturn(match);

        GameMatch result = service.submitScore(playerB, 99L, "6:4 6:4", 2L);

        assertEquals(MatchStatus.PLAYED, result.getStatus());
        assertEquals(playerA, result.getWinner());
        verify(notificationService).create(organizer, "Upisan je rezultat za mec: Match");
    }

    private AppUser user(Long id, AccountType accountType) {
        AppUser user = new AppUser("user-" + id + "@demo.rs", "User " + id, "hash", accountType, UserRole.USER);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
