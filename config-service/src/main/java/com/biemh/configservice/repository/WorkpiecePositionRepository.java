package com.biemh.configservice.repository;

import com.biemh.configservice.domain.WorkpiecePosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for WorkpiecePosition entities.
 */
@Repository
public interface WorkpiecePositionRepository extends JpaRepository<WorkpiecePosition, Long> {

    /**
     * Find the most recently created workpiece position.
     */
    @Query("SELECT w FROM WorkpiecePosition w ORDER BY w.createdAt DESC")
    Optional<WorkpiecePosition> findLatest();
}
