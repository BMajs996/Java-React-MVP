package com.example.vezba.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Court {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private String surface;
    private boolean active = true;

    @ManyToOne
    private AppUser club;

    protected Court() {
    }

    public Court(String name, String location, String surface, AppUser club) {
        this.name = name;
        this.location = location;
        this.surface = surface;
        this.club = club;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSurface() {
        return surface;
    }

    public void setSurface(String surface) {
        this.surface = surface;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public AppUser getClub() {
        return club;
    }

    public void setClub(AppUser club) {
        this.club = club;
    }
}
