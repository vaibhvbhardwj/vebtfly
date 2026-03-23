CREATE TABLE error_log (
    id BIGSERIAL PRIMARY KEY,
    trace_id VARCHAR(36) NOT NULL UNIQUE,
    error_type VARCHAR(100),
    message TEXT,
    path VARCHAR(500),
    stack_trace TEXT,
    user_id BIGINT,
    ip_address VARCHAR(50),
    http_status INTEGER,
    timestamp TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_error_log_trace_id ON error_log(trace_id);
CREATE INDEX idx_error_log_timestamp ON error_log(timestamp DESC);
CREATE INDEX idx_error_log_user_id ON error_log(user_id);
