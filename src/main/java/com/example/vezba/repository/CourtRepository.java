package com.example.vezba.repository;

import com.example.vezba.model.ClubProfile;
import com.example.vezba.model.Court;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourtRepository extends JpaRepository<Court, Long> {
    List<Court> findByClub(ClubProfile club);
}
