package com.example.vezba.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;

@Entity
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String city;
    private int maxPlayers;
    private LocalDate startsOn;
    private LocalDate endsOn;

    @ManyToOne
    private AppUser organizer;

    protected Tournament() {
    }

    public Tournament(String name, String city, int maxPlayers, LocalDate startsOn, LocalDate endsOn, AppUser organizer) {
        this.name = name;
        this.city = city;
        this.maxPlayers = maxPlayers;
        this.startsOn = startsOn;
        this.endsOn = endsOn;
        this.organizer = organizer;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public LocalDate getStartsOn() {
        return startsOn;
    }

    public void setStartsOn(LocalDate startsOn) {
        this.startsOn = startsOn;
    }

    public LocalDate getEndsOn() {
        return endsOn;
    }

    public void setEndsOn(LocalDate endsOn) {
        this.endsOn = endsOn;
    }

    public AppUser getOrganizer() {
        return organizer;
    }

    public void setOrganizer(AppUser organizer) {
        this.organizer = organizer;
    }
}
