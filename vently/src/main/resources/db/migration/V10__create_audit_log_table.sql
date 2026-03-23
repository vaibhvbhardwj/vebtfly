-- Migration script to create AuditLog table
-- This table tracks all audit events including authentication, payments, admin actions, and state transitions

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    details JSONB,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraint (nullable for system actions)
    CONSTRAINT fk_audit_log_user FOREIGN KEY (user_id) 
        REFERENCES _user(id) ON DELETE SET NULL
);

-- Create indexes for better query performance
CREATE INDEX idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_log_action ON audit_log(action);
CREATE INDEX idx_audit_log_timestamp ON audit_log(timestamp);
CREATE INDEX idx_audit_log_entity_type ON audit_log(entity_type);
CREATE INDEX idx_audit_log_ip_address ON audit_log(ip_address);

-- Composite indexes for common queries
CREATE INDEX idx_audit_log_user_action ON audit_log(user_id, action);
CREATE INDEX idx_audit_log_user_timestamp ON audit_log(user_id, timestamp);
CREATE INDEX idx_audit_log_action_timestamp ON audit_log(action, timestamp);
CREATE INDEX idx_audit_log_entity_type_entity_id ON audit_log(entity_type, entity_id);

-- GIN index for JSONB details for efficient JSON queries
CREATE INDEX idx_audit_log_details ON audit_log USING GIN (details);

-- Comments on table
COMMENT ON TABLE audit_log IS 'Tracks all audit events including authentication attempts, payment transactions, admin actions, and state transitions';

-- Comments on important columns
COMMENT ON COLUMN audit_log.user_id IS 'User who performed the action (nullable for system actions)';
COMMENT ON COLUMN audit_log.action IS 'Type of action performed (e.g., LOGIN_SUCCESS, PAYMENT_CREATED, USER_SUSPENDED)';
COMMENT ON COLUMN audit_log.entity_type IS 'Type of entity affected (e.g., USER, EVENT, PAYMENT, DISPUTE)';
COMMENT ON COLUMN audit_log.entity_id IS 'ID of the affected entity';
COMMENT ON COLUMN audit_log.details IS 'JSON object containing additional context and details about the action';
COMMENT ON COLUMN audit_log.ip_address IS 'IP address from which the action was performed';
COMMENT ON COLUMN audit_log.timestamp IS 'Timestamp when the action occurred';
