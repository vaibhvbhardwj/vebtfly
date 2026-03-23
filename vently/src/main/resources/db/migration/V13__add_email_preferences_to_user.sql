-- Add email notification preference columns to _user table
ALTER TABLE _user
ADD COLUMN email_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN notify_on_application_status BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN notify_on_event_cancellation BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN notify_on_payment BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN notify_on_dispute_resolution BOOLEAN NOT NULL DEFAULT TRUE;
