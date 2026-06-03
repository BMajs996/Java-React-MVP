package com.example.vezba.repository;

import com.example.vezba.model.AppUser;
import com.example.vezba.model.GameMatch;
import com.example.vezba.model.MatchStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameMatchRepository extends JpaRepository<GameMatch, Long> {
    List<GameMatch> findByStatusOrderByStartTimeAsc(MatchStatus status);

    List<GameMatch> findByStartTimeBetweenOrderByStartTimeAsc(LocalDateTime from, LocalDateTime to);

    long countByWinner(AppUser winner);

    long countByPlayerAOrPlayerB(AppUser playerA, AppUser playerB);
}
