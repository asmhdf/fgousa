package com.example.pleasework.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Service
public class LibreOfficeConvertService {

    // Adapte pour Linux: "soffice"
    private static final String SOFFICE =
            "C:\\Program Files\\LibreOffice\\program\\soffice.exe";

    public byte[] excelToPdf(byte[] excelBytes, String originalFilename) throws IOException, InterruptedException {
        if (excelBytes == null || excelBytes.length == 0) {
            throw new IllegalArgumentException("Fichier Excel vide");
        }

        String ext = ".xlsx";
        if (originalFilename != null && originalFilename.toLowerCase().endsWith(".xls")) {
            ext = ".xls";
        }

        Path tmpDir = Files.createTempDirectory("loffice_");
        Path inFile = tmpDir.resolve("input" + ext);
        Files.write(inFile, excelBytes);

        // Commande LibreOffice
        List<String> cmd = Arrays.asList(
                SOFFICE,
                "--headless", "--nologo", "--nofirststartwizard",
                "--convert-to", "pdf:calc_pdf_Export",
                "--outdir", tmpDir.toString(),
                inFile.toString()
        );

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = pb.start();

        // (optionnel) lire la sortie pour debug
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            while (br.readLine() != null) { /* ignore ou log */ }
        }

        boolean finished = p.waitFor(60, java.util.concurrent.TimeUnit.SECONDS);
        if (!finished || p.exitValue() != 0) {
            // nettoyage
            Files.deleteIfExists(inFile);
            throw new IllegalStateException("LibreOffice n'a pas pu convertir le fichier (timeout/exit " + p.exitValue() + ")");
        }

        Path outFile = tmpDir.resolve("input.pdf");
        if (!Files.exists(outFile)) {
            // LibreOffice peut renommer différemment selon le nom source, on tente un fallback
            String base = originalFilename != null ? originalFilename.replaceAll("(?i)\\.(xlsx|xls)$", "") : "input";
            Path alt = tmpDir.resolve(base + ".pdf");
            if (Files.exists(alt)) outFile = alt;
        }

        byte[] pdf = Files.readAllBytes(outFile);

        // nettoyage
        try { Files.deleteIfExists(inFile); } catch (Exception ignore) {}
        try { Files.deleteIfExists(outFile); } catch (Exception ignore) {}
        try { Files.deleteIfExists(tmpDir); } catch (Exception ignore) {}

        return pdf;
    }
}
