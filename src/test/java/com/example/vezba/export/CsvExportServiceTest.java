package com.example.vezba.export;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.vezba.model.AccountType;
import com.example.vezba.model.UserRole;
import com.example.vezba.web.ApiDtos;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class CsvExportServiceTest {
    private final CsvExportService csvExportService = new CsvExportService();

    @Test
    void matchesExportEscapesCsvCells() {
        ApiDtos.UserDto player = new ApiDtos.UserDto(1L, "ana@demo.rs", "Ana \"Winner\"", AccountType.PLAYER,
            UserRole.USER, false, false, null, null, null, null);
        ApiDtos.CourtDto court = new ApiDtos.CourtDto(1L, "Teren 1", "Beograd", "Sljaka", true, null);
        ApiDtos.MatchDto match = new ApiDtos.MatchDto(1L, "Finale \"A\"", LocalDateTime.parse("2026-06-04T18:00:00"),
            "6:4 6:3", "PLAYED", player, null, player, player, court);

        String csv = csvExportService.matches(List.of(match));

        assertTrue(csv.startsWith("id,title,startTime,court,playerA,playerB,status,score,winner\n"));
        assertTrue(csv.contains("\"Finale \"\"A\"\"\""));
        assertTrue(csv.contains("\"Ana \"\"Winner\"\"\""));
        assertTrue(csv.contains("\"6:4 6:3\""));
    }

    @Test
    void rankingsExportContainsWinLossColumns() {
        String csv = csvExportService.rankings(List.of(new ApiDtos.RankingDto(7L, "Milos", 3, 5, 2, 0.6)));

        assertTrue(csv.startsWith("playerId,displayName,wins,played,losses,winRate\n"));
        assertTrue(csv.contains("7,\"Milos\",3,5,2,0.6"));
    }
}
