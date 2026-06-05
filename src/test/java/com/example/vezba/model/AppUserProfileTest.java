package com.example.vezba.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AppUserProfileTest {
    @Test
    void playerAccountCreatesPlayerProfileAndStoresPublicFieldsThere() {
        AppUser user = new AppUser("ana@demo.rs", "Ana", "hash", AccountType.PLAYER, UserRole.USER);

        user.setPhone("+38164111222");
        user.setCity("Novi Sad");
        user.setAvatarUrl("https://example.com/ana.png");
        user.setBio("Right-handed player");

        assertNotNull(user.getPlayerProfile());
        assertNull(user.getClubProfile());
        assertSame(user, user.getPlayerProfile().getUser());
        assertEquals("+38164111222", user.getPlayerProfile().getPhone());
        assertEquals("Novi Sad", user.getCity());
        assertEquals("https://example.com/ana.png", user.getAvatarUrl());
        assertEquals("Right-handed player", user.getBio());
    }

    @Test
    void clubAccountCreatesClubProfileAndCourtBelongsToClubProfile() {
        AppUser clubUser = new AppUser("club@demo.rs", "TK Centar", "hash", AccountType.CLUB, UserRole.USER);
        clubUser.setCity("Beograd");

        Court court = new Court("Teren 1", "Bulevar sporta 12", "Sljaka", clubUser.getClubProfile());

        assertNotNull(clubUser.getClubProfile());
        assertNull(clubUser.getPlayerProfile());
        assertSame(clubUser, clubUser.getClubProfile().getUser());
        assertEquals("Beograd", clubUser.getClubProfile().getCity());
        assertSame(clubUser.getClubProfile(), court.getClub());
        assertTrue(court.isActive());
    }

    @Test
    void adminAccountDoesNotCreatePublicProfile() {
        AppUser admin = new AppUser("admin@demo.rs", "Admin", "hash", AccountType.ADMIN, UserRole.ADMIN);

        admin.setPhone("+38164111222");
        admin.setCity("Beograd");
        admin.setBio("Ignored profile data");

        assertNull(admin.getPlayerProfile());
        assertNull(admin.getClubProfile());
        assertNull(admin.getPhone());
        assertNull(admin.getCity());
        assertNull(admin.getBio());
    }

    @Test
    void changingAccountTypeCreatesMatchingProfileWithoutDroppingExistingProfile() {
        AppUser user = new AppUser("user@demo.rs", "User", "hash", AccountType.PLAYER, UserRole.USER);
        user.setCity("Nis");

        user.setAccountType(AccountType.CLUB);
        user.setBio("Club profile");

        assertNotNull(user.getPlayerProfile());
        assertNotNull(user.getClubProfile());
        assertSame(user, user.getClubProfile().getUser());
        assertEquals("Club profile", user.getClubProfile().getBio());
        assertEquals("Nis", user.getPlayerProfile().getCity());
    }
}
