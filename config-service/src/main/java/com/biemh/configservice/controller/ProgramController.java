package com.biemh.configservice.controller;

import com.biemh.configservice.domain.Program;
import com.biemh.configservice.repository.ProgramRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing robot programs.
 * Provides CRUD operations for program configurations.
 * 
 * TODO: Implement API key authentication using X-API-KEY header
 */
@RestController
@RequestMapping("/api/programs")
public class ProgramController {

    @Autowired
    private ProgramRepository programRepository;

    /**
     * Get all programs.
     * GET /api/programs
     * 
     * @param apiKey API key for authentication (TODO: enforce)
     * @return List of all programs
     */
    @GetMapping
    public ResponseEntity<List<Program>> getAllPrograms(
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        // TODO: Validate API key
        List<Program> programs = programRepository.findAll();
        return ResponseEntity.ok(programs);
    }

    /**
     * Get a specific program by program number.
     * GET /api/programs/{program_number}
     * 
     * @param programNumber the program number
     * @param apiKey API key for authentication (TODO: enforce)
     * @return Program if found, 404 otherwise
     */
    @GetMapping("/{program_number}")
    public ResponseEntity<Program> getProgramByNumber(
            @PathVariable("program_number") Integer programNumber,
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        // TODO: Validate API key
        Optional<Program> program = programRepository.findByProgramNumber(programNumber);
        return program.map(ResponseEntity::ok)
                     .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create a new program.
     * POST /api/programs
     * 
     * @param program the program to create
     * @param apiKey API key for authentication (TODO: enforce)
     * @return Created program with 201 status
     */
    @PostMapping
    public ResponseEntity<Program> createProgram(
            @RequestBody Program program,
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        // TODO: Validate API key
        
        // Check if program number already exists
        if (program.getProgramNumber() != null && 
            programRepository.existsByProgramNumber(program.getProgramNumber())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        Program savedProgram = programRepository.save(program);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProgram);
    }

    /**
     * Update an existing program.
     * PUT /api/programs/{program_number}
     * 
     * @param programNumber the program number to update
     * @param programUpdate the updated program data
     * @param apiKey API key for authentication (TODO: enforce)
     * @return Updated program or 404 if not found
     */
    @PutMapping("/{program_number}")
    public ResponseEntity<Program> updateProgram(
            @PathVariable("program_number") Integer programNumber,
            @RequestBody Program programUpdate,
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        // TODO: Validate API key
        
        Optional<Program> existingProgram = programRepository.findByProgramNumber(programNumber);
        if (!existingProgram.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Program program = existingProgram.get();
        
        // Update fields
        if (programUpdate.getProgramName() != null) {
            program.setProgramName(programUpdate.getProgramName());
        }
        if (programUpdate.getProgramType() != null) {
            program.setProgramType(programUpdate.getProgramType());
        }
        if (programUpdate.getDescription() != null) {
            program.setDescription(programUpdate.getDescription());
        }
        if (programUpdate.getEnabled() != null) {
            program.setEnabled(programUpdate.getEnabled());
        }
        
        Program savedProgram = programRepository.save(program);
        return ResponseEntity.ok(savedProgram);
    }

    /**
     * Delete a program.
     * DELETE /api/programs/{program_number}
     * 
     * @param programNumber the program number to delete
     * @param apiKey API key for authentication (TODO: enforce)
     * @return 204 if deleted, 404 if not found
     */
    @DeleteMapping("/{program_number}")
    public ResponseEntity<Void> deleteProgram(
            @PathVariable("program_number") Integer programNumber,
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        // TODO: Validate API key
        
        Optional<Program> program = programRepository.findByProgramNumber(programNumber);
        if (!program.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        programRepository.delete(program.get());
        return ResponseEntity.noContent().build();
    }
}
