package com.example.vezba.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.vezba.model.AccountType;
import com.example.vezba.model.AppUser;
import com.example.vezba.model.UserRole;
import com.example.vezba.repository.TournamentRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class TournamentServiceTest {
    @Mock
    private TournamentRepository tournaments;

    @InjectMocks
    private TournamentService service;

    @Test
    void createRejectsRegularPlayerOrganizer() {
        AppUser player = new AppUser("player@demo.rs", "Player", "hash", AccountType.PLAYER, UserRole.USER);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.create(player, "Cup", "Belgrade", 16, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2)));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(tournaments, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createRejectsInvalidMaxPlayers() {
        AppUser club = new AppUser("club@demo.rs", "Club", "hash", AccountType.CLUB, UserRole.USER);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.create(club, "Cup", "Belgrade", 0, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2)));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(tournaments, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createRejectsPastStartDateForNonAdmin() {
        AppUser club = new AppUser("club@demo.rs", "Club", "hash", AccountType.CLUB, UserRole.USER);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.create(club, "Cup", "Belgrade", 16, LocalDate.now().minusDays(1), LocalDate.now().plusDays(2)));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(tournaments, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
