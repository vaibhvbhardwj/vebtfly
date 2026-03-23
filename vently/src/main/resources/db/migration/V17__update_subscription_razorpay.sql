-- Update subscription table to use Razorpay instead of Stripe
-- Rename stripe_subscription_id column to razorpay_payment_id

ALTER TABLE subscription 
RENAME COLUMN stripe_subscription_id TO razorpay_payment_id;

-- Update column comment
COMMENT ON COLUMN subscription.razorpay_payment_id IS 'Razorpay payment ID for premium subscriptions';