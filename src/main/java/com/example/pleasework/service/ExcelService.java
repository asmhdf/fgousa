package com.example.pleasework.service;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class ExcelService {

    public byte[] lockFilledCells(byte[] excelBytes) throws IOException {
        InputStream is = new ByteArrayInputStream(excelBytes);
        Workbook workbook;

        // ➤ Détection du format XLS vs XLSX
        if (isXLS(excelBytes)) {
            workbook = new HSSFWorkbook(new POIFSFileSystem(is));
        } else {
            workbook = new XSSFWorkbook(is);
        }

        for (Sheet sheet : workbook) {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (cell == null) continue;

                    CellStyle originalStyle = cell.getCellStyle();
                    CellStyle newStyle = workbook.createCellStyle();

                    if (originalStyle != null) {
                        newStyle.cloneStyleFrom(originalStyle);
                    }

                    if (isNotEmpty(cell)) {
                        newStyle.setLocked(true);  // ➤ Verrouille les cellules pleines
                    } else {
                        newStyle.setLocked(false); // ➤ Déverrouille les cellules vides
                    }

                    cell.setCellStyle(newStyle);
                }
            }

            // ➤ Protège uniquement les cellules marquées Locked
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
