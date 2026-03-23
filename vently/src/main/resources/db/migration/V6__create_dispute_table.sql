-- Migration script to create Dispute table
-- This table tracks disputes raised by users related to events

CREATE TABLE IF NOT EXISTS dispute (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    raised_by BIGINT NOT NULL,
    against_user_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    description TEXT NOT NULL,
    evidence_urls JSONB,
    resolution TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    resolved_by BIGINT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_dispute_event FOREIGN KEY (event_id) 
        REFERENCES event(id) ON DELETE CASCADE,
    CONSTRAINT fk_dispute_raised_by FOREIGN KEY (raised_by) 
        REFERENCES _user(id) ON DELETE CASCADE,
    CONSTRAINT fk_dispute_against_user FOREIGN KEY (against_user_id) 
        REFERENCES _user(id) ON DELETE SET NULL,
    CONSTRAINT fk_dispute_resolved_by FOREIGN KEY (resolved_by) 
        REFERENCES _user(id) ON DELETE SET NULL,
    
    -- Check constraint for status
    CONSTRAINT chk_dispute_status 
        CHECK (status IN ('OPEN', 'UNDER_REVIEW', 'RESOLVED', 'CLOSED')),
    
    -- Check constraint: resolution required when status is RESOLVED
    CONSTRAINT chk_dispute_resolution 
        CHECK (
            (status = 'RESOLVED' AND resolution IS NOT NULL) OR 
            (status != 'RESOLVED')
        ),
    
    -- Check constraint: resolved_at and resolved_by required when status is RESOLVED or CLOSED
    CONSTRAINT chk_dispute_resolved_fields 
        CHECK (
            (status IN ('RESOLVED', 'CLOSED') AND resolved_at IS NOT NULL AND resolved_by IS NOT NULL) OR 
            (status NOT IN ('RESOLVED', 'CLOSED'))
        )
);

-- Create indexes for better query performance
CREATE INDEX idx_dispute_event_id ON dispute(event_id);
CREATE INDEX idx_dispute_raised_by ON dispute(raised_by);
CREATE INDEX idx_dispute_against_user_id ON dispute(against_user_id);
CREATE INDEX idx_dispute_status ON dispute(status);
CREATE INDEX idx_dispute_created_at ON dispute(created_at);
CREATE INDEX idx_dispute_resolved_at ON dispute(resolved_at);
CREATE INDEX idx_dispute_resolved_by ON dispute(resolved_by);

-- Composite indexes for common queries
CREATE INDEX idx_dispute_status_created ON dispute(status, created_at);
CREATE INDEX idx_dispute_event_status ON dispute(event_id, status);
CREATE INDEX idx_dispute_raised_by_status ON dispute(raised_by, status);

-- GIN index for JSONB evidence_urls for efficient JSON queries
CREATE INDEX idx_dispute_evidence_urls ON dispute USING GIN (evidence_urls);

-- Comments on table
COMMENT ON TABLE dispute IS 'Tracks disputes raised by users related to specific events requiring admin resolution';

-- Comments on important columns
COMMENT ON COLUMN dispute.status IS 'Dispute status: OPEN, UNDER_REVIEW, RESOLVED, CLOSED';
COMMENT ON COLUMN dispute.description IS 'Detailed description of the dispute provided by the user';
COMMENT ON COLUMN dispute.evidence_urls IS 'JSON array of URLs to evidence files (images, documents) stored in S3';
COMMENT ON COLUMN dispute.resolution IS 'Admin resolution decision and notes (required when status is RESOLVED)';
COMMENT ON COLUMN dispute.raised_by IS 'User who raised the dispute';
COMMENT ON COLUMN dispute.against_user_id IS 'User the dispute is against (optional)';
COMMENT ON COLUMN dispute.resolved_by IS 'Admin who resolved or closed the dispute';
COMMENT ON COLUMN dispute.created_at IS 'Timestamp when dispute was created';
COMMENT ON COLUMN dispute.resolved_at IS 'Timestamp when dispute was resolved or closed';
