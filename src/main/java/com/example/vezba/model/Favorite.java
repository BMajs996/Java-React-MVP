package com.example.vezba.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private AppUser owner;

    @ManyToOne
    private AppUser target;

    @Enumerated(EnumType.STRING)
    private FavoriteType type;

    protected Favorite() {
    }

    public Favorite(AppUser owner, AppUser target, FavoriteType type) {
        this.owner = owner;
        this.target = target;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public AppUser getOwner() {
        return owner;
    }

    public void setOwner(AppUser owner) {
        this.owner = owner;
    }

    public AppUser getTarget() {
        return target;
    }

    public void setTarget(AppUser target) {
        this.target = target;
    }

    public FavoriteType getType() {
        return type;
    }

    public void setType(FavoriteType type) {
        this.type = type;
    }
}
