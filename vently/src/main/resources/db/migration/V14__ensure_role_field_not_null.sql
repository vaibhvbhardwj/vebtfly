-- Migration script to ensure role field is not null and has proper constraints
-- This fixes the 403 Forbidden error for organizers

-- Ensure role column exists and is not null
DO $
BEGIN
    -- Check if role column exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = '_user' AND column_name = 'role'
    ) THEN
        ALTER TABLE _user ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'VOLUNTEER';
    ELSE
        -- If column exists, ensure it's not null by setting default and updating existing nulls
        ALTER TABLE _user ALTER COLUMN role SET NOT NULL;
        ALTER TABLE _user ALTER COLUMN role SET DEFAULT 'VOLUNTEER';
        UPDATE _user SET role = 'VOLUNTEER' WHERE role IS NULL;
    END IF;
END $;

-- Ensure role has proper check constraint
DO $
BEGIN
    -- Drop existing constraint if it exists
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_role') THEN
        ALTER TABLE _user DROP CONSTRAINT chk_role;
    END IF;
    
    -- Add updated constraint
    ALTER TABLE _user ADD CONSTRAINT chk_role 
        CHECK (role IN ('VOLUNTEER', 'ORGANIZER', 'ADMIN'));
END $;

-- Create index on role if it doesn't exist
CREATE INDEX IF NOT EXISTS idx_user_role ON _user(role);

-- Add comment to role column for documentation
COMMENT ON COLUMN _user.role IS 'User role: VOLUNTEER, ORGANIZER, or ADMIN';
