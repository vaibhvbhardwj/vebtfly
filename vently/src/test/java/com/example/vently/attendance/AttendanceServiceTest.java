package com.example.vently.attendance;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.example.vently.application.Application;
import com.example.vently.application.ApplicationRepository;
import com.example.vently.application.ApplicationStatus;
import com.example.vently.event.Event;
import com.example.vently.event.EventRepository;
import com.example.vently.event.EventStatus;
import com.example.vently.payment.PaymentService;
import com.example.vently.user.Role;
import com.example.vently.user.User;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceCodeRepository attendanceCodeRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private ExcelParser excelParser;

    @Mock
    private com.example.vently.notification.NotificationService notificationService;

    @InjectMocks
    private AttendanceService attendanceService;

    private User organizer;
    private User volunteer;
    private Event testEvent;
    private Application testApplication;
    private AttendanceCode testAttendanceCode;

    @BeforeEach
    void setUp() {
        organizer = User.builder()
                .id(1L)
                .email("organizer@example.com")
                .fullName("Test Organizer")
                .role(Role.ORGANIZER)
                .build();

        volunteer = User.builder()
                .id(2L)
                .email("volunteer@example.com")
                .fullName("Test Volunteer")
                .role(Role.VOLUNTEER)
                .build();

        testEvent = Event.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .location("Test Location")
                .date(LocalDate.now())
                .time(LocalTime.now().minusHours(2)) // Event started 2 hours ago
                .requiredVolunteers(5)
                .paymentPerVolunteer(new BigDecimal("50.00"))
                .status(EventStatus.IN_PROGRESS)
                .organizer(organizer)
                .build();

        testApplication = Application.builder()
                .id(1L)
                .event(testEvent)
                .volunteer(volunteer)
                .status(ApplicationStatus.CONFIRMED)
                .build();

        testAttendanceCode = AttendanceCode.builder()
                .id(1L)
                .event(testEvent)
                .volunteer(volunteer)
                .code("ABC123XYZ")
                .build();
    }

    @Test
    void testGenerateAttendanceCodes_ShouldGenerateUniqueCodes() {
        // Arrange
        List<Application> confirmedApplications = List.of(testApplication);
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(attendanceCodeRepository.countByEventId(1L)).thenReturn(0L);
        when(applicationRepository.findByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED))
                .thenReturn(confirmedApplications);
        when(attendanceCodeRepository.existsByCode(anyString())).thenReturn(false);
        when(attendanceCodeRepository.saveAll(anyList())).thenReturn(List.of(testAttendanceCode));

        // Act
        attendanceService.generateAttendanceCodes(1L);

        // Assert
        verify(attendanceCodeRepository).saveAll(anyList());
        verify(applicationRepository).findByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED);
    }

    @Test
    void testGenerateAttendanceCodes_EventNotInProgress_ShouldThrowException() {
        // Arrange
        testEvent.setStatus(EventStatus.PUBLISHED);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.generateAttendanceCodes(1L);
        });
    }

    @Test
    void testGenerateAttendanceCodes_CodesAlreadyGenerated_ShouldThrowException() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(attendanceCodeRepository.countByEventId(1L)).thenReturn(5L);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.generateAttendanceCodes(1L);
        });
    }

    @Test
    void testGenerateAttendanceCodes_NoConfirmedVolunteers_ShouldThrowException() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(attendanceCodeRepository.countByEventId(1L)).thenReturn(0L);
        when(applicationRepository.findByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED))
                .thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.generateAttendanceCodes(1L);
        });
    }

    @Test
    void testMarkAttendanceByCode_ShouldMarkAttendanceAndTriggerPayment() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(attendanceCodeRepository.findByCode("ABC123XYZ")).thenReturn(Optional.of(testAttendanceCode));
        when(applicationRepository.findByEventIdAndVolunteerId(1L, 2L))
                .thenReturn(Optional.of(testApplication));
        doNothing().when(paymentService).releasePaymentToVolunteer(1L);

        // Act
        attendanceService.markAttendanceByCode(1L, "ABC123XYZ", organizer);

        // Assert
        assertTrue(testAttendanceCode.isMarked());
        assertNotNull(testAttendanceCode.getMarkedAt());
        assertEquals(organizer, testAttendanceCode.getMarkedBy());
        verify(paymentService).releasePaymentToVolunteer(1L);
    }

    @Test
    void testMarkAttendanceByCode_InvalidCode_ShouldThrowException() {
        // Arrange
        // Set event to today at 10:00 AM (started 2 hours ago, so within 24h window)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventStart = now.minusHours(2);
        testEvent.setDate(eventStart.toLocalDate());
        testEvent.setTime(eventStart.toLocalTime());
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(attendanceCodeRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.markAttendanceByCode(1L, "INVALID", organizer);
        });
    }

    @Test
    void testMarkAttendanceByCode_WrongEvent_ShouldThrowException() {
        // Arrange
        // Set event to today at 10:00 AM (started 2 hours ago, so within 24h window)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventStart = now.minusHours(2);
        testEvent.setDate(eventStart.toLocalDate());
        testEvent.setTime(eventStart.toLocalTime());
        
        Event otherEvent = Event.builder().id(2L).build();
        testAttendanceCode.setEvent(otherEvent);
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(attendanceCodeRepository.findByCode("ABC123XYZ")).thenReturn(Optional.of(testAttendanceCode));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.markAttendanceByCode(1L, "ABC123XYZ", organizer);
        });
    }

    @Test
    void testMarkAttendanceByCode_AlreadyMarked_ShouldThrowException() {
        // Arrange
        // Set event to today at 10:00 AM (started 2 hours ago, so within 24h window)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventStart = now.minusHours(2);
        testEvent.setDate(eventStart.toLocalDate());
        testEvent.setTime(eventStart.toLocalTime());
        
        testAttendanceCode.markAttendance(organizer);
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(attendanceCodeRepository.findByCode("ABC123XYZ")).thenReturn(Optional.of(testAttendanceCode));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.markAttendanceByCode(1L, "ABC123XYZ", organizer);
        });
    }

    @Test
    void testMarkAttendanceByCode_NotOrganizer_ShouldThrowException() {
        // Arrange
        User otherUser = User.builder().id(3L).role(Role.ORGANIZER).build();
        testEvent.setOrganizer(otherUser);
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.markAttendanceByCode(1L, "ABC123XYZ", organizer);
        });
    }

    @Test
    void testMarkAttendanceByCode_BeforeEventStart_ShouldThrowException() {
        // Arrange
        testEvent.setDate(LocalDate.now().plusDays(1));
        testEvent.setTime(LocalTime.of(10, 0));
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.markAttendanceByCode(1L, "ABC123XYZ", organizer);
        });
    }

    @Test
    void testMarkAttendanceByCode_After24Hours_ShouldThrowException() {
        // Arrange
        testEvent.setDate(LocalDate.now().minusDays(2));
        testEvent.setTime(LocalTime.of(10, 0));
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.markAttendanceByCode(1L, "ABC123XYZ", organizer);
        });
    }

    @Test
    void testMarkAttendanceByExcel_ShouldMarkMultipleAttendances() {
        // Arrange
        // Set event to today at 10:00 AM (started 2 hours ago, so within 24h window)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventStart = now.minusHours(2);
        testEvent.setDate(eventStart.toLocalDate());
        testEvent.setTime(eventStart.toLocalTime());
        
        MultipartFile mockFile = mock(MultipartFile.class);
        List<String> codes = List.of("ABC123XYZ", "DEF456UVW");
        
        AttendanceCode secondCode = AttendanceCode.builder()
                .id(2L)
                .event(testEvent)
                .volunteer(User.builder().id(3L).build())
                .code("DEF456UVW")
                .build();
        
        Application secondApp = Application.builder()
                .id(2L)
                .event(testEvent)
                .volunteer(User.builder().id(3L).build())
                .status(ApplicationStatus.CONFIRMED)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(excelParser.parseAttendanceFile(mockFile)).thenReturn(codes);
        when(attendanceCodeRepository.findByCode("ABC123XYZ")).thenReturn(Optional.of(testAttendanceCode));
        when(attendanceCodeRepository.findByCode("DEF456UVW")).thenReturn(Optional.of(secondCode));
        when(applicationRepository.findByEventIdAndVolunteerId(1L, 2L))
                .thenReturn(Optional.of(testApplication));
        when(applicationRepository.findByEventIdAndVolunteerId(1L, 3L))
                .thenReturn(Optional.of(secondApp));
        doNothing().when(paymentService).releasePaymentToVolunteer(anyLong());

        // Act
        attendanceService.markAttendanceByExcel(1L, mockFile, organizer);

        // Assert
        assertTrue(testAttendanceCode.isMarked());
        assertTrue(secondCode.isMarked());
        verify(paymentService, times(2)).releasePaymentToVolunteer(anyLong());
    }

    @Test
    void testMarkAttendanceByExcel_WithInvalidCodes_ShouldThrowException() {
        // Arrange
        // Set event to today at 10:00 AM (started 2 hours ago, so within 24h window)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventStart = now.minusHours(2);
        testEvent.setDate(eventStart.toLocalDate());
        testEvent.setTime(eventStart.toLocalTime());
        
        MultipartFile mockFile = mock(MultipartFile.class);
        List<String> codes = List.of("ABC123XYZ", "INVALID");
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(excelParser.parseAttendanceFile(mockFile)).thenReturn(codes);
        when(attendanceCodeRepository.findByCode("ABC123XYZ")).thenReturn(Optional.of(testAttendanceCode));
        when(attendanceCodeRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.markAttendanceByExcel(1L, mockFile, organizer);
        });
    }

    @Test
    void testDownloadAttendanceTemplate_ShouldGenerateExcelFile() {
        // Arrange
        List<AttendanceCode> attendanceCodes = List.of(testAttendanceCode);
        byte[] mockExcelData = "Mock Excel Data".getBytes();
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(attendanceCodeRepository.findByEventId(1L)).thenReturn(attendanceCodes);
        when(excelParser.generateAttendanceTemplate(attendanceCodes)).thenReturn(mockExcelData);

        // Act
        byte[] result = attendanceService.downloadAttendanceTemplate(1L, organizer);

        // Assert
        assertNotNull(result);
        assertEquals(mockExcelData, result);
        verify(excelParser).generateAttendanceTemplate(attendanceCodes);
    }

    @Test
    void testDownloadAttendanceTemplate_NoCodesGenerated_ShouldThrowException() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(attendanceCodeRepository.findByEventId(1L)).thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.downloadAttendanceTemplate(1L, organizer);
        });
    }

    @Test
    void testGetEventAttendance_ShouldReturnAttendanceList() {
        // Arrange
        List<AttendanceCode> attendanceCodes = List.of(testAttendanceCode);
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(attendanceCodeRepository.findByEventId(1L)).thenReturn(attendanceCodes);

        // Act
        List<AttendanceCode> result = attendanceService.getEventAttendance(1L, organizer);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAttendanceCode, result.get(0));
    }

    @Test
    void testGetEventAttendance_NotOrganizer_ShouldThrowException() {
        // Arrange
        User otherUser = User.builder().id(3L).role(Role.ORGANIZER).build();
        testEvent.setOrganizer(otherUser);
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.getEventAttendance(1L, organizer);
        });
    }

    // ===== ADDITIONAL TESTS FOR TASK 12.6 =====

    @Test
    void testGenerateAttendanceCodes_GeneratesUniqueCodesForEachVolunteer() {
        // Arrange
        User volunteer2 = User.builder().id(3L).email("volunteer2@example.com").role(Role.VOLUNTEER).build();
        User volunteer3 = User.builder().id(4L).email("volunteer3@example.com").role(Role.VOLUNTEER).build();
        
        Application app2 = Application.builder()
                .id(2L)
                .event(testEvent)
                .volunteer(volunteer2)
                .status(ApplicationStatus.CONFIRMED)
                .build();
        
        Application app3 = Application.builder()
                .id(3L)
                .event(testEvent)
                .volunteer(volunteer3)
                .status(ApplicationStatus.CONFIRMED)
                .build();
        
        List<Application> confirmedApplications = List.of(testApplication, app2, app3);
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(attendanceCodeRepository.countByEventId(1L)).thenReturn(0L);
        when(applicationRepository.findByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED))
                .thenReturn(confirmedApplications);
        when(attendanceCodeRepository.existsByCode(anyString())).thenReturn(false);
        when(attendanceCodeRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        attendanceService.generateAttendanceCodes(1L);

        // Assert
        verify(attendanceCodeRepository).saveAll(argThat(codes -> {
            List<AttendanceCode> codeList = (List<AttendanceCode>) codes;
            return codeList.size() == 3 && 
                   codeList.stream().map(AttendanceCode::getCode).distinct().count() == 3;
        }));
    }

    @Test
    void testGenerateAttendanceCodes_CodesAreAlphanumeric() {
        // Arrange
        List<Application> confirmedApplications = List.of(testApplication);
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(attendanceCodeRepository.countByEventId(1L)).thenReturn(0L);
        when(applicationRepository.findByEventIdAndStatus(1L, ApplicationStatus.CONFIRMED))
                .thenReturn(confirmedApplications);
        when(attendanceCodeRepository.existsByCode(anyString())).thenReturn(false);
        when(attendanceCodeRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        attendanceService.generateAttendanceCodes(1L);

        // Assert
        verify(attendanceCodeRepository).saveAll(argThat(codes -> {
            List<AttendanceCode> codeList = (List<AttendanceCode>) codes;
            return codeList.stream().allMatch(code -> code.getCode().matches("[A-Z0-9]{8}"));
        }));
    }

    @Test
    void testMarkAttendanceByCode_ValidatesCodeFormat() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventStart = now.minusHours(2);
        testEvent.setDate(eventStart.toLocalDate());
        testEvent.setTime(eventStart.toLocalTime());
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(attendanceCodeRepository.findByCode("INVALID_CODE")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.markAttendanceByCode(1L, "INVALID_CODE", organizer);
        });
    }

    @Test
    void testMarkAttendanceByExcel_ParsesValidExcelFile() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventStart = now.minusHours(2);
        testEvent.setDate(eventStart.toLocalDate());
        testEvent.setTime(eventStart.toLocalTime());
        
        MultipartFile mockFile = mock(MultipartFile.class);
        List<String> codes = List.of("ABC123XYZ");
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(excelParser.parseAttendanceFile(mockFile)).thenReturn(codes);
        when(attendanceCodeRepository.findByCode("ABC123XYZ")).thenReturn(Optional.of(testAttendanceCode));
        when(applicationRepository.findByEventIdAndVolunteerId(1L, 2L))
                .thenReturn(Optional.of(testApplication));
        doNothing().when(paymentService).releasePaymentToVolunteer(anyLong());

        // Act
        attendanceService.markAttendanceByExcel(1L, mockFile, organizer);

        // Assert
        verify(excelParser).parseAttendanceFile(mockFile);
        assertTrue(testAttendanceCode.isMarked());
    }

    @Test
    void testMarkAttendanceByExcel_HandlesPartialFailures() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventStart = now.minusHours(2);
        testEvent.setDate(eventStart.toLocalDate());
        testEvent.setTime(eventStart.toLocalTime());
        
        MultipartFile mockFile = mock(MultipartFile.class);
        List<String> codes = List.of("ABC123XYZ", "INVALID", "DEF456UVW");
        
        AttendanceCode thirdCode = AttendanceCode.builder()
                .id(3L)
                .event(testEvent)
                .volunteer(User.builder().id(5L).build())
                .code("DEF456UVW")
                .build();
        
        Application thirdApp = Application.builder()
                .id(3L)
                .event(testEvent)
                .volunteer(User.builder().id(5L).build())
                .status(ApplicationStatus.CONFIRMED)
                .build();
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(excelParser.parseAttendanceFile(mockFile)).thenReturn(codes);
        when(attendanceCodeRepository.findByCode("ABC123XYZ")).thenReturn(Optional.of(testAttendanceCode));
        when(attendanceCodeRepository.findByCode("INVALID")).thenReturn(Optional.empty());
        when(attendanceCodeRepository.findByCode("DEF456UVW")).thenReturn(Optional.of(thirdCode));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.markAttendanceByExcel(1L, mockFile, organizer);
        });
    }

    @Test
    void testMarkAttendanceByExcel_SkipsAlreadyMarkedCodes() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventStart = now.minusHours(2);
        testEvent.setDate(eventStart.toLocalDate());
        testEvent.setTime(eventStart.toLocalTime());
        
        MultipartFile mockFile = mock(MultipartFile.class);
        List<String> codes = List.of("ABC123XYZ");
        
        testAttendanceCode.markAttendance(organizer); // Already marked
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(excelParser.parseAttendanceFile(mockFile)).thenReturn(codes);
        when(attendanceCodeRepository.findByCode("ABC123XYZ")).thenReturn(Optional.of(testAttendanceCode));

        // Act
        attendanceService.markAttendanceByExcel(1L, mockFile, organizer);

        // Assert - Should not throw exception, just skip already marked code
        verify(paymentService, never()).releasePaymentToVolunteer(anyLong());
    }

    @Test
    void testMarkAttendanceByExcel_WithMultipleValidCodes() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventStart = now.minusHours(2);
        testEvent.setDate(eventStart.toLocalDate());
        testEvent.setTime(eventStart.toLocalTime());
        
        MultipartFile mockFile = mock(MultipartFile.class);
        List<String> codes = List.of("ABC123XYZ", "DEF456UVW", "GHI789RST");
        
        AttendanceCode code2 = AttendanceCode.builder()
                .id(2L)
                .event(testEvent)
                .volunteer(User.builder().id(3L).build())
                .code("DEF456UVW")
                .build();
        
        AttendanceCode code3 = AttendanceCode.builder()
                .id(3L)
                .event(testEvent)
                .volunteer(User.builder().id(4L).build())
                .code("GHI789RST")
                .build();
        
        Application app2 = Application.builder()
                .id(2L)
                .event(testEvent)
                .volunteer(User.builder().id(3L).build())
                .status(ApplicationStatus.CONFIRMED)
                .build();
        
        Application app3 = Application.builder()
                .id(3L)
                .event(testEvent)
                .volunteer(User.builder().id(4L).build())
                .status(ApplicationStatus.CONFIRMED)
                .build();
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(excelParser.parseAttendanceFile(mockFile)).thenReturn(codes);
        when(attendanceCodeRepository.findByCode("ABC123XYZ")).thenReturn(Optional.of(testAttendanceCode));
        when(attendanceCodeRepository.findByCode("DEF456UVW")).thenReturn(Optional.of(code2));
        when(attendanceCodeRepository.findByCode("GHI789RST")).thenReturn(Optional.of(code3));
        when(applicationRepository.findByEventIdAndVolunteerId(1L, 2L))
                .thenReturn(Optional.of(testApplication));
        when(applicationRepository.findByEventIdAndVolunteerId(1L, 3L))
                .thenReturn(Optional.of(app2));
        when(applicationRepository.findByEventIdAndVolunteerId(1L, 4L))
                .thenReturn(Optional.of(app3));
        doNothing().when(paymentService).releasePaymentToVolunteer(anyLong());

        // Act
        attendanceService.markAttendanceByExcel(1L, mockFile, organizer);

        // Assert
        verify(paymentService, times(3)).releasePaymentToVolunteer(anyLong());
        assertTrue(testAttendanceCode.isMarked());
        assertTrue(code2.isMarked());
        assertTrue(code3.isMarked());
    }

    @Test
    void testDownloadAttendanceTemplate_WithMultipleCodes() {
        // Arrange
        AttendanceCode code2 = AttendanceCode.builder()
                .id(2L)
                .event(testEvent)
                .volunteer(User.builder().id(3L).build())
                .code("DEF456UVW")
                .build();
        
        List<AttendanceCode> attendanceCodes = List.of(testAttendanceCode, code2);
        byte[] mockExcelData = "Mock Excel Data with 2 codes".getBytes();
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(attendanceCodeRepository.findByEventId(1L)).thenReturn(attendanceCodes);
        when(excelParser.generateAttendanceTemplate(attendanceCodes)).thenReturn(mockExcelData);

        // Act
        byte[] result = attendanceService.downloadAttendanceTemplate(1L, organizer);

        // Assert
        assertNotNull(result);
        assertEquals(mockExcelData, result);
        verify(excelParser).generateAttendanceTemplate(argThat(codes -> codes.size() == 2));
    }
}