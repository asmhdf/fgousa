package com.example.pleasework.controller;

import com.example.pleasework.entity.Template;


import com.example.pleasework.repository.TemplateRepository;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Controller
@RestController
@RequestMapping("/template")
public class TemplateController {

    @Autowired
    private TemplateRepository templateRepository;
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadTemplate(@PathVariable Integer id) {
        Template template = templateRepository.findById(id).orElse(null);
        if (template == null || template.getFile() == null) {
            return ResponseEntity.notFound().build();
        }

        ByteArrayResource resource = new ByteArrayResource(template.getFile());

        // Use the stored filename if available, otherwise fallback
        String filename = template.getFilename() != null ? template.getFilename() : "template_" + id + ".xlsx";

        // Determine the MIME type (basic handling for Excel)
        String contentType = filename.endsWith(".xlsx") || filename.endsWith(".xls")
                ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                : "application/octet-stream";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(resource);
    }


    @PostMapping("/update/{id}")
    public String updateTemplate(@PathVariable Integer id, @RequestParam("file") MultipartFile file) throws IOException {
        Template template = templateRepository.findById(id).orElse(null);
        if (template == null) {
            return "redirect:/post";
        }
        System.out.println("Fichier reçu : " + file.getOriginalFilename() + " (" + file.getSize() + " bytes)");

        // Store file content and filename
        template.setFile(file.getBytes());
        template.setFilename(file.getOriginalFilename());
        templateRepository.save(template);

        return "redirect:/post/" + template.getId();
    }
    @GetMapping("/json/{id}")
    public ResponseEntity<List<List<List<String>>>> getExcelAsJson(@PathVariable Integer id) {
        Template template = templateRepository.findById(id).orElse(null);
        if (template == null || template.getFile() == null) {
            return ResponseEntity.notFound().build();
        }

        try (InputStream input = new ByteArrayInputStream(template.getFile());
             Workbook workbook = WorkbookFactory.create(input)) {

            List<List<List<String>>> allSheets = new ArrayList<>();

            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                Sheet sheet = workbook.getSheetAt(s);
                List<List<String>> sheetData = new ArrayList<>();

                for (Row row : sheet) {
                    List<String> rowData = new ArrayList<>();
                    int max = row.getLastCellNum();
                    if (max < 0) continue;

                    for (int i = 0; i < max; i++) {
                        Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        cell.setCellType(CellType.STRING);
                        rowData.add(cell.getStringCellValue());
                    }
                    sheetData.add(rowData);
                }

                allSheets.add(sheetData);
            }

            return ResponseEntity.ok(allSheets);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    @GetMapping("/base64/{id}")
    public ResponseEntity<String> getExcelFileBase64(@PathVariable Integer id) {
        Template template = templateRepository.findById(id).orElse(null);
        if (template == null || template.getFile() == null) {
            return ResponseEntity.notFound().build();
        }

        String base64 = Base64.getEncoder().encodeToString(template.getFile());
        return ResponseEntity.ok(base64);
    }




}
