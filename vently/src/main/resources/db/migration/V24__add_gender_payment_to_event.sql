ALTER TABLE event ADD COLUMN IF NOT EXISTS payment_per_male_volunteer NUMERIC(10,2);
ALTER TABLE event ADD COLUMN IF NOT EXISTS payment_per_female_volunteer NUMERIC(10,2);
