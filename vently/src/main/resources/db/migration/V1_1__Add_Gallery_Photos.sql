-- Add gallery_photos column to _user table for photo gallery feature
-- This allows volunteers to upload up to 3 photos to their profile

ALTER TABLE _user ADD COLUMN IF NOT EXISTS gallery_photos TEXT;

-- Add comment for documentation
COMMENT ON COLUMN _user.gallery_photos IS 'JSON array of gallery photo URLs (max 3 photos)';
