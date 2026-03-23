-- Update payment and payout tables to use Razorpay instead of Stripe

-- Update payment table
ALTER TABLE payment 
RENAME COLUMN stripe_payment_intent_id TO razorpay_payment_id;

-- Update payout table
ALTER TABLE payout 
RENAME COLUMN stripe_transfer_id TO razorpay_transfer_id;

-- Update column comments
COMMENT ON COLUMN payment.razorpay_payment_id IS 'Razorpay payment ID for tracking payments';
COMMENT ON COLUMN payout.razorpay_transfer_id IS 'Razorpay transfer ID for tracking payouts';

-- Update indexes if they exist
DROP INDEX IF EXISTS idx_payment_stripe_intent_id;
DROP INDEX IF EXISTS idx_payout_stripe_transfer_id;

CREATE INDEX idx_payment_razorpay_id ON payment(razorpay_payment_id);
CREATE INDEX idx_payout_razorpay_transfer_id ON payout(razorpay_transfer_id);