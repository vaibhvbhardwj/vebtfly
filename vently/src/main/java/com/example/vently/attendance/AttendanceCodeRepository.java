package com.example.vently.attendance;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceCodeRepository extends JpaRepository<AttendanceCode, Long> {

    // Find attendance code by code string
    Optional<AttendanceCode> findByCode(String code);

    // Check if code exists
    boolean existsByCode(String code);

    // Find all attendance codes for an event
    List<AttendanceCode> findByEventId(Long eventId);

    // Find all attendance codes for a volunteer
    List<AttendanceCode> findByVolunteerId(Long volunteerId);

    // Find unmarked attendance codes for an event
    @Query("SELECT ac FROM AttendanceCode ac WHERE ac.event.id = :eventId AND ac.markedAt IS NULL")
    List<AttendanceCode> findUnmarkedByEventId(@Param("eventId") Long eventId);

    // Find marked attendance codes for an event
    @Query("SELECT ac FROM AttendanceCode ac WHERE ac.event.id = :eventId AND ac.markedAt IS NOT NULL")
    List<AttendanceCode> findMarkedByEventId(@Param("eventId") Long eventId);

    // Check if a specific code is already marked
    @Query("SELECT CASE WHEN ac.markedAt IS NOT NULL THEN true ELSE false END " +
           "FROM AttendanceCode ac WHERE ac.code = :code")
    Optional<Boolean> isCodeMarked(@Param("code") String code);

    // Find codes marked by a specific organizer
    List<AttendanceCode> findByMarkedById(Long organizerId);

    // Count total codes for an event
    long countByEventId(Long eventId);

    // Count marked codes for an event
    @Query("SELECT COUNT(ac) FROM AttendanceCode ac WHERE ac.event.id = :eventId AND ac.markedAt IS NOT NULL")
    long countMarkedByEventId(@Param("eventId") Long eventId);

    // Count unmarked codes for an event
    @Query("SELECT COUNT(ac) FROM AttendanceCode ac WHERE ac.event.id = :eventId AND ac.markedAt IS NULL")
    long countUnmarkedByEventId(@Param("eventId") Long eventId);

    // Find attendance code by event and volunteer
    Optional<AttendanceCode> findByEventIdAndVolunteerId(Long eventId, Long volunteerId);

    // Check if volunteer has an attendance code for an event
    boolean existsByEventIdAndVolunteerId(Long eventId, Long volunteerId);
}
