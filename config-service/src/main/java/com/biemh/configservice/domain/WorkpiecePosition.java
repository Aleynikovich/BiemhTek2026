package com.biemh.configservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a workpiece position from the vision system.
 */
@Entity
@Table(name = "workpiece_positions")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "source_program", nullable = false)
    private String sourceProgram;

    @Column(name = "metadata")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
