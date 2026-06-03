package com.example.vezba.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Court court;

    @ManyToOne
    private AppUser reservedBy;

    private LocalDateTime startsAt;
    private LocalDateTime endsAt;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    protected Reservation() {
    }

    public Reservation(Court court, AppUser reservedBy, LocalDateTime startsAt, LocalDateTime endsAt) {
        this.court = court;
        this.reservedBy = reservedBy;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public Long getId() {
        return id;
    }

    public Court getCourt() {
        return court;
    }

    public void setCourt(Court court) {
        this.court = court;
    }

    public AppUser getReservedBy() {
        return reservedBy;
    }

    public void setReservedBy(AppUser reservedBy) {
        this.reservedBy = reservedBy;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(LocalDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(LocalDateTime endsAt) {
        this.endsAt = endsAt;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
}
