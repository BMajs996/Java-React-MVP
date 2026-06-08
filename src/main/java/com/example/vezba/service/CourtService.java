package com.example.vezba.service;

import com.example.vezba.model.AppUser;
import com.example.vezba.model.ClubProfile;
import com.example.vezba.model.Court;
import com.example.vezba.repository.CourtRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CourtService {
    private final CourtRepository courts;

    public CourtService(CourtRepository courts) {
        this.courts = courts;
    }

    public List<Court> list() {
        return courts.findAll();
    }

    public Court create(AppUser user, String name, String location, String surface) {
        ClubProfile club = user.getClubProfile();
        if (club == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only club accounts can create courts");
        }
        return courts.save(new Court(name, location, surface, club));
    }
}
