package com.biemh.configservice.controller;

import com.biemh.configservice.domain.Program;
import com.biemh.configservice.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing robot programs.
 * 
 * TODO: Implement API key authentication via X-API-KEY header
 * for production deployment.
 */
@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
@Slf4j
public class ProgramController {

    private final ProgramRepository programRepository;

    /**
     * Get all programs.
     * 
     * @return List of all programs
     */
    @GetMapping
    public ResponseEntity<List<Program>> getAllPrograms() {
        log.info("Fetching all programs");
        List<Program> programs = programRepository.findAll();
        return ResponseEntity.ok(programs);
    }

    /**
     * Get a program by program number.
     * 
     * @param programNumber The program number
     * @return The program if found
     */
    @GetMapping("/{programNumber}")
    public ResponseEntity<Program> getProgramByNumber(@PathVariable Integer programNumber) {
        log.info("Fetching program with number: {}", programNumber);
        return programRepository.findByProgramNumber(programNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new program.
     * 
     * @param program The program to create
     * @return The created program
     */
    @PostMapping
    public ResponseEntity<Program> createProgram(@RequestBody Program program) {
        log.info("Creating new program: {}", program.getProgramName());
        
        // Check if program number already exists
        if (programRepository.findByProgramNumber(program.getProgramNumber()).isPresent()) {
            log.warn("Program with number {} already exists", program.getProgramNumber());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        Program savedProgram = programRepository.save(program);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProgram);
    }

    /**
     * Update an existing program.
     * 
     * @param programNumber The program number to update
     * @param programUpdate The updated program data
     * @return The updated program
     */
    @PutMapping("/{programNumber}")
    public ResponseEntity<Program> updateProgram(
            @PathVariable Integer programNumber,
            @RequestBody Program programUpdate) {
        log.info("Updating program with number: {}", programNumber);
        
        return programRepository.findByProgramNumber(programNumber)
                .map(existingProgram -> {
                    existingProgram.setProgramName(programUpdate.getProgramName());
                    existingProgram.setProgramType(programUpdate.getProgramType());
                    existingProgram.setDescription(programUpdate.getDescription());
                    existingProgram.setEnabled(programUpdate.getEnabled());
                    
                    Program updated = programRepository.save(existingProgram);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a program by program number.
     * 
     * @param programNumber The program number to delete
     * @return No content if successful
     */
    @DeleteMapping("/{programNumber}")
    public ResponseEntity<Void> deleteProgram(@PathVariable Integer programNumber) {
        log.info("Deleting program with number: {}", programNumber);
        
        return programRepository.findByProgramNumber(programNumber)
                .map(program -> {
                    programRepository.delete(program);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
