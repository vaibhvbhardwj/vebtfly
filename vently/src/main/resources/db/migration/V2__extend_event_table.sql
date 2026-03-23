-- Migration script to extend Event table with lifecycle fields
-- This script modifies the existing event table structure

-- Drop the old event_volunteers many-to-many table if it exists
-- (We'll use Application entity instead for better tracking)
DROP TABLE IF EXISTS event_volunteers;

-- Modify existing columns to match new structure
ALTER TABLE event ALTER COLUMN title SET NOT NULL;
ALTER TABLE event ALTER COLUMN location SET NOT NULL;
ALTER TABLE event ALTER COLUMN organizer_id SET NOT NULL;

-- Add new columns for event lifecycle
ALTER TABLE event ADD COLUMN IF NOT EXISTS date DATE;
ALTER TABLE event ADD COLUMN IF NOT EXISTS time TIME;
ALTER TABLE event ADD COLUMN IF NOT EXISTS required_volunteers INTEGER NOT NULL DEFAULT 1;
ALTER TABLE event ADD COLUMN IF NOT EXISTS payment_per_volunteer DECIMAL(10, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE event ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE event ADD COLUMN IF NOT EXISTS category VARCHAR(100);
ALTER TABLE event ADD COLUMN IF NOT EXISTS application_deadline TIMESTAMP;
ALTER TABLE event ADD COLUMN IF NOT EXISTS cancellation_reason VARCHAR(1000);

-- Add audit fields
ALTER TABLE event ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE event ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Migrate existing eventDate to date and time columns if they exist
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'event' AND column_name = 'event_date') THEN
        UPDATE event 
        SET date = CAST(event_date AS DATE),
            time = CAST(event_date AS TIME)
        WHERE date IS NULL AND event_date IS NOT NULL;
        
        -- Drop the old column after migration
        ALTER TABLE event DROP COLUMN IF EXISTS event_date;
    END IF;
END $$;

-- Set NOT NULL constraints after data migration
ALTER TABLE event ALTER COLUMN date SET NOT NULL;
ALTER TABLE event ALTER COLUMN time SET NOT NULL;

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_event_status ON event(status);
CREATE INDEX IF NOT EXISTS idx_event_date ON event(date);
CREATE INDEX IF NOT EXISTS idx_event_organizer ON event(organizer_id);
CREATE INDEX IF NOT EXISTS idx_event_category ON event(category);
CREATE INDEX IF NOT EXISTS idx_event_created_at ON event(created_at);

-- Add check constraint for status
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_event_status') THEN
        ALTER TABLE event ADD CONSTRAINT chk_event_status 
            CHECK (status IN ('DRAFT', 'PUBLISHED', 'DEPOSIT_PAID', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'));
    END IF;
END $$;

-- Add check constraint for required_volunteers (must be positive)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_required_volunteers_positive') THEN
        ALTER TABLE event ADD CONSTRAINT chk_required_volunteers_positive 
            CHECK (required_volunteers > 0);
    END IF;
END $$;

-- Add check constraint for payment_per_volunteer (must be non-negative)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_payment_non_negative') THEN
        ALTER TABLE event ADD CONSTRAINT chk_payment_non_negative 
            CHECK (payment_per_volunteer >= 0);
    END IF;
END $$;

-- Update existing events to have default values
UPDATE event SET 
    status = 'DRAFT',
    required_volunteers = 1,
    payment_per_volunteer = 0.00,
    created_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE status IS NULL 
   OR required_volunteers IS NULL 
   OR payment_per_volunteer IS NULL
   OR created_at IS NULL
   OR updated_at IS NULL;
