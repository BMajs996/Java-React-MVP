package com.example.vezba.repository;

import com.example.vezba.model.Tournament;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    List<Tournament> findAllByOrderByStartsOnAsc();
}
