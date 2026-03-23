-- Add email verification token fields to _user table
ALTER TABLE _user
ADD COLUMN verification_token VARCHAR(255),
ADD COLUMN verification_token_expires_at TIMESTAMP;

-- Create index on verification_token for faster lookups
CREATE INDEX idx_user_verification_token ON _user(verification_token);
