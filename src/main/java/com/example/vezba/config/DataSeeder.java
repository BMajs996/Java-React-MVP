package com.example.vezba.config;

import com.example.vezba.auth.PasswordHasher;
import com.example.vezba.model.AccountType;
import com.example.vezba.model.AppUser;
import com.example.vezba.model.Court;
import com.example.vezba.model.Favorite;
import com.example.vezba.model.FavoriteType;
import com.example.vezba.model.GameMatch;
import com.example.vezba.model.MatchStatus;
import com.example.vezba.model.Notification;
import com.example.vezba.model.Reservation;
import com.example.vezba.model.Tournament;
import com.example.vezba.model.UserRole;
import com.example.vezba.repository.AppUserRepository;
import com.example.vezba.repository.CourtRepository;
import com.example.vezba.repository.FavoriteRepository;
import com.example.vezba.repository.GameMatchRepository;
import com.example.vezba.repository.NotificationRepository;
import com.example.vezba.repository.ReservationRepository;
import com.example.vezba.repository.TournamentRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seed(AppUserRepository users, CourtRepository courts, GameMatchRepository matches,
                           ReservationRepository reservations, TournamentRepository tournaments,
                           FavoriteRepository favorites, NotificationRepository notifications,
                           PasswordHasher passwordHasher) {
        return args -> {
            if (users.count() > 0) {
                return;
            }

            AppUser admin = users.save(new AppUser("admin@demo.rs", "Admin", passwordHasher.hash("password"), AccountType.ADMIN, UserRole.ADMIN));
            AppUser ana = users.save(new AppUser("ana@demo.rs", "Ana Markovic", passwordHasher.hash("password"), AccountType.PLAYER, UserRole.USER));
            AppUser milos = users.save(new AppUser("milos@demo.rs", "Milos Petrovic", passwordHasher.hash("password"), AccountType.PLAYER, UserRole.USER));
            AppUser club = users.save(new AppUser("club@demo.rs", "TK Centar", passwordHasher.hash("password"), AccountType.CLUB, UserRole.USER));
            club.setCity("Beograd");
            club.setBio("Klub sa tri rekreativna terena i vecernjim terminima.");
            users.save(club);

            Court clay = courts.save(new Court("Teren 1", "Bulevar sporta 12", "Sljaka", club));
            Court hard = courts.save(new Court("Teren 2", "Bulevar sporta 12", "Beton", club));

            GameMatch played = new GameMatch("Ana vs Milos", LocalDateTime.now().minusDays(2).withHour(18).withMinute(0), ana, milos, ana, clay);
            played.setStatus(MatchStatus.PLAYED);
            played.setScore("6:4 6:3");
            played.setWinner(ana);
            matches.save(played);

            matches.save(new GameMatch("Vecernji termin", LocalDateTime.now().plusDays(1).withHour(20).withMinute(0), ana, milos, club, hard));
            reservations.save(new Reservation(clay, milos, LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(4)));
            tournaments.save(new Tournament("Letnji kup", "Beograd", 32, LocalDate.now().plusWeeks(2), LocalDate.now().plusWeeks(2).plusDays(2), club));
            favorites.save(new Favorite(ana, club, FavoriteType.CLUB));
            notifications.save(new Notification(ana, "Milos je potvrdio rezultat meca."));
            notifications.save(new Notification(admin, "Demo podaci su ucitani."));
        };
    }
}
