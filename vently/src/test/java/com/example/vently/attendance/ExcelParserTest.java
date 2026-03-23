package com.example.vently.attendance;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.example.vently.event.Event;
import com.example.vently.user.User;

@ExtendWith(MockitoExtension.class)
class ExcelParserTest {

    private ExcelParser excelParser;

    @BeforeEach
    void setUp() {
        excelParser = new ExcelParser();
    }

    @Test
    void testParseAttendanceFile_ValidExcel_ShouldParseCodes() throws IOException {
        // Arrange
        XSSFWorkbook workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Attendance");
        
        // Create header row
        var headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Volunteer Name");
        headerRow.createCell(1).setCellValue("Attendance Code");
        headerRow.createCell(2).setCellValue("Status");
        
        // Create data rows
        var row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("John Doe");
        row1.createCell(1).setCellValue("ABC123XYZ");
        row1.createCell(2).setCellValue("Y");
        
        var row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("Jane Smith");
        row2.createCell(1).setCellValue("DEF456UVW");
        row2.createCell(2).setCellValue("N");
        
        // Convert workbook to byte array
        var outputStream = new java.io.ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        byte[] excelData = outputStream.toByteArray();
        MultipartFile file = new MockMultipartFile(
            "attendance.xlsx",
            "attendance.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            excelData
        );

        // Act
        List<String> codes = excelParser.parseAttendanceFile(file);

        // Assert
        assertNotNull(codes);
        assertEquals(2, codes.size());
        assertEquals("ABC123XYZ", codes.get(0));
        assertEquals("DEF456UVW", codes.get(1));
    }

    @Test
    void testParseAttendanceFile_ExcelWithNumericCodes_ShouldParseAsString() throws IOException {
        // Arrange
        XSSFWorkbook workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Attendance");
        
        var headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Volunteer Name");
        headerRow.createCell(1).setCellValue("Attendance Code");
        headerRow.createCell(2).setCellValue("Status");
        
        var row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("John Doe");
        row1.createCell(1).setCellValue(12345678); // Numeric code
        row1.createCell(2).setCellValue("Y");
        
        var outputStream = new java.io.ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        byte[] excelData = outputStream.toByteArray();
        MultipartFile file = new MockMultipartFile(
            "attendance.xlsx",
            "attendance.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            excelData
        );

        // Act
        List<String> codes = excelParser.parseAttendanceFile(file);

        // Assert
        assertNotNull(codes);
        assertEquals(1, codes.size());
        assertEquals("12345678", codes.get(0)); // Should convert numeric to string
    }

