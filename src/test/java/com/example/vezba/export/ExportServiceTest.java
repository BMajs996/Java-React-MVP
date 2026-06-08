package com.example.vezba.export;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.example.vezba.model.AccountType;
import com.example.vezba.model.AppUser;
import com.example.vezba.model.GameMatch;
import com.example.vezba.model.MatchStatus;
import com.example.vezba.model.UserRole;
import com.example.vezba.service.MatchService;
import com.example.vezba.service.RankingService;
import com.example.vezba.web.ApiDtos;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {
    @Mock
    private MatchService matchService;

    @Mock
    private RankingService rankingService;

    @Mock
    private CsvExportService csvExportService;

    @Mock
    private PdfExportService pdfExportService;

    @InjectMocks
    private ExportService exportService;

    @Test
    void csvExportsMatchesThroughMatchService() {
        GameMatch match = match();
        when(matchService.list("all", null)).thenReturn(List.of(match));
        when(csvExportService.matches(List.of(ApiDtos.MatchDto.from(match)))).thenReturn("matches,csv\n");

        assertEquals("matches,csv\n", exportService.csv("matches"));
    }

    @Test
    void csvExportsRankingsThroughRankingService() {
        ApiDtos.RankingDto ranking = new ApiDtos.RankingDto(1L, "Ana", 3, 4, 1, 0.75);
        when(rankingService.ranking()).thenReturn(List.of(ranking));
        when(csvExportService.rankings(List.of(ranking))).thenReturn("rankings,csv\n");

        assertEquals("rankings,csv\n", exportService.csv("rankings"));
    }

    @Test
    void pdfExportsRankingsThroughRankingService() {
        ApiDtos.RankingDto ranking = new ApiDtos.RankingDto(1L, "Ana", 3, 4, 1, 0.75);
        byte[] pdf = "%PDF-1.4".getBytes();
        when(rankingService.ranking()).thenReturn(List.of(ranking));
        when(pdfExportService.simpleTable("rankings", List.of("Ana | W:3 L:1 Played:4"))).thenReturn(pdf);

        assertArrayEquals(pdf, exportService.pdf("rankings"));
    }

    @Test
    void unsupportedDatasetReturnsNotFound() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> exportService.csv("users"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    private GameMatch match() {
        AppUser player = new AppUser("ana@demo.rs", "Ana", "hash", AccountType.PLAYER, UserRole.USER);
        ReflectionTestUtils.setField(player, "id", 1L);
        GameMatch match = new GameMatch("Finale", LocalDateTime.parse("2031-01-01T18:00:00"), player, null, player, null);
        ReflectionTestUtils.setField(match, "id", 10L);
        match.setStatus(MatchStatus.PLAYED);
        match.setScore("6:4 6:4");
        match.setWinner(player);
        return match;
    }
}
