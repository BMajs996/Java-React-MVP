package com.example.vezba.web;

import com.example.vezba.auth.AuthService;
import com.example.vezba.model.AppUser;
import com.example.vezba.service.CourtService;
import com.example.vezba.service.ReservationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

@RestController
@RequestMapping("/api/courts")
public class CourtController {
    private final AuthService authService;
    private final CourtService courtService;
    private final ReservationService reservationService;

    public CourtController(AuthService authService, CourtService courtService, ReservationService reservationService) {
        this.authService = authService;
        this.courtService = courtService;
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<ApiDtos.CourtDto> list() {
        return courtService.list().stream().map(ApiDtos.CourtDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiDtos.CourtDto create(@RequestHeader("X-Auth-Token") String token, @Valid @RequestBody CourtRequest request) {
        AppUser user = authService.requireUser(token);
        return ApiDtos.CourtDto.from(courtService.create(user, request.name(), request.location(), request.surface()));
    }

    @GetMapping("/reservations")
    public List<ApiDtos.ReservationDto> reservations() {
        return reservationService.list().stream().map(ApiDtos.ReservationDto::from).toList();
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiDtos.ReservationDto reserve(@RequestHeader("X-Auth-Token") String token, @Valid @RequestBody ReservationRequest request) {
        AppUser user = authService.requireUser(token);
        return ApiDtos.ReservationDto.from(reservationService.reserve(user, request.courtId(), request.startsAt(), request.endsAt()));
    }

    public record CourtRequest(@NotBlank String name, @NotBlank String location, @NotBlank String surface) {
    }

    public record ReservationRequest(@NotNull Long courtId, @NotNull LocalDateTime startsAt, @NotNull LocalDateTime endsAt) {
    }
}
