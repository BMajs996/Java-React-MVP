package com.example.vezba.export;

import com.example.vezba.service.MatchService;
import com.example.vezba.service.RankingService;
import com.example.vezba.web.ApiDtos;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ExportService {
    private final MatchService matchService;
    private final RankingService rankingService;
    private final CsvExportService csvExportService;
    private final PdfExportService pdfExportService;

    public ExportService(MatchService matchService, RankingService rankingService, CsvExportService csvExportService,
                         PdfExportService pdfExportService) {
        this.matchService = matchService;
        this.rankingService = rankingService;
        this.csvExportService = csvExportService;
        this.pdfExportService = pdfExportService;
    }

    public String csv(String dataset) {
        return switch (dataset) {
            case "matches" -> csvExportService.matches(matches());
            case "rankings" -> csvExportService.rankings(rankings());
            default -> throw unsupportedDataset(dataset);
        };
    }

    public byte[] pdf(String dataset) {
        List<String> lines = switch (dataset) {
            case "matches" -> matches().stream()
                .map(match -> match.title() + " | " + match.startTime() + " | " + match.status() + " | "
                    + (match.score() == null ? "-" : match.score()))
                .toList();
            case "rankings" -> rankings().stream()
                .map(ranking -> ranking.displayName() + " | W:" + ranking.wins() + " L:" + ranking.losses()
                    + " Played:" + ranking.played())
                .toList();
            default -> throw unsupportedDataset(dataset);
        };
        return pdfExportService.simpleTable(dataset, lines);
    }

    private List<ApiDtos.MatchDto> matches() {
        return matchService.list("all", null).stream().map(ApiDtos.MatchDto::from).toList();
    }

    private List<ApiDtos.RankingDto> rankings() {
        return rankingService.ranking();
    }

    private ResponseStatusException unsupportedDataset(String dataset) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Unsupported export dataset: " + dataset);
    }
}
