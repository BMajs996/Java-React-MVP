package com.example.vezba.web;

import com.example.vezba.auth.AuthService;
import com.example.vezba.model.AppUser;
import com.example.vezba.model.ClubProfile;
import com.example.vezba.model.Court;
import com.example.vezba.model.Reservation;
import com.example.vezba.repository.CourtRepository;
import com.example.vezba.repository.ReservationRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/courts")
public class CourtController {
    private final CourtRepository courts;
    private final ReservationRepository reservations;
    private final AuthService authService;

    public CourtController(CourtRepository courts, ReservationRepository reservations, AuthService authService) {
        this.courts = courts;
        this.reservations = reservations;
        this.authService = authService;
    }

    @GetMapping
    public List<ApiDtos.CourtDto> list() {
        return courts.findAll().stream().map(ApiDtos.CourtDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiDtos.CourtDto create(@RequestHeader("X-Auth-Token") String token, @RequestBody CourtRequest request) {
        AppUser user = authService.requireUser(token);
        ClubProfile club = user.getClubProfile();
        if (club == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only club accounts can create courts");
        }
        return ApiDtos.CourtDto.from(courts.save(new Court(request.name(), request.location(), request.surface(), club)));
    }

    @GetMapping("/reservations")
    public List<ApiDtos.ReservationDto> reservations() {
        return reservations.findAll().stream().map(ApiDtos.ReservationDto::from).toList();
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiDtos.ReservationDto reserve(@RequestHeader("X-Auth-Token") String token, @RequestBody ReservationRequest request) {
        AppUser user = authService.requireUser(token);
        Court court = courts.findById(request.courtId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Court not found"));
        List<Reservation> overlaps = reservations.findByCourtAndStartsAtLessThanAndEndsAtGreaterThan(court, request.endsAt(), request.startsAt());
        if (!overlaps.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Court is already reserved in this period");
        }
        return ApiDtos.ReservationDto.from(reservations.save(new Reservation(court, user, request.startsAt(), request.endsAt())));
    }

    public record CourtRequest(String name, String location, String surface) {
    }

    public record ReservationRequest(Long courtId, LocalDateTime startsAt, LocalDateTime endsAt) {
    }
}
