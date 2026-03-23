-- Migration script to extend User table with profile fields
-- This script adds new columns to the existing _user table

-- Add common profile fields
ALTER TABLE _user ADD COLUMN IF NOT EXISTS bio VARCHAR(1000);
ALTER TABLE _user ADD COLUMN IF NOT EXISTS phone VARCHAR(255);
ALTER TABLE _user ADD COLUMN IF NOT EXISTS profile_picture_url VARCHAR(500);
ALTER TABLE _user ADD COLUMN IF NOT EXISTS verification_badge BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE _user ADD COLUMN IF NOT EXISTS no_show_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE _user ADD COLUMN IF NOT EXISTS account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE _user ADD COLUMN IF NOT EXISTS suspended_until TIMESTAMP;
ALTER TABLE _user ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT FALSE;

-- Add volunteer-specific fields
ALTER TABLE _user ADD COLUMN IF NOT EXISTS skills VARCHAR(2000);
ALTER TABLE _user ADD COLUMN IF NOT EXISTS availability VARCHAR(1000);
ALTER TABLE _user ADD COLUMN IF NOT EXISTS experience VARCHAR(2000);

-- Add organizer-specific fields
ALTER TABLE _user ADD COLUMN IF NOT EXISTS organization_name VARCHAR(255);
ALTER TABLE _user ADD COLUMN IF NOT EXISTS organization_details VARCHAR(2000);

-- Add audit fields
ALTER TABLE _user ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE _user ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Create index on email for faster lookups (if not exists)
CREATE INDEX IF NOT EXISTS idx_user_email ON _user(email);

-- Create index on account_status for admin queries
CREATE INDEX IF NOT EXISTS idx_user_account_status ON _user(account_status);

-- Create index on role for filtering
CREATE INDEX IF NOT EXISTS idx_user_role ON _user(role);

-- Add check constraint for account_status
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_account_status') THEN
        ALTER TABLE _user ADD CONSTRAINT chk_account_status 
            CHECK (account_status IN ('ACTIVE', 'SUSPENDED', 'BANNED'));
    END IF;
END $$;

-- Add check constraint for role
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_role') THEN
        ALTER TABLE _user ADD CONSTRAINT chk_role 
            CHECK (role IN ('VOLUNTEER', 'ORGANIZER', 'ADMIN'));
    END IF;
END $$;

-- Update existing users to have default values
UPDATE _user SET 
    verification_badge = FALSE,
    no_show_count = 0,
    account_status = 'ACTIVE',
    email_verified = FALSE,
    created_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE verification_badge IS NULL 
   OR no_show_count IS NULL 
   OR account_status IS NULL 
   OR email_verified IS NULL
   OR created_at IS NULL
   OR updated_at IS NULL;
