package com.example.vently.event;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    List<Event> findByOrganizerId(Long organizerId);
    
    /**
     * Count active events for an organizer (for tier limit checks)
     */
    long countByOrganizerIdAndStatusIn(Long organizerId, EventStatus... statuses);
    
    /**
     * Find events by status
     */
    List<Event> findByStatus(EventStatus status);
    
    /**
     * Count events by status (for analytics)
     */
    Long countByStatus(EventStatus status);
    
    /**
     * Find events that need no-show processing
     * Events that ended 24 hours ago (assuming 8-hour event duration)
     * 
     * @param cutoffTime Events that ended before this time (32 hours ago)
     * @param startTime Events that ended after this time (33 hours ago)
     * @param status Event status (DEPOSIT_PAID)
     * @return List of events to process
     */
    @Query("SELECT e FROM Event e WHERE " +
           "e.date >= CAST(:startTime AS date) AND e.date <= CAST(:cutoffTime AS date) " +
           "AND e.status = :status")
    List<Event> findEventsForNoShowProcessing(
        @Param("cutoffTime") LocalDateTime cutoffTime,
        @Param("startTime") LocalDateTime startTime,
        @Param("status") EventStatus status
    );
}
