# Razorpay Setup Guide for Test Mode

## Why Razorpay?
Razorpay is perfect for India-based applications and supports:
- UPI (Google Pay, PhonePe, Paytm, etc.)
- Credit/Debit Cards
- Net Banking
- Wallets
- EMI options

## Getting Test Credentials (5 minutes)

### Step 1: Sign Up
1. Go to https://dashboard.razorpay.com/signup
2. Sign up with your email
3. Complete the basic registration (no business verification needed for test mode)

### Step 2: Get Test API Keys
1. After login, you'll be in **Test Mode** by default (look for "Test Mode" toggle in top-right)
2. Go to **Settings** → **API Keys** (or directly: https://dashboard.razorpay.com/app/keys)
3. Click **Generate Test Keys** (if not already generated)
4. You'll see two keys:
   - **Key ID**: Starts with `rzp_test_` (e.g., `rzp_test_1234567890abcd`)
   - **Key Secret**: A long string (e.g., `abcdefghijklmnopqrstuvwxyz123456`)

### Step 3: Add to Your Project
1. Open `vently/.env` file
2. Add your keys:
   ```
   RAZORPAY_KEY_ID=rzp_test_your_actual_key_id
   RAZORPAY_KEY_SECRET=your_actual_key_secret
   ```
3. Save the file

### Step 4: Verify Setup
Run your Spring Boot application:
```bash
./mvnw spring-boot:run
```

Look for this log message:
```
Initializing Razorpay client with key ID: rzp_test_...
```

If you see a warning about credentials not configured, double-check your `.env` file.

## Test Mode Features

### What You Can Do in Test Mode:
✅ Create orders and test payments
✅ Test all payment methods (UPI, cards, etc.)
✅ Test refunds and transfers
✅ Use test card numbers (no real money)
✅ Simulate payment failures
✅ Test webhooks

### What You CANNOT Do:
❌ Receive real money
❌ Transfer to real bank accounts
❌ Use real UPI IDs or cards

## Test Payment Methods

### Test Cards (No Real Money Charged)
```
Success Card:
Card Number: 4111 1111 1111 1111
CVV: Any 3 digits
Expiry: Any future date
Name: Any name

Failure Card:
Card Number: 4000 0000 0000 0002
CVV: Any 3 digits
Expiry: Any future date
```

### Test UPI
```
Success UPI: success@razorpay
Failure UPI: failure@razorpay
```

### Test Net Banking
All test mode net banking options will work without real authentication.

## Testing Payment Flow

### 1. Create Order (Backend)
```java
Order order = razorpayService.createOrder(
    BigDecimal.valueOf(500),  // ₹500
    "INR",
    "receipt_123"
);
```

### 2. Complete Payment (Frontend)
Use Razorpay Checkout to collect payment:
```javascript
const options = {
  key: 'rzp_test_your_key_id',
  amount: 50000, // Amount in paise (₹500)
  currency: 'INR',
  order_id: order.id,
  handler: function(response) {
    // Send to backend for verification
    verifyPayment(response);
  }
};
const rzp = new Razorpay(options);
rzp.open();
```

### 3. Verify Payment (Backend)
```java
boolean isValid = razorpayService.verifyPaymentSignature(
    orderId,
    paymentId,
    signature
);
```

## Common Issues

### Issue: "Razorpay client not configured"
**Solution:** 
- Check if `.env` file exists in `vently/` directory
- Verify keys are set correctly (no extra spaces)
- Restart your Spring Boot application

### Issue: "Invalid API key"
**Solution:**
- Ensure you're using **Test Mode** keys (start with `rzp_test_`)
- Don't use Live Mode keys in development
- Regenerate keys if needed from dashboard

### Issue: "Payment fails immediately"
**Solution:**
- Use test card numbers provided above
- Don't use real card numbers in test mode
- Check Razorpay dashboard logs for details

## Moving to Production

When ready to go live:

1. **Complete KYC**: Submit business documents on Razorpay dashboard
2. **Get Live Keys**: Generate live API keys (start with `rzp_live_`)
3. **Update Environment**: 
   ```
   RAZORPAY_KEY_ID=rzp_live_your_live_key_id
   RAZORPAY_KEY_SECRET=your_live_key_secret
   ```
4. **Enable Webhooks**: Set up webhook URL for payment notifications
5. **Test Thoroughly**: Test with small real amounts first

## Razorpay vs Stripe Comparison

| Feature | Razorpay | Stripe |
|---------|----------|--------|
| **India Support** | ✅ Excellent | ⚠️ Invite only |
| **UPI Payments** | ✅ Yes | ❌ No |
| **Setup Time** | 5 minutes | Requires invite |
| **Transaction Fee** | 2% + GST | 2.9% + $0.30 |
| **Payout Time** | T+1 to T+3 days | T+2 to T+7 days |
| **Local Support** | ✅ Yes | Limited |
| **Documentation** | Good | Excellent |

## Resources

- **Dashboard**: https://dashboard.razorpay.com
- **Documentation**: https://razorpay.com/docs/
- **API Reference**: https://razorpay.com/docs/api/
- **Test Cards**: https://razorpay.com/docs/payments/payments/test-card-details/
- **Support**: support@razorpay.com

## Quick Reference

```bash
# Check if credentials are loaded
echo $RAZORPAY_KEY_ID

# Run application with explicit credentials
RAZORPAY_KEY_ID=rzp_test_xxx RAZORPAY_KEY_SECRET=xxx ./mvnw spring-boot:run

# View Razorpay logs in application
grep -i razorpay logs/application.log
```

## Next Steps

1. ✅ Get test API keys from Razorpay dashboard
2. ✅ Add keys to `.env` file
3. ✅ Run the application
4. ✅ Test payment creation from API
5. ✅ Integrate Razorpay Checkout in frontend
6. ✅ Test complete payment flow

Need help? Check the Razorpay documentation or raise an issue!
