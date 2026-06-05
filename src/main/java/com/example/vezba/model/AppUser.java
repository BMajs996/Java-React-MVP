package com.example.vezba.model;

import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_users")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    private boolean banned;
    private boolean twoFactorEnabled;
    private String twoFactorSecret;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private PlayerProfile playerProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private ClubProfile clubProfile;

    protected AppUser() {
    }

    public AppUser(String email, String displayName, String passwordHash, AccountType accountType, UserRole role) {
        this.email = email;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
        this.accountType = accountType;
        this.role = role;
        ensureProfileForAccountType();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
        ensureProfileForAccountType();
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public String getTwoFactorSecret() {
        return twoFactorSecret;
    }

    public void setTwoFactorSecret(String twoFactorSecret) {
        this.twoFactorSecret = twoFactorSecret;
    }

    public String getPhone() {
        ProfileDetails profile = publicProfile();
        return profile == null ? null : profile.getPhone();
    }

    public void setPhone(String phone) {
        ProfileDetails profile = ensurePublicProfile();
        if (profile != null) {
            profile.setPhone(phone);
        }
    }

    public String getCity() {
        ProfileDetails profile = publicProfile();
        return profile == null ? null : profile.getCity();
    }

    public void setCity(String city) {
        ProfileDetails profile = ensurePublicProfile();
        if (profile != null) {
            profile.setCity(city);
        }
    }

    public String getAvatarUrl() {
        ProfileDetails profile = publicProfile();
        return profile == null ? null : profile.getAvatarUrl();
    }

    public void setAvatarUrl(String avatarUrl) {
        ProfileDetails profile = ensurePublicProfile();
        if (profile != null) {
            profile.setAvatarUrl(avatarUrl);
        }
    }

    public String getBio() {
        ProfileDetails profile = publicProfile();
        return profile == null ? null : profile.getBio();
    }

    public void setBio(String bio) {
        ProfileDetails profile = ensurePublicProfile();
        if (profile != null) {
            profile.setBio(bio);
        }
    }

    public PlayerProfile getPlayerProfile() {
        return playerProfile;
    }

    public void setPlayerProfile(PlayerProfile playerProfile) {
        this.playerProfile = playerProfile;
        if (playerProfile != null && playerProfile.getUser() != this) {
            playerProfile.setUser(this);
        }
    }

    public ClubProfile getClubProfile() {
        return clubProfile;
    }

    public void setClubProfile(ClubProfile clubProfile) {
        this.clubProfile = clubProfile;
        if (clubProfile != null && clubProfile.getUser() != this) {
            clubProfile.setUser(this);
        }
    }

    private ProfileDetails publicProfile() {
        return switch (accountType) {
            case PLAYER -> playerProfile;
            case CLUB -> clubProfile;
            case ADMIN -> null;
        };
    }

    private ProfileDetails ensurePublicProfile() {
        ensureProfileForAccountType();
        return publicProfile();
    }

    private void ensureProfileForAccountType() {
        if (accountType == AccountType.PLAYER && playerProfile == null) {
            playerProfile = new PlayerProfile(this);
        }
        if (accountType == AccountType.CLUB && clubProfile == null) {
            clubProfile = new ClubProfile(this);
        }
    }
}
