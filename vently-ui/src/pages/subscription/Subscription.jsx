import React, { useState, useEffect } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { API_BASE_URL } from '../../utils/constants';

// ── Plan definitions ──────────────────────────────────────────────────────────

const VOLUNTEER_PLANS = [
  {
    tier: 'FREE',
    name: 'Free',
    price: 0,
    badge: null,
    tagline: 'Good for onboarding',
    features: ['5 active applications', 'Basic event filtering', 'Standard support'],
    icon: 'bx-user',
  },
  {
    tier: 'GOLD',
    name: 'Gold',
    price: 99,
    badge: 'Popular',
    tagline: '1 event = ₹400–₹1200 — this pays itself',
    features: ['12 active applications', 'Advanced event filtering', 'Priority support', 'No ads'],
    icon: 'bx-medal',
  },
  {
    tier: 'PLATINUM',
    name: 'Platinum',
    price: 199,
    badge: 'Best Value',
    tagline: 'For daily workers — still < 1 event earning',
    features: ['Unlimited applications', 'Advanced filtering', 'Priority support', 'No ads', 'Priority visibility'],
    icon: 'bx-diamond',
  },
];

const ORGANIZER_PLANS = [
  {
    tier: 'FREE',
    name: 'Free',
    price: 0,
    badge: null,
    tagline: 'Try before you commit',
    features: ['3 event posts', 'Basic applicant filtering', 'Standard support'],
    icon: 'bx-user',
  },
  {
    tier: 'GOLD',
    name: 'Gold',
    price: 299,
    badge: 'Popular',
    tagline: 'Save middleman cost on 8 events',
    features: ['8 event posts', 'Advanced applicant filtering', 'Priority support', 'Analytics dashboard'],
    icon: 'bx-medal',
  },
  {
    tier: 'PLATINUM',
    name: 'Platinum',
    price: 799,
    badge: 'Best Value',
    tagline: 'One event budget = months of unlimited posts',
    features: ['Unlimited event posts', 'Advanced filtering', 'Priority support', 'Full analytics', 'Bulk operations'],
    icon: 'bx-diamond',
  },
];

// ── Component ─────────────────────────────────────────────────────────────────

