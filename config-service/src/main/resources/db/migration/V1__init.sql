-- V1__init.sql
-- Initial schema for config-service database

-- Create programs table
CREATE TABLE programs (
    id BIGSERIAL PRIMARY KEY,
    program_number INTEGER NOT NULL UNIQUE,
    program_name VARCHAR(255) NOT NULL,
    program_type VARCHAR(50) NOT NULL,
    description TEXT,
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create index on program_number for faster lookups
CREATE INDEX idx_programs_program_number ON programs(program_number);

-- Create index on program_type for filtering
CREATE INDEX idx_programs_program_type ON programs(program_type);

-- Create workpiece_positions table
CREATE TABLE workpiece_positions (
    id BIGSERIAL PRIMARY KEY,
    x DOUBLE PRECISION NOT NULL,
    y DOUBLE PRECISION NOT NULL,
    z DOUBLE PRECISION NOT NULL,
    rx DOUBLE PRECISION NOT NULL,
    ry DOUBLE PRECISION NOT NULL,
    rz DOUBLE PRECISION NOT NULL,
    score DOUBLE PRECISION,
    source_program VARCHAR(255),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL
);

-- Create index on created_at for latest queries
CREATE INDEX idx_workpiece_positions_created_at ON workpiece_positions(created_at DESC);

-- Create index on source_program for filtering
CREATE INDEX idx_workpiece_positions_source_program ON workpiece_positions(source_program);

-- Insert sample program configurations
INSERT INTO programs (program_number, program_name, program_type, description, enabled, created_at, updated_at)
VALUES 
    (1, 'Get New Workpiece Position', 'VISION', 'Captures new workpiece position from vision system', true, NOW(), NOW()),
    (2, 'Calibration', 'VISION', 'Executes vision system calibration', true, NOW(), NOW()),
    (3, 'Test Calibration', 'VISION', 'Tests calibration accuracy', true, NOW(), NOW()),
    (4, 'Pick New Workpiece', 'ROBOT', 'Picks workpiece from detected position', true, NOW(), NOW()),
    (5, 'Place New Workpiece', 'ROBOT', 'Places workpiece at target location', true, NOW(), NOW()),
    (6, 'Pick Measured Workpiece', 'ROBOT', 'Picks measured workpiece', true, NOW(), NOW()),
    (7, 'Place Measured Workpiece', 'ROBOT', 'Places measured workpiece', true, NOW(), NOW());
