package com.codementor.repository;

import com.codementor.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    // All submissions for a user, newest first
    List<Submission> findByUserIdOrderBySubmittedAtDesc(Long userId);

    // Single submission — verify it belongs to requesting user (security check)
    Optional<Submission> findByIdAndUserId(Long id, Long userId);

    // Count total submissions for a user (used in dashboard)
    long countByUserId(Long userId);

    // Search by title keyword for a specific user
    @Query("SELECT s FROM Submission s WHERE s.user.id = :userId " +
            "AND LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY s.submittedAt DESC")
    List<Submission> searchByTitleAndUserId(
            @Param("keyword") String keyword,
            @Param("userId") Long userId
    );

    // Recent N submissions for dashboard
    @Query("SELECT s FROM Submission s WHERE s.user.id = :userId " +
            "ORDER BY s.submittedAt DESC LIMIT 5")
    List<Submission> findTop5ByUserIdOrderBySubmittedAtDesc(@Param("userId") Long userId);
}