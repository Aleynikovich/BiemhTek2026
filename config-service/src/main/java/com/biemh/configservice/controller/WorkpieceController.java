package com.biemh.configservice.controller;

import com.biemh.configservice.domain.WorkpiecePosition;
import com.biemh.configservice.repository.WorkpiecePositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing workpiece positions.
 * 
 * TODO: Implement API key authentication via X-API-KEY header
 * for production deployment.
 */
@RestController
@RequestMapping("/api/workpieces")
@RequiredArgsConstructor
@Slf4j
public class WorkpieceController {

    private final WorkpiecePositionRepository workpiecePositionRepository;

    /**
     * Create a new workpiece position.
     * 
     * @param workpiece The workpiece position to create
     * @return The created workpiece position
     */
    @PostMapping
    public ResponseEntity<WorkpiecePosition> createWorkpiecePosition(@RequestBody WorkpiecePosition workpiece) {
        log.info("Creating new workpiece position from source: {}", workpiece.getSourceProgram());
        WorkpiecePosition saved = workpiecePositionRepository.save(workpiece);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Get the latest workpiece position.
     * 
     * @return The most recently created workpiece position
     */
    @GetMapping("/latest")
    public ResponseEntity<WorkpiecePosition> getLatestWorkpiecePosition() {
        log.info("Fetching latest workpiece position");
        return workpiecePositionRepository.findLatest()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all workpiece positions with optional limit.
     * 
     * @param limit Maximum number of results (default 100)
     * @return List of workpiece positions
     */
    @GetMapping
    public ResponseEntity<List<WorkpiecePosition>> getAllWorkpiecePositions(
            @RequestParam(defaultValue = "100") int limit) {
        log.info("Fetching workpiece positions with limit: {}", limit);
        
        List<WorkpiecePosition> workpieces = workpiecePositionRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();
        
        return ResponseEntity.ok(workpieces);
    }

    /**
     * Get a specific workpiece position by ID.
     * 
     * @param id The workpiece position ID
     * @return The workpiece position if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<WorkpiecePosition> getWorkpiecePosition(@PathVariable Long id) {
        log.info("Fetching workpiece position with ID: {}", id);
        return workpiecePositionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
