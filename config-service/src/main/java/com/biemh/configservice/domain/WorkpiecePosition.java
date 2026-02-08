package com.biemh.configservice.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Domain entity representing a workpiece position detected by the vision system.
 * Stores position, orientation, and metadata about detected workpieces.
 */
@Entity
@Table(name = "workpiece_positions")
public class WorkpiecePosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "x", nullable = false)
    private Double x;

    @Column(name = "y", nullable = false)
    private Double y;

    @Column(name = "z", nullable = false)
    private Double z;

    @Column(name = "rx", nullable = false)
    private Double rx;

    @Column(name = "ry", nullable = false)
    private Double ry;

    @Column(name = "rz", nullable = false)
    private Double rz;

    @Column(name = "score")
    private Double score;

    @Column(name = "source_program")
    private String sourceProgram;

    @Column(name = "metadata")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors
    public WorkpiecePosition() {
    }

    public WorkpiecePosition(Double x, Double y, Double z, Double rx, Double ry, Double rz, 
                            Double score, String sourceProgram) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
        this.score = score;
        this.sourceProgram = sourceProgram;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Double getZ() {
        return z;
    }

    public void setZ(Double z) {
        this.z = z;
    }

    public Double getRx() {
        return rx;
    }

    public void setRx(Double rx) {
        this.rx = rx;
    }

    public Double getRy() {
        return ry;
    }

    public void setRy(Double ry) {
        this.ry = ry;
    }

    public Double getRz() {
        return rz;
    }

    public void setRz(Double rz) {
        this.rz = rz;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getSourceProgram() {
        return sourceProgram;
    }

    public void setSourceProgram(String sourceProgram) {
        this.sourceProgram = sourceProgram;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
