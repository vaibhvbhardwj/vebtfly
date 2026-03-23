-- Migration script to create AttendanceCode table
-- This table tracks unique attendance codes for confirmed volunteers

CREATE TABLE IF NOT EXISTS attendance_code (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    volunteer_id BIGINT NOT NULL,
    code VARCHAR(20) NOT NULL,
    marked_at TIMESTAMP,
    marked_by BIGINT,
    
    -- Foreign key constraints
    CONSTRAINT fk_attendance_code_event FOREIGN KEY (event_id) 
        REFERENCES event(id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_code_volunteer FOREIGN KEY (volunteer_id) 
        REFERENCES _user(id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_code_marked_by FOREIGN KEY (marked_by) 
        REFERENCES _user(id) ON DELETE SET NULL,
    
    -- Unique constraint: code must be globally unique
    CONSTRAINT uk_attendance_code_code UNIQUE (code)
);

-- Create indexes for better query performance
CREATE INDEX idx_attendance_code_event_id ON attendance_code(event_id);
CREATE INDEX idx_attendance_code_volunteer_id ON attendance_code(volunteer_id);
CREATE INDEX idx_attendance_code_code ON attendance_code(code);
CREATE INDEX idx_attendance_code_marked_at ON attendance_code(marked_at);
CREATE INDEX idx_attendance_code_marked_by ON attendance_code(marked_by);

-- Composite index for common queries
CREATE INDEX idx_attendance_code_event_marked ON attendance_code(event_id, marked_at);

-- Comment on table
COMMENT ON TABLE attendance_code IS 'Tracks unique attendance codes for confirmed volunteers and their attendance marking status';

-- Comments on important columns
COMMENT ON COLUMN attendance_code.code IS 'Globally unique attendance code generated for each confirmed volunteer';
COMMENT ON COLUMN attendance_code.marked_at IS 'Timestamp when attendance was marked (null if not yet marked)';
COMMENT ON COLUMN attendance_code.marked_by IS 'Organizer who marked the attendance (null if not yet marked)';
