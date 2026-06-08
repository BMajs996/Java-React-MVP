package com.example.vezba.export;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PdfExportService {
    public byte[] simpleTable(String title, List<String> lines) {
        List<String> pageLines = new ArrayList<>();
        pageLines.add(title);
        pageLines.add("");
        pageLines.addAll(lines);
        StringBuilder stream = new StringBuilder("BT\n/F1 12 Tf\n50 780 Td\n");
        for (String line : pageLines) {
            stream.append('(').append(escape(line)).append(") Tj\n0 -18 Td\n");
        }
        stream.append("ET");
        byte[] content = stream.toString().getBytes(StandardCharsets.UTF_8);

        List<byte[]> objects = List.of(
            bytes("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n"),
            bytes("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n"),
            bytes("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                + "/Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n"),
            bytes("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n"),
            bytes("5 0 obj\n<< /Length " + content.length + " >>\nstream\n"
                + new String(content, StandardCharsets.UTF_8) + "\nendstream\nendobj\n")
        );

        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        for (byte[] object : objects) {
            offsets.add(pdf.toString().getBytes(StandardCharsets.UTF_8).length);
            pdf.append(new String(object, StandardCharsets.UTF_8));
        }
        int xref = pdf.toString().getBytes(StandardCharsets.UTF_8).length;
        pdf.append("xref\n0 ").append(objects.size() + 1).append('\n');
        pdf.append("0000000000 65535 f \n");
        for (Integer offset : offsets) {
            pdf.append(String.format("%010d 00000 n \n", offset));
        }
        pdf.append("trailer\n<< /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xref).append("\n%%EOF\n");
        return pdf.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
