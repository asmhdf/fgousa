package com.example.pleasework.service;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.apache.poi.ss.usermodel.*;

import java.io.*;

public class ExcelOperatorVersionGenerator {

    public static byte[] generateProtectedCopyForOperator(File originalFile) throws Exception {
        try (InputStream in = new FileInputStream(originalFile);
             HSSFWorkbook workbook = new HSSFWorkbook(in)) {

            Sheet sheet = workbook.getSheetAt(0);

            CellStyle unlockedStyle = workbook.createCellStyle();
            unlockedStyle.setLocked(false);

            for (Row row : sheet) {
                Cell cell = row.getCell(1); // column B (index 1)
                if (cell == null) {
                    cell = row.createCell(1);
                }
                cell.setCellStyle(unlockedStyle);
            }

            sheet.protectSheet("operator-protect");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
