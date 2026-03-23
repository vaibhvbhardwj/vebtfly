ALTER TABLE event ADD COLUMN IF NOT EXISTS required_male_volunteers INTEGER DEFAULT 0;
ALTER TABLE event ADD COLUMN IF NOT EXISTS required_female_volunteers INTEGER DEFAULT 0;
UPDATE event SET required_male_volunteers = 0 WHERE required_male_volunteers IS NULL;
UPDATE event SET required_female_volunteers = 0 WHERE required_female_volunteers IS NULL;