const Subscription = () => {
  const { user } = useAuth();
  const [subscription, setSubscription] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [upgrading, setUpgrading] = useState(null); // tier being upgraded to

  const plans = user?.role === 'ORGANIZER' ? ORGANIZER_PLANS : VOLUNTEER_PLANS;
  const currentTier = subscription?.tier || 'FREE';

  useEffect(() => {
    fetchSubscription();
  }, []);

  const fetchSubscription = async () => {
    try {
      setLoading(true);
      const res = await fetch(`${API_BASE_URL}/subscriptions/current`, {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
      });
      if (!res.ok) throw new Error('Failed to fetch subscription');
      setSubscription(await res.json());
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const loadRazorpay = () =>
    new Promise((resolve) => {
      if (window.Razorpay) return resolve(true);
      const s = document.createElement('script');
      s.src = 'https://checkout.razorpay.com/v1/checkout.js';
      s.onload = () => resolve(true);
      s.onerror = () => resolve(false);
      document.body.appendChild(s);
    });

  const handleUpgrade = async (tier) => {
    try {
      setUpgrading(tier);
      setError(null);

      const loaded = await loadRazorpay();
      if (!loaded) throw new Error('Failed to load payment gateway. Please try again.');

      const orderRes = await fetch(`${API_BASE_URL}/subscriptions/create-order`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
        body: JSON.stringify({ tier }),
      });
      if (!orderRes.ok) throw new Error('Failed to create payment order');
      const orderData = await orderRes.json();

      const options = {
        key: orderData.razorpayKeyId,
        amount: orderData.amount * 100,
        currency: orderData.currency,
        name: 'Vently',
        description: orderData.description,
        order_id: orderData.orderId,
        prefill: { name: orderData.userName, email: orderData.userEmail, contact: orderData.userPhone },
        theme: { color: '#807aeb' },
        handler: async (response) => {
          try {
            const verifyRes = await fetch(`${API_BASE_URL}/subscriptions/verify-payment`, {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem('token')}`,
              },
              body: JSON.stringify({
                orderId: response.razorpay_order_id,
                paymentId: response.razorpay_payment_id,
                signature: response.razorpay_signature,
                tier,
              }),
            });
            if (!verifyRes.ok) throw new Error('Payment verification failed');
            setSubscription(await verifyRes.json());
          } catch (err) {
            setError('Payment verification failed. Please contact support.');
          } finally {
            setUpgrading(null);
          }
        },
        modal: { ondismiss: () => setUpgrading(null) },
      };

      new window.Razorpay(options).open();
    } catch (err) {
      setError(err.message);
      setUpgrading(null);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ background: '#ebf2fa' }}>
        <div className="animate-spin rounded-full h-10 w-10 border-b-2" style={{ borderColor: '#807aeb' }} />
      </div>
    );
  }

  return (
    <div className="min-h-screen py-10 px-4" style={{ background: '#ebf2fa' }}>
      <div className="max-w-5xl mx-auto">

        {/* Header */}
        <div className="text-center mb-10">
          <h1 className="text-3xl font-bold mb-2" style={{ color: '#111827' }}>Choose Your Plan</h1>
          <p style={{ color: '#6B7280' }}>
            {user?.role === 'ORGANIZER'
              ? 'Post events and find the right volunteers'
              : 'Apply to more events and grow your profile'}
          </p>
        </div>

        {error && (
          <div className="mb-6 p-4 rounded-xl text-sm" style={{ background: '#FEE2E2', color: '#EF4444' }}>
            {error}
          </div>
        )}

        {/* Plan Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
          {plans.map((plan) => {
            const isCurrent = currentTier === plan.tier;
            const isHigher = tierRank(plan.tier) > tierRank(currentTier);
            const isLower = tierRank(plan.tier) < tierRank(currentTier);
            const isPlatinum = plan.tier === 'PLATINUM';
            const isGold = plan.tier === 'GOLD';

            return (
              <div
                key={plan.tier}
                className="rounded-2xl p-7 flex flex-col shadow-sm transition"
                style={{
                  background: isPlatinum ? 'linear-gradient(135deg, #807aeb 0%, #6b64d4 100%)' : '#fff',
                  border: isCurrent ? '2.5px solid #807aeb' : '1.5px solid #e5e7eb',
                  color: isPlatinum ? '#fff' : '#111827',
                  position: 'relative',
                  overflow: 'hidden',
                }}
              >
                {/* Badge */}
                {plan.badge && (
                  <span
                    className="absolute top-4 right-4 px-3 py-1 rounded-full text-xs font-bold"
                    style={{
                      background: isPlatinum ? 'rgba(255,255,255,0.2)' : '#EDE9FE',
                      color: isPlatinum ? '#fff' : '#807aeb',
                    }}
                  >
                    {plan.badge}
                  </span>
                )}

                {/* Icon + Name */}
                <div className="flex items-center gap-3 mb-4">
                  <div
                    className="w-10 h-10 rounded-xl flex items-center justify-center"
                    style={{ background: isPlatinum ? 'rgba(255,255,255,0.2)' : '#EDE9FE' }}
                  >
                    <i className={`bx ${plan.icon} text-xl`} style={{ color: isPlatinum ? '#fff' : '#807aeb' }} />
                  </div>
                  <div>
                    <p className="font-bold text-lg leading-tight">{plan.name}</p>
                    {isCurrent && (
                      <span
                        className="text-xs font-semibold px-2 py-0.5 rounded-full"
                        style={{ background: isPlatinum ? 'rgba(255,255,255,0.25)' : '#EDE9FE', color: isPlatinum ? '#fff' : '#807aeb' }}
                      >
                        Current
                      </span>
                    )}
                  </div>
                </div>

                {/* Price */}
                <div className="mb-2">
                  {plan.price === 0 ? (
                    <span className="text-4xl font-bold">Free</span>
                  ) : (
                    <>
                      <span className="text-4xl font-bold">₹{plan.price}</span>
                      <span className="text-sm ml-1" style={{ opacity: 0.7 }}>/month</span>
                    </>
                  )}
                </div>

                {/* Tagline */}
                <p className="text-sm mb-5" style={{ opacity: isPlatinum ? 0.85 : undefined, color: isPlatinum ? undefined : '#6B7280' }}>
                  {plan.tagline}
                </p>

                {/* Features */}
                <ul className="space-y-2 mb-6 flex-1">
                  {plan.features.map((f, i) => (
                    <li key={i} className="flex items-start gap-2 text-sm">
                      <i
                        className="bx bx-check-circle text-base mt-0.5 flex-shrink-0"
                        style={{ color: isPlatinum ? 'rgba(255,255,255,0.8)' : '#10B981' }}
                      />
                      <span style={{ opacity: isPlatinum ? 0.9 : undefined, color: isPlatinum ? undefined : '#374151' }}>{f}</span>
                    </li>
                  ))}
                </ul>

                {/* CTA */}
                {isCurrent ? (
                  <button
                    disabled
                    className="w-full py-3 rounded-xl text-sm font-semibold cursor-not-allowed"
                    style={{
                      background: isPlatinum ? 'rgba(255,255,255,0.2)' : '#F3F4F6',
                      color: isPlatinum ? '#fff' : '#9CA3AF',
                    }}
                  >
                    Current Plan
                  </button>
                ) : isHigher ? (
                  <button
                    onClick={() => handleUpgrade(plan.tier)}
                    disabled={upgrading === plan.tier}
                    className="w-full py-3 rounded-xl text-sm font-semibold transition hover:opacity-90 disabled:opacity-60"
                    style={{
                      background: isPlatinum ? 'rgba(255,255,255,0.25)' : '#807aeb',
                      color: '#fff',
                    }}
                  >
                    {upgrading === plan.tier ? 'Processing...' : `Upgrade to ${plan.name}`}
                  </button>
                ) : (
                  <button
                    disabled
                    className="w-full py-3 rounded-xl text-sm font-medium cursor-not-allowed"
                    style={{ background: '#F3F4F6', color: '#9CA3AF' }}
                  >
                    Lower Plan
                  </button>
                )}
              </div>
            );
          })}
        </div>

        {/* Comparison table */}
        <div className="rounded-2xl overflow-hidden shadow-sm mb-10" style={{ background: '#fff', border: '1.5px solid #e5e7eb' }}>
          <div className="px-6 py-4 border-b" style={{ borderColor: '#e5e7eb' }}>
            <h2 className="font-semibold" style={{ color: '#111827' }}>Plan Comparison</h2>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr style={{ background: '#F9FAFB' }}>
                  <th className="text-left px-6 py-3 font-medium" style={{ color: '#6B7280' }}>Feature</th>
                  <th className="text-center px-4 py-3 font-medium" style={{ color: '#6B7280' }}>Free</th>
                  <th className="text-center px-4 py-3 font-medium" style={{ color: '#807aeb' }}>Gold</th>
                  <th className="text-center px-4 py-3 font-medium" style={{ color: '#807aeb' }}>Platinum</th>
                </tr>
              </thead>
              <tbody>
                {user?.role === 'ORGANIZER' ? (
                  <>
                    <CompRow label="Event posts" free="3" gold="8" platinum="Unlimited" />
                    <CompRow label="Applicant filtering" free="Basic" gold="Advanced" platinum="Advanced" />
                    <CompRow label="Analytics" free="—" gold="✓" platinum="Full" />
                    <CompRow label="Bulk operations" free="—" gold="—" platinum="✓" />
                    <CompRow label="Support" free="Standard" gold="Priority" platinum="Priority" />
                  </>
                ) : (
                  <>
                    <CompRow label="Active applications" free="5" gold="12" platinum="Unlimited" />
                    <CompRow label="Event filtering" free="Basic" gold="Advanced" platinum="Advanced" />
                    <CompRow label="Ads" free="Yes" gold="No" platinum="No" />
                    <CompRow label="Visibility boost" free="—" gold="—" platinum="✓" />
                    <CompRow label="Support" free="Standard" gold="Priority" platinum="Priority" />
                  </>
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* FAQ */}
        <div>
          <h2 className="text-lg font-semibold mb-4" style={{ color: '#111827' }}>FAQ</h2>
          <div className="space-y-3">
            {[
              { q: 'Can I cancel anytime?', a: 'Yes. Access continues until the end of your billing period.' },
              { q: 'What payment methods are accepted?', a: 'UPI (GPay, PhonePe, Paytm, etc.) via Razorpay.' },
              { q: 'Can I switch between Gold and Platinum?', a: 'Yes — upgrading takes effect immediately. Downgrading takes effect at the next billing cycle.' },
            ].map((faq, i) => (
              <div key={i} className="rounded-2xl p-5" style={{ background: '#fff', border: '1.5px solid #e5e7eb' }}>
                <p className="font-semibold text-sm mb-1" style={{ color: '#111827' }}>{faq.q}</p>
                <p className="text-sm" style={{ color: '#6B7280' }}>{faq.a}</p>
              </div>
            ))}
          </div>
        </div>

      </div>
    </div>
  );
};

// ── Helpers ───────────────────────────────────────────────────────────────────

function tierRank(tier) {
  return { FREE: 0, GOLD: 1, PLATINUM: 2 }[tier] ?? 0;
}

function CompRow({ label, free, gold, platinum }) {
  return (
    <tr className="border-t" style={{ borderColor: '#F3F4F6' }}>
      <td className="px-6 py-3" style={{ color: '#374151' }}>{label}</td>
      <td className="text-center px-4 py-3" style={{ color: '#6B7280' }}>{free}</td>
      <td className="text-center px-4 py-3 font-medium" style={{ color: '#807aeb' }}>{gold}</td>
      <td className="text-center px-4 py-3 font-medium" style={{ color: '#807aeb' }}>{platinum}</td>
    </tr>
  );
}

export default Subscription;
