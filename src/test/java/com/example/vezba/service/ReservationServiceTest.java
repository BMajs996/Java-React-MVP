package com.example.vezba.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.vezba.model.AccountType;
import com.example.vezba.model.AppUser;
import com.example.vezba.model.Court;
import com.example.vezba.model.Reservation;
import com.example.vezba.model.UserRole;
import com.example.vezba.repository.CourtRepository;
import com.example.vezba.repository.ReservationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
    @Mock
    private CourtRepository courts;

    @Mock
    private ReservationRepository reservations;

    @InjectMocks
    private ReservationService service;

    @Test
    void reserveRejectsInvalidTimeWindow() {
        AppUser user = new AppUser("player@demo.rs", "Player", "hash", AccountType.PLAYER, UserRole.USER);
        LocalDateTime start = LocalDateTime.parse("2031-01-01T11:00:00");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.reserve(user, 1L, start, start.minusHours(1)));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(courts, never()).findById(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void reserveRejectsOverlappingReservation() {
        AppUser user = new AppUser("player@demo.rs", "Player", "hash", AccountType.PLAYER, UserRole.USER);
        Court court = new Court("Court", "City", "Hard", null);
        LocalDateTime start = LocalDateTime.parse("2031-01-01T10:00:00");
        when(courts.findById(1L)).thenReturn(Optional.of(court));
        when(reservations.findByCourtAndStartsAtLessThanAndEndsAtGreaterThan(court, start.plusHours(1), start))
            .thenReturn(List.of(new Reservation(court, user, start.minusMinutes(30), start.plusMinutes(30))));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.reserve(user, 1L, start, start.plusHours(1)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(reservations, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
