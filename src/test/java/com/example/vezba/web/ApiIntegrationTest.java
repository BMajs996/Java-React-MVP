package com.example.vezba.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiIntegrationTest {
    private final HttpClient client = HttpClient.newHttpClient();

    @Value("${local.server.port}")
    private int port;

    @Test
    void loginAndMeEndpointReturnCurrentUserWithoutSensitiveFields() throws Exception {
        String token = login("admin@demo.rs", "password");

        TestResponse me = send("GET", "/api/auth/me", null, Map.of("X-Auth-Token", token));

        assertEquals(200, me.statusCode());
        assertTrue(me.body().contains("\"email\":\"admin@demo.rs\""));
        assertFalse(me.body().contains("passwordHash"));
        assertFalse(me.body().contains("twoFactorSecret"));
    }

    @Test
    void enablingTwoFactorMakesNextLoginRequireChallenge() throws Exception {
        String email = "two-factor-" + System.nanoTime() + "@demo.rs";
        register(email, "Two Factor User", "password", "PLAYER");
        String token = login(email, "password");

        TestResponse setup = send("POST", "/api/auth/2fa/enable", "", Map.of("X-Auth-Token", token));
        TestResponse nextLogin = send("POST", "/api/auth/login", json(
            "email", email,
            "password", "password"
        ), Map.of());

        assertEquals(200, setup.statusCode());
        assertTrue(setup.body().contains("otpauth://totp/"));
        assertEquals(200, nextLogin.statusCode());
        assertTrue(nextLogin.body().contains("\"requiresTwoFactor\":true"));
        assertTrue(nextLogin.body().contains("\"challengeId\":"));
    }

    @Test
    void profileUpdatePersistsPublicProfileFields() throws Exception {
        String token = login("club@demo.rs", "password");

        TestResponse updated = send("PATCH", "/api/profile", json(
            "displayName", "TK Test",
            "phone", "+38164111222",
            "city", "Novi Sad",
            "avatarUrl", "https://example.com/avatar.png",
            "bio", "Updated club profile"
        ), Map.of("X-Auth-Token", token));
        TestResponse me = send("GET", "/api/auth/me", null, Map.of("X-Auth-Token", token));

        assertEquals(200, updated.statusCode());
        assertTrue(me.body().contains("\"displayName\":\"TK Test\""));
        assertTrue(me.body().contains("\"phone\":\"+38164111222\""));
        assertTrue(me.body().contains("\"city\":\"Novi Sad\""));
        assertTrue(me.body().contains("\"avatarUrl\":\"https://example.com/avatar.png\""));
        assertTrue(me.body().contains("\"bio\":\"Updated club profile\""));
    }

    @Test
    void onlyClubAccountsCanCreateCourts() throws Exception {
        String clubToken = login("club@demo.rs", "password");
        String playerToken = login("ana@demo.rs", "password");

        TestResponse created = send("POST", "/api/courts", json(
            "name", "Integration Court",
            "location", "Novi Sad",
            "surface", "Hard"
        ), Map.of("X-Auth-Token", clubToken));
        TestResponse forbidden = send("POST", "/api/courts", json(
            "name", "Player Court",
            "location", "Novi Sad",
            "surface", "Hard"
        ), Map.of("X-Auth-Token", playerToken));

        assertEquals(201, created.statusCode());
        assertTrue(created.body().contains("\"club\""));
        assertTrue(created.body().contains("\"email\":\"club@demo.rs\""));
        assertEquals(403, forbidden.statusCode());
    }

    @Test
    void adminProfileUpdateDoesNotCreatePublicProfileFields() throws Exception {
        String token = login("admin@demo.rs", "password");

        TestResponse updated = send("PATCH", "/api/profile", json(
            "displayName", "Admin Updated",
            "phone", "+38164111222",
            "city", "Beograd",
            "avatarUrl", "https://example.com/admin.png",
            "bio", "Admin should not have public profile"
        ), Map.of("X-Auth-Token", token));
        TestResponse me = send("GET", "/api/auth/me", null, Map.of("X-Auth-Token", token));

        assertEquals(200, updated.statusCode());
        assertTrue(me.body().contains("\"displayName\":\"Admin Updated\""));
        assertTrue(me.body().contains("\"phone\":null"));
        assertTrue(me.body().contains("\"city\":null"));
        assertTrue(me.body().contains("\"avatarUrl\":null"));
        assertTrue(me.body().contains("\"bio\":null"));
    }

    @Test
    void overlappingCourtReservationIsRejected() throws Exception {
        String token = login("ana@demo.rs", "password");
        long courtId = firstId(send("GET", "/api/courts", null, Map.of()).body());
        LocalDateTime start = LocalDateTime.parse("2031-01-01T10:00:00");

        TestResponse first = reserve(token, courtId, start, start.plusHours(1));
        TestResponse overlap = reserve(token, courtId, start.plusMinutes(30), start.plusHours(2));

        assertEquals(201, first.statusCode());
        assertEquals(409, overlap.statusCode());
    }

    @Test
    void matchCanBeCreatedAndScored() throws Exception {
        String token = login("ana@demo.rs", "password");
        long playerId = extractLong(send("GET", "/api/auth/me", null, Map.of("X-Auth-Token", token)).body(), "\"id\":(\\d+)");
        long courtId = firstId(send("GET", "/api/courts", null, Map.of()).body());

        TestResponse created = send("POST", "/api/matches", json(
            "title", "Integration test match",
            "startTime", "2031-02-01T18:00:00",
            "courtId", courtId
        ), Map.of("X-Auth-Token", token));
        long matchId = extractLong(created.body(), "\"id\":(\\d+)");

        TestResponse scored = send("PATCH", "/api/matches/" + matchId + "/score", json(
            "score", "6:4 6:4",
            "winnerId", playerId
        ), Map.of("X-Auth-Token", token));

        assertEquals(201, created.statusCode());
        assertEquals(200, scored.statusCode());
        assertTrue(scored.body().contains("\"status\":\"PLAYED\""));
        assertTrue(scored.body().contains("\"score\":\"6:4 6:4\""));
    }

    @Test
    void matchCreationRejectsInvalidParticipants() throws Exception {
        String token = login("ana@demo.rs", "password");
        long playerId = extractLong(send("GET", "/api/auth/me", null, Map.of("X-Auth-Token", token)).body(), "\"id\":(\\d+)");
        long courtId = firstId(send("GET", "/api/courts", null, Map.of()).body());
        long clubId = extractLong(send("GET", "/api/courts", null, Map.of()).body(), "\"club\":\\{\"id\":(\\d+)");

        TestResponse samePlayers = send("POST", "/api/matches", json(
            "title", "Invalid same players",
            "startTime", "2031-03-01T18:00:00",
            "playerAId", playerId,
            "playerBId", playerId,
            "courtId", courtId
        ), Map.of("X-Auth-Token", token));
        TestResponse clubAsPlayer = send("POST", "/api/matches", json(
            "title", "Invalid club player",
            "startTime", "2031-03-01T19:00:00",
            "playerAId", clubId,
            "playerBId", playerId,
            "courtId", courtId
        ), Map.of("X-Auth-Token", token));

        assertEquals(400, samePlayers.statusCode());
        assertEquals(400, clubAsPlayer.statusCode());
    }

    @Test
    void adminCanBanUserAndRegularUserCannotReadAdminList() throws Exception {
        String adminToken = login("admin@demo.rs", "password");
        String regularToken = login("ana@demo.rs", "password");
        String email = "banned-" + System.nanoTime() + "@demo.rs";
        long userId = register(email, "Banned User", "password", "PLAYER");

        TestResponse forbidden = send("GET", "/api/admin/users", null, Map.of("X-Auth-Token", regularToken));
        TestResponse banned = send("PATCH", "/api/admin/users/" + userId + "/ban", "", Map.of("X-Auth-Token", adminToken));
        TestResponse bannedLogin = send("POST", "/api/auth/login", json(
            "email", email,
            "password", "password"
        ), Map.of());

        assertEquals(403, forbidden.statusCode());
        assertEquals(200, banned.statusCode());
        assertTrue(banned.body().contains("\"banned\":true"));
        assertEquals(401, bannedLogin.statusCode());
    }

    @Test
    void playersEndpointReturnsActivePlayersOnly() throws Exception {
        String adminToken = login("admin@demo.rs", "password");
        long hiddenPlayerId = register("hidden-player-" + System.nanoTime() + "@demo.rs", "Hidden Player", "password", "PLAYER");
        send("PATCH", "/api/admin/users/" + hiddenPlayerId + "/ban", "", Map.of("X-Auth-Token", adminToken));

        TestResponse players = send("GET", "/api/players", null, Map.of());

        assertEquals(200, players.statusCode());
        assertTrue(players.body().contains("\"displayName\":\"Ana Markovic\""));
        assertTrue(players.body().contains("\"displayName\":\"Milos Petrovic\""));
        assertFalse(players.body().contains("Hidden Player"));
        assertFalse(players.body().contains("TK Centar"));
        assertFalse(players.body().contains("\"email\""));
        assertFalse(players.body().contains("twoFactorEnabled"));
    }

    @Test
    void exportEndpointsReturnCsvAndPdf() throws Exception {
        TestResponse csv = send("GET", "/api/export/rankings.csv", null, Map.of());
        TestResponse pdf = send("GET", "/api/export/matches.pdf", null, Map.of());

        assertEquals(200, csv.statusCode());
        assertTrue(csv.body().startsWith("playerId,displayName,wins,played,losses,winRate"));
        assertEquals(200, pdf.statusCode());
        assertTrue(pdf.body().startsWith("%PDF-1.4"));
    }

    private long register(String email, String displayName, String password, String accountType) throws Exception {
        TestResponse response = send("POST", "/api/auth/register", json(
            "email", email,
            "displayName", displayName,
            "password", password,
            "accountType", accountType
        ), Map.of());
        assertEquals(200, response.statusCode());
        return extractLong(response.body(), "\"id\":(\\d+)");
    }

    private String login(String email, String password) throws Exception {
        TestResponse response = send("POST", "/api/auth/login", json(
            "email", email,
            "password", password
        ), Map.of());
        assertEquals(200, response.statusCode());
        String token = extractString(response.body(), "\"token\":\"([^\"]+)\"");
        assertNotNull(token);
        return token;
    }

    private TestResponse reserve(String token, long courtId, LocalDateTime startsAt, LocalDateTime endsAt) throws Exception {
        return send("POST", "/api/courts/reservations", json(
            "courtId", courtId,
            "startsAt", startsAt.toString(),
            "endsAt", endsAt.toString()
        ), Map.of("X-Auth-Token", token));
    }

    private TestResponse send(String method, String path, String body, Map<String, String> headers)
        throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        headers.forEach(builder::header);
        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.header("Content-Type", "application/json");
            builder.method(method, HttpRequest.BodyPublishers.ofString(body));
        }
        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return new TestResponse(response.statusCode(), response.body());
    }

    private String json(Object... entries) {
        StringBuilder body = new StringBuilder("{");
        for (int i = 0; i < entries.length; i += 2) {
            if (i > 0) {
                body.append(',');
            }
            body.append('"').append(entries[i]).append('"').append(':');
            Object value = entries[i + 1];
            if (value instanceof Number || value instanceof Boolean) {
                body.append(value);
            } else {
                body.append('"').append(String.valueOf(value).replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
            }
        }
        return body.append('}').toString();
    }

    private long firstId(String body) {
        return extractLong(body, "\"id\":(\\d+)");
    }

    private long extractLong(String body, String pattern) {
        return Long.parseLong(extractString(body, pattern));
    }

    private String extractString(String body, String pattern) {
        Matcher matcher = Pattern.compile(pattern).matcher(body);
        if (!matcher.find()) {
            throw new AssertionError("Pattern not found: " + pattern + " in " + body);
        }
        return matcher.group(1);
    }

    private record TestResponse(int statusCode, String body) {
    }
}
