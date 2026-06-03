package com.example.vezba.web;

import com.example.vezba.export.CsvExportService;
import com.example.vezba.export.PdfExportService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/export")
public class ExportController {
    private final MatchController matchController;
    private final RankingController rankingController;
    private final CsvExportService csvExportService;
    private final PdfExportService pdfExportService;

    public ExportController(MatchController matchController, RankingController rankingController,
                            CsvExportService csvExportService, PdfExportService pdfExportService) {
        this.matchController = matchController;
        this.rankingController = rankingController;
        this.csvExportService = csvExportService;
        this.pdfExportService = pdfExportService;
    }

    @GetMapping("/{dataset}.csv")
    public ResponseEntity<byte[]> csv(@PathVariable String dataset) {
        String body = switch (dataset) {
            case "matches" -> csvExportService.matches(matchController.list("all", null));
            case "rankings" -> csvExportService.rankings(rankingController.ranking());
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unsupported export dataset: " + dataset);
        };
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(dataset + ".csv").build().toString())
            .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
            .body(body.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/{dataset}.pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable String dataset) {
        List<String> lines = switch (dataset) {
            case "matches" -> matchController.list("all", null).stream()
                .map(match -> match.title() + " | " + match.startTime() + " | " + match.status() + " | " + (match.score() == null ? "-" : match.score()))
                .toList();
            case "rankings" -> rankingController.ranking().stream()
                .map(ranking -> ranking.displayName() + " | W:" + ranking.wins() + " L:" + ranking.losses() + " Played:" + ranking.played())
                .toList();
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unsupported export dataset: " + dataset);
        };
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(dataset + ".pdf").build().toString())
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfExportService.simpleTable(dataset, lines));
    }
}
