-- Add attendance_status column to attendance_code table
-- Values: PRESENT, LATE, or NULL (not yet marked)

ALTER TABLE attendance_code
    ADD COLUMN IF NOT EXISTS attendance_status VARCHAR(10);

COMMENT ON COLUMN attendance_code.attendance_status IS 'Attendance status: PRESENT, LATE, or NULL if not marked';
