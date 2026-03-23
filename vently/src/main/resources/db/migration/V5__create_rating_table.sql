-- Migration script to create Rating table
-- This table tracks bidirectional ratings between organizers and volunteers

CREATE TABLE IF NOT EXISTS rating (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    rater_id BIGINT NOT NULL,
    rated_id BIGINT NOT NULL,
    rating INTEGER NOT NULL,
    review VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_rating_event FOREIGN KEY (event_id) 
        REFERENCES event(id) ON DELETE CASCADE,
    CONSTRAINT fk_rating_rater FOREIGN KEY (rater_id) 
        REFERENCES _user(id) ON DELETE CASCADE,
    CONSTRAINT fk_rating_rated FOREIGN KEY (rated_id) 
        REFERENCES _user(id) ON DELETE CASCADE,
    
    -- Unique constraint: one rating per rater-rated pair per event
    CONSTRAINT uk_rating_event_rater_rated UNIQUE (event_id, rater_id, rated_id),
    
    -- Check constraints
    CONSTRAINT chk_rating_value CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT chk_rating_different_users CHECK (rater_id != rated_id)
);

-- Create indexes for better query performance
CREATE INDEX idx_rating_event_id ON rating(event_id);
CREATE INDEX idx_rating_rater_id ON rating(rater_id);
CREATE INDEX idx_rating_rated_id ON rating(rated_id);
CREATE INDEX idx_rating_created_at ON rating(created_at);
CREATE INDEX idx_rating_value ON rating(rating);

-- Composite indexes for common queries
CREATE INDEX idx_rating_rated_value ON rating(rated_id, rating);

-- Comments on table
COMMENT ON TABLE rating IS 'Tracks bidirectional ratings between organizers and volunteers after event completion';

-- Comments on important columns
COMMENT ON COLUMN rating.rating IS 'Rating value from 1 to 5 stars';
COMMENT ON COLUMN rating.review IS 'Optional text review accompanying the rating';
COMMENT ON COLUMN rating.rater_id IS 'User who gave the rating';
COMMENT ON COLUMN rating.rated_id IS 'User who received the rating';
COMMENT ON COLUMN rating.created_at IS 'Timestamp when rating was submitted (must be within 7 days of event completion)';
