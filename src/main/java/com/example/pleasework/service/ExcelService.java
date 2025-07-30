package com.example.pleasework.service;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExcelService {

    public byte[] lockNewlyFilledCells(byte[] excelBytes) throws IOException {
        InputStream is = new ByteArrayInputStream(excelBytes);
        Workbook workbook;

        if (isXLS(excelBytes)) {
            workbook = new HSSFWorkbook(new POIFSFileSystem(is));
        } else {
            workbook = new XSSFWorkbook(is);
        }

        Map<CellStyle, CellStyle> lockedStyleCache = new HashMap<>();

        for (Sheet sheet : workbook) {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (cell == null) continue;

                    boolean isEmpty = cell.getCellType() == CellType.BLANK
                            || (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty());

                    // Vérifie si la cellule est déjà lockée
                    if (!isEmpty && !cell.getCellStyle().getLocked()) {
                        // 🔐 Elle est remplie et pas encore lockée : on la modifie

                        CellStyle original = cell.getCellStyle();
                        CellStyle lockedStyle = lockedStyleCache.get(original);

                        if (lockedStyle == null) {
                            lockedStyle = workbook.createCellStyle();
                            lockedStyle.cloneStyleFrom(original);
                            lockedStyle.setLocked(true);
                            lockedStyleCache.put(original, lockedStyle);
                        }

                        cell.setCellStyle(lockedStyle);
                    }
                    // ❌ NE PAS modifier les autres styles (vides ou déjà lockées)
                }
            }

            sheet.protectSheet("readonly");
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        workbook.write(os);
        workbook.close();
        return os.toByteArray();
    }

    private boolean isXLS(byte[] data) {
        return data.length >= 8
                && data[0] == (byte) 0xD0
                && data[1] == (byte) 0xCF
                && data[2] == (byte) 0x11
                && data[3] == (byte) 0xE0;
    }

    private boolean isNotEmpty(Cell cell) {
        if (cell == null) return false;
        if (cell.getCellType() == CellType.BLANK) return false;
        if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty()) return false;
        return true;
    }
}
