-- Migration script to create Application table
-- This table tracks volunteer applications to events

CREATE TABLE IF NOT EXISTS application (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    volunteer_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP,
    confirmed_at TIMESTAMP,
    declined_at TIMESTAMP,
    confirmation_deadline TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_application_event FOREIGN KEY (event_id) 
        REFERENCES event(id) ON DELETE CASCADE,
    CONSTRAINT fk_application_volunteer FOREIGN KEY (volunteer_id) 
        REFERENCES _user(id) ON DELETE CASCADE,
    
    -- Unique constraint: one application per volunteer per event
    CONSTRAINT uk_application_event_volunteer UNIQUE (event_id, volunteer_id),
    
    -- Check constraint for status
    CONSTRAINT chk_application_status 
        CHECK (status IN ('PENDING', 'ACCEPTED', 'CONFIRMED', 'DECLINED', 'REJECTED', 'CANCELLED'))
);

-- Create indexes for better query performance
CREATE INDEX idx_application_event_id ON application(event_id);
CREATE INDEX idx_application_volunteer_id ON application(volunteer_id);
CREATE INDEX idx_application_status ON application(status);
CREATE INDEX idx_application_applied_at ON application(applied_at);
CREATE INDEX idx_application_confirmation_deadline ON application(confirmation_deadline);

-- Composite index for common queries
CREATE INDEX idx_application_event_status ON application(event_id, status);
CREATE INDEX idx_application_volunteer_status ON application(volunteer_id, status);

-- Comment on table
COMMENT ON TABLE application IS 'Tracks volunteer applications to events with status workflow';

-- Comments on important columns
COMMENT ON COLUMN application.status IS 'Application status: PENDING, ACCEPTED, CONFIRMED, DECLINED, REJECTED, CANCELLED';
COMMENT ON COLUMN application.confirmation_deadline IS 'Deadline for volunteer to confirm acceptance (48 hours after acceptance)';
COMMENT ON COLUMN application.applied_at IS 'Timestamp when volunteer submitted application';
COMMENT ON COLUMN application.accepted_at IS 'Timestamp when organizer accepted application';
COMMENT ON COLUMN application.confirmed_at IS 'Timestamp when volunteer confirmed participation';
COMMENT ON COLUMN application.declined_at IS 'Timestamp when volunteer declined or confirmation expired';
