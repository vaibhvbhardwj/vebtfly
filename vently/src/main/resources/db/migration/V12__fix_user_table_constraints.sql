-- Migration script to fix user table constraints and ensure all columns exist
-- This script ensures all required columns for attendance and no-show tracking are properly defined

-- Ensure verification_token and verification_token_expires_at columns exist
DO $$
BEGIN
    -- Check if verification_token column exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = '_user' AND column_name = 'verification_token'
    ) THEN
        ALTER TABLE _user ADD COLUMN verification_token VARCHAR(255);
    END IF;

    -- Check if verification_token_expires_at column exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = '_user' AND column_name = 'verification_token_expires_at'
    ) THEN
        ALTER TABLE _user ADD COLUMN verification_token_expires_at TIMESTAMP;
    END IF;
END $$;

-- Ensure account_status has proper check constraint
DO $$
BEGIN
    -- Drop existing constraint if it exists
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_account_status') THEN
        ALTER TABLE _user DROP CONSTRAINT chk_account_status;
    END IF;
    
    -- Add updated constraint
    ALTER TABLE _user ADD CONSTRAINT chk_account_status 
        CHECK (account_status IN ('ACTIVE', 'SUSPENDED', 'BANNED'));
END $$;

-- Ensure suspended_until column exists and is nullable
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = '_user' AND column_name = 'suspended_until'
    ) THEN
        ALTER TABLE _user ADD COLUMN suspended_until TIMESTAMP;
    END IF;
END $$;

-- Ensure no_show_count has default value of 0
ALTER TABLE _user ALTER COLUMN no_show_count SET DEFAULT 0;
UPDATE _user SET no_show_count = 0 WHERE no_show_count IS NULL;

-- Ensure account_status has default value of 'ACTIVE'
ALTER TABLE _user ALTER COLUMN account_status SET DEFAULT 'ACTIVE';
UPDATE _user SET account_status = 'ACTIVE' WHERE account_status IS NULL;

-- Ensure email_verified has default value of FALSE
ALTER TABLE _user ALTER COLUMN email_verified SET DEFAULT FALSE;
UPDATE _user SET email_verified = FALSE WHERE email_verified IS NULL;

-- Create index on verification_token if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE tablename = '_user' AND indexname = 'idx_user_verification_token'
    ) THEN
        CREATE INDEX idx_user_verification_token ON _user(verification_token);
    END IF;
END $$;

-- Create index on suspended_until for faster queries
CREATE INDEX IF NOT EXISTS idx_user_suspended_until ON _user(suspended_until);

-- Create index on no_show_count for admin queries
CREATE INDEX IF NOT EXISTS idx_user_no_show_count ON _user(no_show_count);

-- Add comment to table for documentation
COMMENT ON TABLE _user IS 'User accounts for Event Volunteer Management Platform with attendance and no-show tracking';