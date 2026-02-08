package com.biemh.configservice.repository;

import com.biemh.configservice.domain.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Program entity.
 * Provides CRUD operations and custom queries for programs.
 */
@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {

    /**
     * Find a program by its program number.
     * @param programNumber the unique program number
     * @return Optional containing the program if found
     */
    Optional<Program> findByProgramNumber(Integer programNumber);

    /**
     * Check if a program with the given number exists.
     * @param programNumber the program number to check
     * @return true if exists, false otherwise
     */
    boolean existsByProgramNumber(Integer programNumber);
}
