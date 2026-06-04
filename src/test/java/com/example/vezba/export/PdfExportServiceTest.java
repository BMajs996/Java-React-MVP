package com.example.vezba.export;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class PdfExportServiceTest {
    private final PdfExportService pdfExportService = new PdfExportService();

    @Test
    void simpleTableProducesPdfDocument() {
        byte[] pdf = pdfExportService.simpleTable("rankings", List.of("Ana | W:3 L:1"));
        String body = new String(pdf, StandardCharsets.UTF_8);

        assertTrue(body.startsWith("%PDF-1.4"));
        assertTrue(body.contains("/Type /Catalog"));
        assertTrue(body.contains("(rankings) Tj"));
        assertTrue(body.contains("(Ana | W:3 L:1) Tj"));
        assertTrue(body.endsWith("%%EOF\n"));
    }
}
