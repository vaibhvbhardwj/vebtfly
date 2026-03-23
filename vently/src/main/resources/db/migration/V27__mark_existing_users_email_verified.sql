-- Mark all existing users as email verified
-- New users will go through OTP verification during registration
UPDATE _user SET email_verified = true WHERE email_verified = false;
