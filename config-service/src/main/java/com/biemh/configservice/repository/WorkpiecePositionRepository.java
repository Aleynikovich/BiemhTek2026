package com.biemh.configservice.repository;

import com.biemh.configservice.domain.WorkpiecePosition;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for WorkpiecePosition entity.
 * Provides CRUD operations and custom queries for workpiece positions.
 */
@Repository
public interface WorkpiecePositionRepository extends JpaRepository<WorkpiecePosition, Long> {

    /**
     * Find the most recent workpiece position.
     * @param pageable page request with size 1 and sort by createdAt DESC
     * @return List containing the latest workpiece position
     */
    List<WorkpiecePosition> findTopByOrderByCreatedAtDesc(Pageable pageable);
}
