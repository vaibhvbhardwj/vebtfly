package com.example.vently.attendance;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ExcelParser {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] SUPPORTED_EXTENSIONS = {".xlsx", ".xls"};

    /**
     * Parse attendance file and extract attendance codes
     * Requirements: 10.1, 10.2, 10.3, 10.5, 10.6, 10.7
     * 
     * Expected format:
     * Row 1: Headers (Volunteer Name, Attendance Code, Status)
     * Row 2+: Data rows
     */
    public List<String> parseAttendanceFile(MultipartFile file) {
        log.info("Parsing attendance file: {}", file.getOriginalFilename());

        // Validate file
        validateFile(file);

        List<String> codes = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            
            // Validate header row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Excel file is empty");
            }

            // Skip header row and process data rows
            int rowCount = sheet.getPhysicalNumberOfRows();
            for (int i = 1; i < rowCount; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                try {
                    // Get attendance code from column B (index 1)
                    Cell codeCell = row.getCell(1);
                    if (codeCell == null) {
                        errors.add("Row " + (i + 1) + ": Missing attendance code");
                        continue;
                    }

                    String code = getCellValueAsString(codeCell).trim();
                    if (code.isEmpty()) {
                        errors.add("Row " + (i + 1) + ": Empty attendance code");
                        continue;
                    }

                    codes.add(code);

                } catch (Exception e) {
                    errors.add("Row " + (i + 1) + ": " + e.getMessage());
                }
            }

            if (!errors.isEmpty()) {
                throw new IllegalArgumentException("Validation errors: " + String.join("; ", errors));
            }

            if (codes.isEmpty()) {
                throw new IllegalArgumentException("No valid attendance codes found in file");
            }

            log.info("Parsed {} attendance codes from file", codes.size());
            return codes;

        } catch (IOException e) {
            log.error("Failed to parse Excel file", e);
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage());
        }
    }

    /**
     * Generate attendance template with volunteer names and codes
     * Requirements: 10.2, 10.5, 10.6, 10.7
     */
    public byte[] generateAttendanceTemplate(List<AttendanceCode> attendanceCodes) {
        log.info("Generating attendance template for {} volunteers", attendanceCodes.size());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Attendance");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Create header row
            Row headerRow = sheet.createRow(0);
            
            Cell nameHeader = headerRow.createCell(0);
            nameHeader.setCellValue("Volunteer Name");
            nameHeader.setCellStyle(headerStyle);

            Cell codeHeader = headerRow.createCell(1);
            codeHeader.setCellValue("Attendance Code");
            codeHeader.setCellStyle(headerStyle);

            Cell statusHeader = headerRow.createCell(2);
            statusHeader.setCellValue("Status");
            statusHeader.setCellStyle(headerStyle);

            // Add data rows
            int rowNum = 1;
            for (AttendanceCode attendanceCode : attendanceCodes) {
                Row row = sheet.createRow(rowNum++);

                // Volunteer name
                Cell nameCell = row.createCell(0);
                nameCell.setCellValue(attendanceCode.getVolunteer().getFullName());

                // Attendance code
                Cell codeCell = row.createCell(1);
                codeCell.setCellValue(attendanceCode.getCode());

                // Status
                Cell statusCell = row.createCell(2);
                statusCell.setCellValue(attendanceCode.isMarked() ? "Present" : "Not Marked");
            }

            // Auto-size columns
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);

            // Write to output stream
            workbook.write(outputStream);

            log.info("Generated attendance template with {} rows", attendanceCodes.size());
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Failed to generate attendance template", e);
            throw new RuntimeException("Failed to generate attendance template: " + e.getMessage());
        }
    }

    /**
     * Validate uploaded file
     * Requirements: 10.6, 10.7
     */
    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check file size (max 5MB)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }

        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Invalid filename");
        }

        boolean validExtension = false;
        for (String ext : SUPPORTED_EXTENSIONS) {
            if (filename.toLowerCase().endsWith(ext)) {
                validExtension = true;
                break;
            }
        }

        if (!validExtension) {
            throw new IllegalArgumentException("Unsupported file format. Only .xlsx and .xls files are supported");
        }
    }

    /**
     * Get cell value as string regardless of cell type
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // Convert numeric to string (in case code is entered as number)
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
