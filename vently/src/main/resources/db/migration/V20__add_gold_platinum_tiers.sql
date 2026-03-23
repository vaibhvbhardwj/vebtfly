-- Rename old PREMIUM tier rows to PLATINUM so existing subscribers keep access
UPDATE subscription SET tier = 'PLATINUM' WHERE tier = 'PREMIUM';

-- Add end_date column if it doesn't exist (safe no-op if already present)
ALTER TABLE subscription ADD COLUMN IF NOT EXISTS end_date DATE;
