package com.example.pleasework.service;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.stereotype.Service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelService {

    public byte[] lockNewlyFilledCells(byte[] excelBytes) throws IOException {
        if (excelBytes == null || excelBytes.length == 0) return excelBytes;

        try (InputStream is = new ByteArrayInputStream(excelBytes);
             Workbook workbook = isXLS(excelBytes)
                     ? new HSSFWorkbook(new POIFSFileSystem(is))
                     : new XSSFWorkbook(is)) {

            // Cache: pour chaque style d’origine (index), on garde 2 clones (unlocked/locked)
            Map<Short, CellStyle[]> styleCache = new HashMap<>();

            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    if (row == null) continue;

                    short lastCell = row.getLastCellNum();
                    if (lastCell < 0) continue; // ligne vide sans cellules connues

                    for (int c = 0; c < lastCell; c++) {
                        // Crée les vides pour pouvoir leur appliquer "Unlocked"
                        Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

                        // Style d’origine (peut être le style par défaut index=0)
                        CellStyle original = cell.getCellStyle();
                        short key = original == null ? 0 : original.getIndex();

                        // Récupère/Crée le duo (unlocked, locked) pour CE style d’origine
                        CellStyle[] pair = styleCache.get(key);
                        if (pair == null) {
                            CellStyle unlocked = workbook.createCellStyle();
                            CellStyle locked = workbook.createCellStyle();
                            if (original != null) {
                                unlocked.cloneStyleFrom(original);
                                locked.cloneStyleFrom(original);
                            }
                            unlocked.setLocked(false);
                            locked.setLocked(true);
                            pair = new CellStyle[]{unlocked, locked};
                            styleCache.put(key, pair);
                        }

                        // Applique locked si non vide, sinon unlocked
                        cell.setCellStyle(isEmpty(cell) ? pair[0] : pair[1]);
                    }
                }

                // Active la protection : seules les cellules "Locked" seront modifiées
                sheet.protectSheet("readonly");
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            workbook.write(os);
            return os.toByteArray();
        }
    }

    private boolean isXLS(byte[] data) {
        return data.length >= 8
                && data[0] == (byte) 0xD0
                && data[1] == (byte) 0xCF
                && data[2] == (byte) 0x11
                && data[3] == (byte) 0xE0;
    }

    private boolean isEmpty(Cell cell) {
        if (cell == null) return true;
        CellType type = cell.getCellType();
        switch (type) {
            case BLANK:
                return true;
            case STRING:
                String s = cell.getStringCellValue();
                return s == null || s.trim().isEmpty();
            case FORMULA:
                CellType cached = cell.getCachedFormulaResultType();
                if (cached == CellType.STRING) {
                    String fs = cell.getStringCellValue();
                    return fs == null || fs.trim().isEmpty();
                }
                // On considère toute autre formule comme non vide
                return false;
            case NUMERIC:
            case BOOLEAN:
            case ERROR:
                return false;
            default:
                return false;
        }
    }

}
