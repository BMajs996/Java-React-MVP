package com.example.vezba.service;

import com.example.vezba.model.AccountType;
import com.example.vezba.model.AppUser;
import com.example.vezba.model.Tournament;
import com.example.vezba.model.UserRole;
import com.example.vezba.repository.TournamentRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TournamentService {
    private final TournamentRepository tournaments;

    public TournamentService(TournamentRepository tournaments) {
        this.tournaments = tournaments;
    }

    public List<Tournament> list() {
        return tournaments.findAllByOrderByStartsOnAsc();
    }

    public Tournament create(AppUser organizer, String name, String city, int maxPlayers, LocalDate startsOn, LocalDate endsOn) {
        validateOrganizer(organizer);
        validateDates(organizer, startsOn, endsOn);
        if (maxPlayers <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Max players must be greater than zero");
        }
        return tournaments.save(new Tournament(name, city, maxPlayers, startsOn, endsOn, organizer));
    }

    private void validateOrganizer(AppUser organizer) {
        if (organizer.getAccountType() != AccountType.CLUB && organizer.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only clubs or admins can create tournaments");
        }
    }

    private void validateDates(AppUser organizer, LocalDate startsOn, LocalDate endsOn) {
        if (startsOn == null || endsOn == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tournament start and end dates are required");
        }
        if (endsOn.isBefore(startsOn)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tournament end date must be after start date");
        }
        if (organizer.getRole() != UserRole.ADMIN && startsOn.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tournament start date cannot be in the past");
        }
    }
}
