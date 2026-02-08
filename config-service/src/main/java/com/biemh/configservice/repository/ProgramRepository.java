package com.biemh.configservice.repository;

import com.biemh.configservice.domain.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Program entities.
 */
@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {

    /**
     * Find a program by its program number.
     */
    Optional<Program> findByProgramNumber(Integer programNumber);

    /**
     * Find all enabled programs.
     */
    List<Program> findByEnabledTrue();

    /**
     * Find programs by type.
     */
    List<Program> findByProgramType(Program.ProgramType programType);
}
