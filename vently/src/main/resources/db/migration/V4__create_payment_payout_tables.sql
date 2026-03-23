-- Migration script to create Payment and Payout tables
-- These tables handle escrow payments and volunteer payouts

-- Create Payment table (organizer deposits)
CREATE TABLE IF NOT EXISTS payment (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    organizer_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    stripe_payment_intent_id VARCHAR(255) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_payment_event FOREIGN KEY (event_id) 
        REFERENCES event(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_organizer FOREIGN KEY (organizer_id) 
        REFERENCES _user(id) ON DELETE CASCADE,
    
    -- Check constraints
    CONSTRAINT chk_payment_status 
        CHECK (status IN ('PENDING', 'COMPLETED', 'REFUNDED', 'FAILED')),
    CONSTRAINT chk_payment_amount_positive 
        CHECK (amount > 0)
);

-- Create Payout table (volunteer payments)
CREATE TABLE IF NOT EXISTS payout (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    application_id BIGINT NOT NULL,
    volunteer_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    platform_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    stripe_transfer_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    retry_count INTEGER NOT NULL DEFAULT 0,
    failure_reason VARCHAR(500),
    
    -- Foreign key constraints
    CONSTRAINT fk_payout_payment FOREIGN KEY (payment_id) 
        REFERENCES payment(id) ON DELETE CASCADE,
    CONSTRAINT fk_payout_application FOREIGN KEY (application_id) 
        REFERENCES application(id) ON DELETE CASCADE,
    CONSTRAINT fk_payout_volunteer FOREIGN KEY (volunteer_id) 
        REFERENCES _user(id) ON DELETE CASCADE,
    
    -- Check constraints
    CONSTRAINT chk_payout_status 
        CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    CONSTRAINT chk_payout_amount_positive 
        CHECK (amount > 0),
    CONSTRAINT chk_payout_platform_fee_non_negative 
        CHECK (platform_fee >= 0),
    CONSTRAINT chk_payout_retry_count_non_negative 
        CHECK (retry_count >= 0)
);

-- Create indexes for Payment table
CREATE INDEX idx_payment_event_id ON payment(event_id);
CREATE INDEX idx_payment_organizer_id ON payment(organizer_id);
CREATE INDEX idx_payment_status ON payment(status);
CREATE INDEX idx_payment_stripe_intent ON payment(stripe_payment_intent_id);
CREATE INDEX idx_payment_created_at ON payment(created_at);

-- Create indexes for Payout table
CREATE INDEX idx_payout_payment_id ON payout(payment_id);
CREATE INDEX idx_payout_application_id ON payout(application_id);
CREATE INDEX idx_payout_volunteer_id ON payout(volunteer_id);
CREATE INDEX idx_payout_status ON payout(status);
CREATE INDEX idx_payout_created_at ON payout(created_at);

-- Composite indexes for common queries
CREATE INDEX idx_payment_organizer_status ON payment(organizer_id, status);
CREATE INDEX idx_payout_volunteer_status ON payout(volunteer_id, status);

-- Comments on tables
COMMENT ON TABLE payment IS 'Tracks organizer deposits held in escrow until event completion';
COMMENT ON TABLE payout IS 'Tracks individual volunteer payouts from escrow after attendance confirmation';

-- Comments on important columns
COMMENT ON COLUMN payment.status IS 'Payment status: PENDING, COMPLETED, REFUNDED, FAILED';
COMMENT ON COLUMN payment.stripe_payment_intent_id IS 'Stripe payment intent ID for tracking and reconciliation';
COMMENT ON COLUMN payout.status IS 'Payout status: PENDING, COMPLETED, FAILED';
COMMENT ON COLUMN payout.platform_fee IS 'Platform service fee deducted from volunteer payment';
COMMENT ON COLUMN payout.retry_count IS 'Number of retry attempts for failed payouts (max 3)';
COMMENT ON COLUMN payout.stripe_transfer_id IS 'Stripe transfer ID for tracking volunteer payout';
