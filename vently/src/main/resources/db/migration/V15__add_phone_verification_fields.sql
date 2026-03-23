-- Add phone verification fields to _user table
ALTER TABLE _user
    ADD COLUMN IF NOT EXISTS phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS phone_otp VARCHAR(6),
    ADD COLUMN IF NOT EXISTS phone_otp_expires_at TIMESTAMP;
