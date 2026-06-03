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
public class GameMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private LocalDateTime startTime;
    private String score;
    private LocalDateTime resultSubmittedAt;

    @Enumerated(EnumType.STRING)
    private MatchStatus status = MatchStatus.SCHEDULED;

    @ManyToOne
    private AppUser playerA;

    @ManyToOne
    private AppUser playerB;

    @ManyToOne
    private AppUser winner;

    @ManyToOne
    private AppUser organizer;

    @ManyToOne
    private Court court;

    protected GameMatch() {
    }

    public GameMatch(String title, LocalDateTime startTime, AppUser playerA, AppUser playerB, AppUser organizer, Court court) {
        this.title = title;
        this.startTime = startTime;
        this.playerA = playerA;
        this.playerB = playerB;
        this.organizer = organizer;
        this.court = court;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public LocalDateTime getResultSubmittedAt() {
        return resultSubmittedAt;
    }

    public void setResultSubmittedAt(LocalDateTime resultSubmittedAt) {
        this.resultSubmittedAt = resultSubmittedAt;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public AppUser getPlayerA() {
        return playerA;
    }

    public void setPlayerA(AppUser playerA) {
        this.playerA = playerA;
    }

    public AppUser getPlayerB() {
        return playerB;
    }

    public void setPlayerB(AppUser playerB) {
        this.playerB = playerB;
    }

    public AppUser getWinner() {
        return winner;
    }

    public void setWinner(AppUser winner) {
        this.winner = winner;
    }

    public AppUser getOrganizer() {
        return organizer;
    }

    public void setOrganizer(AppUser organizer) {
        this.organizer = organizer;
    }

    public Court getCourt() {
        return court;
    }

    public void setCourt(Court court) {
        this.court = court;
    }
}
