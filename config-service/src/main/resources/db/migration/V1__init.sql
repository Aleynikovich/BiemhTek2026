-- Create programs table
CREATE TABLE programs (
    id SERIAL PRIMARY KEY,
    program_number INTEGER NOT NULL UNIQUE,
    program_name VARCHAR(255) NOT NULL,
    program_type VARCHAR(50) NOT NULL CHECK (program_type IN ('ROBOT', 'VISION')),
    description TEXT,
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create workpiece_positions table
CREATE TABLE workpiece_positions (
    id SERIAL PRIMARY KEY,
    x DOUBLE PRECISION NOT NULL,
    y DOUBLE PRECISION NOT NULL,
    z DOUBLE PRECISION NOT NULL,
    rx DOUBLE PRECISION NOT NULL,
    ry DOUBLE PRECISION NOT NULL,
    rz DOUBLE PRECISION NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    source_program VARCHAR(255) NOT NULL,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on program_number for fast lookups
CREATE INDEX idx_programs_program_number ON programs(program_number);

-- Create index on created_at for sorting workpiece positions
CREATE INDEX idx_workpiece_positions_created_at ON workpiece_positions(created_at DESC);

-- Insert sample programs
INSERT INTO programs (program_number, program_name, program_type, description, enabled) VALUES
(1, 'Get New Workpiece Position', 'VISION', 'Captures workpiece position from vision system', true),
(2, 'Calibration', 'VISION', 'Calibrates the vision system', true),
(3, 'Test Calibration', 'VISION', 'Tests the calibration', true),
(4, 'Pick New Workpiece', 'ROBOT', 'Picks a new workpiece based on vision data', true),
(5, 'Place New Workpiece', 'ROBOT', 'Places the picked workpiece', true),
(6, 'Pick Measured Workpiece', 'ROBOT', 'Picks a measured workpiece', true),
(7, 'Place Measured Workpiece', 'ROBOT', 'Places a measured workpiece', true);
