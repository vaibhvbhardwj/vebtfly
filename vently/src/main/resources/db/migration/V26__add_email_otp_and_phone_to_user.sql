-- Add email OTP fields for registration verification
ALTER TABLE _user ADD COLUMN IF NOT EXISTS email_otp VARCHAR(6);
ALTER TABLE _user ADD COLUMN IF NOT EXISTS email_otp_expires_at TIMESTAMP;

-- Phone is already present but ensure it exists
ALTER TABLE _user ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
