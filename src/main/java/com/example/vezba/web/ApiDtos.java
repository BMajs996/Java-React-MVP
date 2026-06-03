package com.example.vezba.web;

import com.example.vezba.model.AccountType;
import com.example.vezba.model.AppUser;
import com.example.vezba.model.Court;
import com.example.vezba.model.GameMatch;
import com.example.vezba.model.Notification;
import com.example.vezba.model.Photo;
import com.example.vezba.model.Reservation;
import com.example.vezba.model.Tournament;
import com.example.vezba.model.UserRole;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class ApiDtos {
    private ApiDtos() {
    }

    public record UserDto(Long id, String email, String displayName, AccountType accountType, UserRole role,
                          boolean banned, boolean twoFactorEnabled, String phone, String city, String avatarUrl,
                          String bio) {
        public static UserDto from(AppUser user) {
            return new UserDto(user.getId(), user.getEmail(), user.getDisplayName(), user.getAccountType(), user.getRole(),
                user.isBanned(), user.isTwoFactorEnabled(), user.getPhone(), user.getCity(), user.getAvatarUrl(), user.getBio());
        }
    }

    public record CourtDto(Long id, String name, String location, String surface, boolean active, UserDto club) {
        public static CourtDto from(Court court) {
            return new CourtDto(court.getId(), court.getName(), court.getLocation(), court.getSurface(), court.isActive(),
                court.getClub() == null ? null : UserDto.from(court.getClub()));
        }
    }

    public record MatchDto(Long id, String title, LocalDateTime startTime, String score, String status,
                           UserDto playerA, UserDto playerB, UserDto winner, UserDto organizer, CourtDto court) {
        public static MatchDto from(GameMatch match) {
            return new MatchDto(match.getId(), match.getTitle(), match.getStartTime(), match.getScore(), match.getStatus().name(),
                match.getPlayerA() == null ? null : UserDto.from(match.getPlayerA()),
                match.getPlayerB() == null ? null : UserDto.from(match.getPlayerB()),
                match.getWinner() == null ? null : UserDto.from(match.getWinner()),
                match.getOrganizer() == null ? null : UserDto.from(match.getOrganizer()),
                match.getCourt() == null ? null : CourtDto.from(match.getCourt()));
        }
    }

    public record ReservationDto(Long id, CourtDto court, UserDto reservedBy, LocalDateTime startsAt, LocalDateTime endsAt,
                                 String status) {
        public static ReservationDto from(Reservation reservation) {
            return new ReservationDto(reservation.getId(), CourtDto.from(reservation.getCourt()), UserDto.from(reservation.getReservedBy()),
                reservation.getStartsAt(), reservation.getEndsAt(), reservation.getStatus().name());
        }
    }

    public record TournamentDto(Long id, String name, String city, int maxPlayers, LocalDate startsOn, LocalDate endsOn,
                                UserDto organizer) {
        public static TournamentDto from(Tournament tournament) {
            return new TournamentDto(tournament.getId(), tournament.getName(), tournament.getCity(), tournament.getMaxPlayers(),
                tournament.getStartsOn(), tournament.getEndsOn(), UserDto.from(tournament.getOrganizer()));
        }
    }

    public record RankingDto(Long playerId, String displayName, long wins, long played, long losses, double winRate) {
    }

    public record NotificationDto(Long id, String message, boolean read, LocalDateTime createdAt) {
        public static NotificationDto from(Notification notification) {
            return new NotificationDto(notification.getId(), notification.getMessage(), notification.isRead(), notification.getCreatedAt());
        }
    }

    public record PhotoDto(Long id, String url, String caption, LocalDateTime createdAt) {
        public static PhotoDto from(Photo photo) {
            return new PhotoDto(photo.getId(), photo.getUrl(), photo.getCaption(), photo.getCreatedAt());
        }
    }
}
