-- Drop the old check constraint that only allowed FREE and PREMIUM
ALTER TABLE subscription DROP CONSTRAINT IF EXISTS chk_subscription_tier;

-- Update any remaining PREMIUM rows to PLATINUM
UPDATE subscription SET tier = 'PLATINUM' WHERE tier = 'PREMIUM';

-- Add new constraint with all three tiers
ALTER TABLE subscription ADD CONSTRAINT chk_subscription_tier 
    CHECK (tier IN ('FREE', 'GOLD', 'PLATINUM'));

-- Drop the partial index that referenced PREMIUM
DROP INDEX IF EXISTS idx_subscription_premium_active;

-- Recreate it for paid tiers
CREATE INDEX IF NOT EXISTS idx_subscription_paid_active 
    ON subscription(tier, active) WHERE tier IN ('GOLD', 'PLATINUM') AND active = TRUE;
