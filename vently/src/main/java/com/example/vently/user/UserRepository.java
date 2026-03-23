package com.example.vently.user;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    
    // Spring Data JPA will automatically generate the SQL for this
    Optional<User> findByEmail(String email);
    
    Optional<User> findByVerificationToken(String verificationToken);
    
    // Analytics queries
    Long countByRole(Role role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.createdAt BETWEEN :startDate AND :endDate")
    Long countByRoleAndCreatedAtBetween(@Param("role") Role role, 
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.createdAt < :endDate")
    Long countByRoleAndCreatedAtBefore(@Param("role") Role role,
                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.noShowCount > 0")
    Long countVolunteersWithNoShows(@Param("role") Role role);
    
    @Query("SELECT SUM(u.noShowCount) FROM User u WHERE u.role = :role")
    Long sumNoShowCounts(@Param("role") Role role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.suspendedUntil IS NOT NULL AND u.suspendedUntil > CURRENT_TIMESTAMP")
    Long countSuspendedDueToNoShows(@Param("role") Role role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.accountStatus = com.example.vently.user.AccountStatus.BANNED")
    Long countBannedDueToNoShows(@Param("role") Role role);

    /**
     * Check if a phone number is already verified on a different account
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.phone = :phone AND u.phoneVerified = true AND u.id <> :excludeUserId")
    Long countVerifiedPhoneOnOtherAccounts(@Param("phone") String phone, @Param("excludeUserId") Long excludeUserId);
}
