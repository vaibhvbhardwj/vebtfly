package com.example.vently.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // Check if volunteer has already applied to an event
    boolean existsByEventIdAndVolunteerId(Long eventId, Long volunteerId);

    // Check if volunteer has already applied to an event with specific status
    boolean existsByEventIdAndVolunteerIdAndStatus(Long eventId, Long volunteerId, ApplicationStatus status);

    // Find application by event and volunteer
    Optional<Application> findByEventIdAndVolunteerId(Long eventId, Long volunteerId);

    // Get all applications for an event
    List<Application> findByEventId(Long eventId);

    // Get all applications for an event with specific status
    List<Application> findByEventIdAndStatus(Long eventId, ApplicationStatus status);

    // Get all applications for a volunteer
    Page<Application> findByVolunteerId(Long volunteerId, Pageable pageable);

    // Get all applications for a volunteer with specific status
    Page<Application> findByVolunteerIdAndStatus(Long volunteerId, ApplicationStatus status, Pageable pageable);

    // Count applications by event and status
    long countByEventIdAndStatus(Long eventId, ApplicationStatus status);

    // Count all applications for a volunteer
    long countByVolunteerId(Long volunteerId);

    // Count applications for a volunteer with specific status
    long countByVolunteerIdAndStatus(Long volunteerId, ApplicationStatus status);

    // Count confirmed applications for an event
    @Query("SELECT COUNT(a) FROM Application a WHERE a.event.id = :eventId AND a.status = com.example.vently.application.ApplicationStatus.CONFIRMED")
    long countConfirmedByEventId(@Param("eventId") Long eventId);

    // Count active applications for a volunteer (PENDING, ACCEPTED, CONFIRMED)
    @Query("SELECT COUNT(a) FROM Application a WHERE a.volunteer.id = :volunteerId " +
           "AND a.status IN (com.example.vently.application.ApplicationStatus.PENDING, com.example.vently.application.ApplicationStatus.ACCEPTED, com.example.vently.application.ApplicationStatus.CONFIRMED)")
    long countActiveApplicationsByVolunteerId(@Param("volunteerId") Long volunteerId);
    
    // Count applications by volunteer and status (for tier limit checks)
    long countByVolunteerIdAndStatusIn(Long volunteerId, ApplicationStatus... statuses);

    // Find expired confirmations (ACCEPTED status past deadline)
    @Query("SELECT a FROM Application a WHERE a.status = com.example.vently.application.ApplicationStatus.ACCEPTED " +
           "AND a.confirmationDeadline < :now")
    List<Application> findExpiredConfirmations(@Param("now") LocalDateTime now);

    // Find all PENDING applications for an event
    @Query("SELECT a FROM Application a WHERE a.event.id = :eventId AND a.status = com.example.vently.application.ApplicationStatus.PENDING " +
           "ORDER BY a.appliedAt ASC")
    List<Application> findPendingApplicationsByEventId(@Param("eventId") Long eventId);

    // Get applications for events organized by a specific user
    @Query("SELECT a FROM Application a WHERE a.event.organizer.id = :organizerId")
    Page<Application> findByEventOrganizerId(@Param("organizerId") Long organizerId, Pageable pageable);

    // Find all confirmed applications for an event (for attendance marking)
    @Query("SELECT a FROM Application a WHERE a.event.id = :eventId AND a.status = com.example.vently.application.ApplicationStatus.CONFIRMED")
    List<Application> findConfirmedApplicationsByEventId(@Param("eventId") Long eventId);
}
