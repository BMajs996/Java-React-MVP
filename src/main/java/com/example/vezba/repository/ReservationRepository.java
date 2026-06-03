package com.example.vezba.repository;

import com.example.vezba.model.Court;
import com.example.vezba.model.Reservation;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByCourtAndStartsAtLessThanAndEndsAtGreaterThan(Court court, LocalDateTime endsAt, LocalDateTime startsAt);
}
