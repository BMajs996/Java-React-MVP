package com.example.vezba.service;

import com.example.vezba.model.AppUser;
import com.example.vezba.model.Court;
import com.example.vezba.model.Reservation;
import com.example.vezba.repository.CourtRepository;
import com.example.vezba.repository.ReservationRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReservationService {
    private final CourtRepository courts;
    private final ReservationRepository reservations;

    public ReservationService(CourtRepository courts, ReservationRepository reservations) {
        this.courts = courts;
        this.reservations = reservations;
    }

    public List<Reservation> list() {
        return reservations.findAll();
    }

    public Reservation reserve(AppUser user, Long courtId, LocalDateTime startsAt, LocalDateTime endsAt) {
        validateReservationWindow(startsAt, endsAt);
        Court court = courts.findById(courtId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Court not found"));
        if (!court.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Court is not active");
        }
        List<Reservation> overlaps = reservations.findByCourtAndStartsAtLessThanAndEndsAtGreaterThan(court, endsAt, startsAt);
        if (!overlaps.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Court is already reserved in this period");
        }
        return reservations.save(new Reservation(court, user, startsAt, endsAt));
    }

    private void validateReservationWindow(LocalDateTime startsAt, LocalDateTime endsAt) {
        if (startsAt == null || endsAt == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation start and end are required");
        }
        if (!startsAt.isBefore(endsAt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation start must be before end");
        }
    }
}