    @Test
    void testParseAttendanceFile_EmptyExcelSheet_ShouldThrowException() throws IOException {
        // Arrange
        XSSFWorkbook workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Attendance");
        // Empty sheet - no rows
        
        var outputStream = new java.io.ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        byte[] excelData = outputStream.toByteArray();
        MultipartFile file = new MockMultipartFile(
            "attendance.xlsx",
            "attendance.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            excelData
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            excelParser.parseAttendanceFile(file);
        });
    }

    @Test
    void testParseAttendanceFile_EmptyCodeCells_ShouldThrowException() throws IOException {
        // Arrange
        XSSFWorkbook workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Attendance");
        
        var headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Volunteer Name");
        headerRow.createCell(1).setCellValue("Attendance Code");
        headerRow.createCell(2).setCellValue("Status");
        
        var row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("John Doe");
        // Missing code cell
        row1.createCell(2).setCellValue("Y");
        
        var outputStream = new java.io.ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        byte[] excelData = outputStream.toByteArray();
        MultipartFile file = new MockMultipartFile(
            "attendance.xlsx",
            "attendance.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            excelData
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            excelParser.parseAttendanceFile(file);
        });
    }

    @Test
    void testParseAttendanceFile_NoValidCodes_ShouldThrowException() throws IOException {
        // Arrange
        XSSFWorkbook workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Attendance");
        
        var headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Volunteer Name");
        headerRow.createCell(1).setCellValue("Attendance Code");
        headerRow.createCell(2).setCellValue("Status");
        
        // Only header row, no data rows
        
        var outputStream = new java.io.ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        byte[] excelData = outputStream.toByteArray();
        MultipartFile file = new MockMultipartFile(
            "attendance.xlsx",
            "attendance.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            excelData
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            excelParser.parseAttendanceFile(file);
        });
    }

    @Test
    void testParseAttendanceFile_FileTooLarge_ShouldThrowException() {
        // Arrange
        byte[] largeFile = new byte[6 * 1024 * 1024]; // 6MB > 5MB limit
        MultipartFile file = new MockMultipartFile(
            "attendance.xlsx",
            "attendance.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            largeFile
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            excelParser.parseAttendanceFile(file);
        });
    }

    @Test
    void testParseAttendanceFile_UnsupportedFileType_ShouldThrowException() {
        // Arrange
        byte[] fileData = "test data".getBytes();
        MultipartFile file = new MockMultipartFile(
            "attendance.txt",
            "attendance.txt",
            "text/plain",
            fileData
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            excelParser.parseAttendanceFile(file);
        });
    }

    @Test
    void testParseAttendanceFile_EmptyFile_ShouldThrowException() {
        // Arrange
        byte[] emptyData = new byte[0];
        MultipartFile file = new MockMultipartFile(
            "attendance.xlsx",
            "attendance.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            emptyData
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            excelParser.parseAttendanceFile(file);
        });
    }

    @Test
    void testGenerateAttendanceTemplate_ShouldGenerateExcelFile() {
        // Arrange
        User volunteer1 = User.builder()
                .id(1L)
                .fullName("John Doe")
                .build();
        
        User volunteer2 = User.builder()
                .id(2L)
                .fullName("Jane Smith")
                .build();
        
        Event event = Event.builder()
                .id(1L)
                .title("Test Event")
                .build();
        
        AttendanceCode code1 = AttendanceCode.builder()
                .id(1L)
                .event(event)
                .volunteer(volunteer1)
                .code("ABC123XYZ")
                .build();
        
        AttendanceCode code2 = AttendanceCode.builder()
                .id(2L)
                .event(event)
                .volunteer(volunteer2)
                .code("DEF456UVW")
                .build();
        code2.markAttendance(User.builder().id(3L).fullName("Organizer").build());
        
        List<AttendanceCode> attendanceCodes = List.of(code1, code2);

        // Act
        byte[] result = excelParser.generateAttendanceTemplate(attendanceCodes);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        
        // Verify the Excel file can be read
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            var sheet = workbook.getSheetAt(0);
            assertNotNull(sheet);
            assertEquals("Attendance", sheet.getSheetName());
            
            // Check header row
            var headerRow = sheet.getRow(0);
            assertEquals("Volunteer Name", headerRow.getCell(0).getStringCellValue());
            assertEquals("Attendance Code", headerRow.getCell(1).getStringCellValue());
            assertEquals("Status", headerRow.getCell(2).getStringCellValue());
            
            // Check data rows
            var row1 = sheet.getRow(1);
            assertEquals("John Doe", row1.getCell(0).getStringCellValue());
            assertEquals("ABC123XYZ", row1.getCell(1).getStringCellValue());
            assertEquals("Not Marked", row1.getCell(2).getStringCellValue());
            
            var row2 = sheet.getRow(2);
            assertEquals("Jane Smith", row2.getCell(0).getStringCellValue());
            assertEquals("DEF456UVW", row2.getCell(1).getStringCellValue());
            assertEquals("Present", row2.getCell(2).getStringCellValue());
        } catch (IOException e) {
            fail("Failed to parse generated Excel file: " + e.getMessage());
        }
    }

    @Test
    void testGenerateAttendanceTemplate_EmptyList_ShouldGenerateEmptyTemplate() {
        // Arrange
        List<AttendanceCode> attendanceCodes = List.of();

        // Act
        byte[] result = excelParser.generateAttendanceTemplate(attendanceCodes);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        
        // Verify the Excel file can be read
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            var sheet = workbook.getSheetAt(0);
            assertNotNull(sheet);
            assertEquals("Attendance", sheet.getSheetName());
            
            // Check header row exists
            var headerRow = sheet.getRow(0);
            assertNotNull(headerRow);
            assertEquals("Volunteer Name", headerRow.getCell(0).getStringCellValue());
            assertEquals("Attendance Code", headerRow.getCell(1).getStringCellValue());
            assertEquals("Status", headerRow.getCell(2).getStringCellValue());
            
            // No data rows
            assertNull(sheet.getRow(1));
        } catch (IOException e) {
            fail("Failed to parse generated Excel file: " + e.getMessage());
        }
    }
}