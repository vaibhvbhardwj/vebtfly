-- Migration script to create Notification table
-- This table stores in-app notifications for users with 30-day retention

CREATE TABLE IF NOT EXISTS notification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    
    -- Foreign key constraint
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) 
        REFERENCES _user(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_notification_user_id ON notification(user_id);
CREATE INDEX idx_notification_read ON notification(read);
CREATE INDEX idx_notification_expires_at ON notification(expires_at);
CREATE INDEX idx_notification_created_at ON notification(created_at);

-- Composite indexes for common queries
-- Index for fetching user's notifications ordered by creation time
CREATE INDEX idx_notification_user_created ON notification(user_id, created_at DESC);

-- Index for counting unread notifications (most common query)
CREATE INDEX idx_notification_user_read ON notification(user_id, read);

-- Index for cleanup job to find expired notifications
-- Note: Removed WHERE clause with CURRENT_TIMESTAMP as it's not immutable
CREATE INDEX idx_notification_expires_cleanup ON notification(expires_at);

-- Index for filtering by type
CREATE INDEX idx_notification_user_type ON notification(user_id, type);

-- Comments on table
COMMENT ON TABLE notification IS 'Stores in-app notifications for users with 30-day retention policy';

-- Comments on important columns
COMMENT ON COLUMN notification.user_id IS 'User who receives the notification';
COMMENT ON COLUMN notification.type IS 'Notification type (e.g., APPLICATION_STATUS, EVENT_REMINDER, PAYMENT_RECEIVED)';
COMMENT ON COLUMN notification.title IS 'Short notification title displayed in UI';
COMMENT ON COLUMN notification.message IS 'Detailed notification message content';
COMMENT ON COLUMN notification.read IS 'Whether the user has read the notification';
COMMENT ON COLUMN notification.created_at IS 'Timestamp when notification was created';
COMMENT ON COLUMN notification.expires_at IS 'Timestamp when notification expires (default: created_at + 30 days)';
