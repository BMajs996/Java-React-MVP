package com.example.vezba.web;

import com.example.vezba.export.ExportService;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/export")
public class ExportController {
    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/{dataset}.csv")
    public ResponseEntity<byte[]> csv(@PathVariable String dataset) {
        String body = exportService.csv(dataset);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(dataset + ".csv").build().toString())
            .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
            .body(body.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/{dataset}.pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable String dataset) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(dataset + ".pdf").build().toString())
            .contentType(MediaType.APPLICATION_PDF)
            .body(exportService.pdf(dataset));
    }
}
