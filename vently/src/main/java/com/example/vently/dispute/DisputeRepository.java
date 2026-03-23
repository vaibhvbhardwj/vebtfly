package com.example.vently.dispute;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    /**
     * Find all disputes raised by a specific user
     */
    List<Dispute> findByRaisedById(Long userId);

    /**
     * Find all disputes against a specific user
     */
    List<Dispute> findByAgainstUserId(Long userId);

    /**
     * Find all disputes related to a specific event
     */
    List<Dispute> findByEventId(Long eventId);

    /**
     * Find all disputes with a specific status
     */
    Page<Dispute> findByStatus(DisputeStatus status, Pageable pageable);

    /**
     * Find all open disputes (OPEN or UNDER_REVIEW) for admin dashboard
     */
    @Query("SELECT d FROM Dispute d WHERE d.status IN (com.example.vently.dispute.DisputeStatus.OPEN, com.example.vently.dispute.DisputeStatus.UNDER_REVIEW) ORDER BY d.createdAt ASC")
    Page<Dispute> findOpenDisputes(Pageable pageable);

    /**
     * Find all disputes by a user (either raised by or against)
     */
    @Query("SELECT d FROM Dispute d WHERE d.raisedBy.id = :userId OR d.againstUser.id = :userId ORDER BY d.createdAt DESC")
    List<Dispute> findByUserId(@Param("userId") Long userId);

    /**
     * Count open disputes for analytics
     */
    @Query("SELECT COUNT(d) FROM Dispute d WHERE d.status IN (com.example.vently.dispute.DisputeStatus.OPEN, com.example.vently.dispute.DisputeStatus.UNDER_REVIEW)")
    Long countOpenDisputes();

    /**
     * Count disputes by status
     */
    Long countByStatus(DisputeStatus status);

    /**
     * Find disputes resolved within a date range for analytics
     */
    @Query("SELECT d FROM Dispute d WHERE d.status = com.example.vently.dispute.DisputeStatus.RESOLVED AND d.resolvedAt BETWEEN :startDate AND :endDate")
    List<Dispute> findResolvedDisputesBetween(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Calculate average resolution time for resolved disputes within a date range
     * Returns list of disputes so we can calculate duration in Java
     */
    @Query("SELECT d FROM Dispute d WHERE d.status = com.example.vently.dispute.DisputeStatus.RESOLVED AND d.resolvedAt BETWEEN :startDate AND :endDate")
    List<Dispute> getDisputesForResolutionTimeCalculation(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find disputes by event and status
     */
    List<Dispute> findByEventIdAndStatus(Long eventId, DisputeStatus status);

    /**
     * Find disputes raised by user with specific status
     */
    List<Dispute> findByRaisedByIdAndStatus(Long userId, DisputeStatus status);

    /**
     * Check if a user has any open disputes for a specific event
     */
    @Query("SELECT COUNT(d) > 0 FROM Dispute d WHERE d.event.id = :eventId AND d.raisedBy.id = :userId AND d.status IN (com.example.vently.dispute.DisputeStatus.OPEN, com.example.vently.dispute.DisputeStatus.UNDER_REVIEW)")
    boolean hasOpenDisputeForEvent(@Param("eventId") Long eventId, @Param("userId") Long userId);
}
