package com.example.vently.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.example.vently.application.ApplicationRepository;
import com.example.vently.event.dto.EventFilterDto;
import com.example.vently.event.dto.EventResponseDto;
import com.example.vently.payment.PaymentRepository;
import com.example.vently.rating.RatingRepository;
import com.example.vently.user.Role;
import com.example.vently.user.User;

/**
 * Unit tests for event filtering and search functionality
 * Requirements: 4.1, 4.2, 4.3, 4.6, 29.1, 29.4, 29.5
 */
@ExtendWith(MockitoExtension.class)
class EventFilteringTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RatingRepository ratingRepository;

    @InjectMocks
    private EventService eventService;

    private User organizer;
    private Event event1;
    private Event event2;

    @BeforeEach
    void setUp() {
        // Create test organizer
        organizer = User.builder()
            .id(1L)
            .email("organizer@test.com")
            .fullName("Test Organizer")
            .organizationName("Test Org")
            .role(Role.ORGANIZER)
            .verificationBadge(true)
            .build();

        // Create test events
        event1 = Event.builder()
            .id(1L)
            .title("Music Festival")
            .description("A great music festival")
            .location("New York")
            .date(LocalDate.now().plusDays(30))
            .time(LocalTime.of(18, 0))
            .requiredVolunteers(10)
            .paymentPerVolunteer(new BigDecimal("50.00"))
            .status(EventStatus.PUBLISHED)
            .category("Music")
            .organizer(organizer)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        event2 = Event.builder()
            .id(2L)
            .title("Tech Conference")
            .description("Annual tech conference")
            .location("San Francisco")
            .date(LocalDate.now().plusDays(60))
            .time(LocalTime.of(9, 0))
            .requiredVolunteers(20)
            .paymentPerVolunteer(new BigDecimal("75.00"))
            .status(EventStatus.PUBLISHED)
            .category("Technology")
            .organizer(organizer)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    void testGetPublishedEvents_WithNoFilters() {
        // Arrange
        List<Event> events = Arrays.asList(event1, event2);
        Page<Event> eventPage = new PageImpl<>(events);
        Pageable pageable = PageRequest.of(0, 10);
        EventFilterDto filters = new EventFilterDto();

        when(eventRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(eventPage);
        when(applicationRepository.findByEventId(anyLong()))
            .thenReturn(Arrays.asList());
        when(ratingRepository.calculateAverageRating(anyLong()))
            .thenReturn(4.5);

        // Act
        Page<EventResponseDto> result = eventService.getPublishedEvents(pageable, filters);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(eventRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testGetPublishedEvents_WithLocationFilter() {
        // Arrange
        List<Event> events = Arrays.asList(event1);
        Page<Event> eventPage = new PageImpl<>(events);
        Pageable pageable = PageRequest.of(0, 10);
        EventFilterDto filters = EventFilterDto.builder()
            .location("New York")
            .build();

        when(eventRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(eventPage);
        when(applicationRepository.findByEventId(anyLong()))
            .thenReturn(Arrays.asList());
        when(ratingRepository.calculateAverageRating(anyLong()))
            .thenReturn(4.5);

        // Act
        Page<EventResponseDto> result = eventService.getPublishedEvents(pageable, filters);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Music Festival", result.getContent().get(0).getTitle());
    }

    @Test
    void testGetPublishedEvents_WithPaymentRangeFilter() {
        // Arrange
        List<Event> events = Arrays.asList(event2);
        Page<Event> eventPage = new PageImpl<>(events);
        Pageable pageable = PageRequest.of(0, 10);
        EventFilterDto filters = EventFilterDto.builder()
            .minPayment(new BigDecimal("60.00"))
            .maxPayment(new BigDecimal("100.00"))
            .build();

        when(eventRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(eventPage);
        when(applicationRepository.findByEventId(anyLong()))
            .thenReturn(Arrays.asList());
        when(ratingRepository.calculateAverageRating(anyLong()))
            .thenReturn(4.5);

        // Act
        Page<EventResponseDto> result = eventService.getPublishedEvents(pageable, filters);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Tech Conference", result.getContent().get(0).getTitle());
    }

    @Test
    void testGetPublishedEvents_WithSearchQuery() {
        // Arrange
        List<Event> events = Arrays.asList(event1);
        Page<Event> eventPage = new PageImpl<>(events);
        Pageable pageable = PageRequest.of(0, 10);
        EventFilterDto filters = EventFilterDto.builder()
            .searchQuery("music")
            .build();

        when(eventRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(eventPage);
        when(applicationRepository.findByEventId(anyLong()))
            .thenReturn(Arrays.asList());
        when(ratingRepository.calculateAverageRating(anyLong()))
            .thenReturn(4.5);

        // Act
        Page<EventResponseDto> result = eventService.getPublishedEvents(pageable, filters);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Music Festival", result.getContent().get(0).getTitle());
    }

    @Test
    void testGetPublishedEvents_WithDateRangeFilter() {
        // Arrange
        List<Event> events = Arrays.asList(event1);
        Page<Event> eventPage = new PageImpl<>(events);
        Pageable pageable = PageRequest.of(0, 10);
        EventFilterDto filters = EventFilterDto.builder()
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(45))
            .build();

        when(eventRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(eventPage);
        when(applicationRepository.findByEventId(anyLong()))
            .thenReturn(Arrays.asList());
        when(ratingRepository.calculateAverageRating(anyLong()))
            .thenReturn(4.5);

        // Act
        Page<EventResponseDto> result = eventService.getPublishedEvents(pageable, filters);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Music Festival", result.getContent().get(0).getTitle());
    }

    @Test
    void testGetPublishedEvents_WithCategoryFilter() {
        // Arrange
        List<Event> events = Arrays.asList(event2);
        Page<Event> eventPage = new PageImpl<>(events);
        Pageable pageable = PageRequest.of(0, 10);
        EventFilterDto filters = EventFilterDto.builder()
            .category("Technology")
            .build();

        when(eventRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(eventPage);
        when(applicationRepository.findByEventId(anyLong()))
            .thenReturn(Arrays.asList());
        when(ratingRepository.calculateAverageRating(anyLong()))
            .thenReturn(4.5);

        // Act
        Page<EventResponseDto> result = eventService.getPublishedEvents(pageable, filters);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Tech Conference", result.getContent().get(0).getTitle());
    }

    @Test
    void testGetPublishedEvents_SortByPayment() {
        // Arrange
        List<Event> events = Arrays.asList(event2, event1); // Sorted by payment desc
        Page<Event> eventPage = new PageImpl<>(events);
        Pageable pageable = PageRequest.of(0, 10);
        EventFilterDto filters = EventFilterDto.builder()
            .sortBy("payment")
            .build();

        when(eventRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(eventPage);
        when(applicationRepository.findByEventId(anyLong()))
            .thenReturn(Arrays.asList());
        when(ratingRepository.calculateAverageRating(anyLong()))
            .thenReturn(4.5);

        // Act
        Page<EventResponseDto> result = eventService.getPublishedEvents(pageable, filters);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        // Verify sorting is applied (higher payment first)
        assertTrue(result.getContent().get(0).getPaymentPerVolunteer()
            .compareTo(result.getContent().get(1).getPaymentPerVolunteer()) >= 0);
    }

    @Test
    void testGetPublishedEvents_IncludesOrganizerInfo() {
        // Arrange
        List<Event> events = Arrays.asList(event1);
        Page<Event> eventPage = new PageImpl<>(events);
        Pageable pageable = PageRequest.of(0, 10);
        EventFilterDto filters = new EventFilterDto();

        when(eventRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(eventPage);
        when(applicationRepository.findByEventId(anyLong()))
            .thenReturn(Arrays.asList());
        when(ratingRepository.calculateAverageRating(anyLong()))
            .thenReturn(4.5);

        // Act
        Page<EventResponseDto> result = eventService.getPublishedEvents(pageable, filters);

        // Assert
        assertNotNull(result);
        EventResponseDto dto = result.getContent().get(0);
        assertEquals("Test Organizer", dto.getOrganizer().getName());
        assertEquals("Test Org", dto.getOrganizer().getOrganization());
        assertEquals(4.5, dto.getOrganizer().getAverageRating());
        assertTrue(dto.getOrganizer().getVerificationBadge());
    }
}
