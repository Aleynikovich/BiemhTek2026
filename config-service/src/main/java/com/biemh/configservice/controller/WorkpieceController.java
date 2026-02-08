package com.biemh.configservice.controller;

import com.biemh.configservice.domain.WorkpiecePosition;
import com.biemh.configservice.repository.WorkpiecePositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing workpiece positions.
 * Handles vision system results and retrieval of detected workpiece positions.
 * 
 * TODO: Implement API key authentication using X-API-KEY header
 */
@RestController
@RequestMapping("/api/workpieces")
public class WorkpieceController {

    @Autowired
    private WorkpiecePositionRepository workpiecePositionRepository;

    /**
     * Create a new workpiece position (typically from vision system).
     * POST /api/workpieces
     * 
     * @param workpiece the workpiece position to create
     * @param apiKey API key for authentication (TODO: enforce)
     * @return Created workpiece with 201 status
     */
    @PostMapping
    public ResponseEntity<WorkpiecePosition> createWorkpiecePosition(
            @RequestBody WorkpiecePosition workpiece,
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        // TODO: Validate API key
        
        WorkpiecePosition savedWorkpiece = workpiecePositionRepository.save(workpiece);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedWorkpiece);
    }

    /**
     * Get the latest workpiece position.
     * GET /api/workpieces/latest
     * 
     * @param apiKey API key for authentication (TODO: enforce)
     * @return Latest workpiece position or 404 if none exist
     */
    @GetMapping("/latest")
    public ResponseEntity<WorkpiecePosition> getLatestWorkpiecePosition(
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        // TODO: Validate API key
        
        List<WorkpiecePosition> workpieces = workpiecePositionRepository
                .findTopByOrderByCreatedAtDesc(PageRequest.of(0, 1));
        
        if (workpieces.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(workpieces.get(0));
    }

    /**
     * Get all workpiece positions.
     * GET /api/workpieces
     * 
     * @param apiKey API key for authentication (TODO: enforce)
     * @return List of all workpiece positions
     */
    @GetMapping
    public ResponseEntity<List<WorkpiecePosition>> getAllWorkpiecePositions(
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        // TODO: Validate API key
        
        List<WorkpiecePosition> workpieces = workpiecePositionRepository.findAll();
        return ResponseEntity.ok(workpieces);
    }

    /**
     * Get a specific workpiece position by ID.
     * GET /api/workpieces/{id}
     * 
     * @param id the workpiece ID
     * @param apiKey API key for authentication (TODO: enforce)
     * @return Workpiece if found, 404 otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<WorkpiecePosition> getWorkpiecePositionById(
            @PathVariable Long id,
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        // TODO: Validate API key
        
        return workpiecePositionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
