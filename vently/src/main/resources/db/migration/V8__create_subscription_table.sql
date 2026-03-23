-- Migration script to create Subscription table
-- This table stores user subscription information for freemium tier management

CREATE TABLE IF NOT EXISTS subscription (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    tier VARCHAR(20) NOT NULL DEFAULT 'FREE',
    start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date DATE,
    razorpay_payment_id VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Foreign key constraint
    CONSTRAINT fk_subscription_user FOREIGN KEY (user_id) 
        REFERENCES _user(id) ON DELETE CASCADE,
    
    -- Check constraint for tier values
    CONSTRAINT chk_subscription_tier CHECK (tier IN ('FREE', 'PREMIUM'))
);

-- Create indexes for better query performance
CREATE INDEX idx_subscription_user_id ON subscription(user_id);
CREATE INDEX idx_subscription_tier ON subscription(tier);
CREATE INDEX idx_subscription_active ON subscription(active);
CREATE INDEX idx_subscription_razorpay_id ON subscription(razorpay_payment_id);

-- Composite indexes for common queries
-- Index for finding active subscriptions by tier
CREATE INDEX idx_subscription_tier_active ON subscription(tier, active);

-- Index for finding active premium subscriptions (most common query)
CREATE INDEX idx_subscription_premium_active ON subscription(tier, active) WHERE tier = 'PREMIUM' AND active = TRUE;

-- Index for finding expired subscriptions for cleanup
-- Note: Removed WHERE clause with CURRENT_DATE as it's not immutable
CREATE INDEX idx_subscription_expired ON subscription(end_date, active);

-- Index for finding subscriptions expiring soon (for renewal reminders)
CREATE INDEX idx_subscription_end_date ON subscription(end_date) WHERE active = TRUE;

-- Index for analytics queries by user role and tier
CREATE INDEX idx_subscription_analytics ON subscription(tier, active);

-- Comments on table
COMMENT ON TABLE subscription IS 'Stores user subscription information for freemium tier management';

-- Comments on important columns
COMMENT ON COLUMN subscription.user_id IS 'User who owns the subscription (unique - one subscription per user)';
COMMENT ON COLUMN subscription.tier IS 'Subscription tier: FREE or PREMIUM';
COMMENT ON COLUMN subscription.start_date IS 'Date when subscription started';
COMMENT ON COLUMN subscription.end_date IS 'Date when subscription ends (NULL for lifetime/ongoing subscriptions)';
COMMENT ON COLUMN subscription.razorpay_payment_id IS 'Razorpay payment ID for premium subscriptions';
COMMENT ON COLUMN subscription.active IS 'Whether the subscription is currently active';

-- Create default FREE subscriptions for existing users
INSERT INTO subscription (user_id, tier, start_date, active)
SELECT id, 'FREE', CURRENT_DATE, TRUE
FROM _user
WHERE NOT EXISTS (
    SELECT 1 FROM subscription WHERE subscription.user_id = _user.id
);
