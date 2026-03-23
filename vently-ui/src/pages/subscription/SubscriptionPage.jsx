import { useState, useEffect } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { API_BASE_URL } from '../../utils/constants';

const SubscriptionPage = () => {
  const { user } = useAuth();
  const [currentSubscription, setCurrentSubscription] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [selectedPlan, setSelectedPlan] = useState(null);

  // UPI Configuration - Update these with your actual UPI details
  const UPI_CONFIG = {
    upiId: '8368801490@ptsbi', // e.g., user@okhdfcbank
    upiQrCode: 'https://vently-profile-pictures.s3.us-east-1.amazonaws.com/upi.png', // S3 URL to QR code
  };
  useEffect(() => {
    fetchCurrentSubscription();
  }, []);

  const fetchCurrentSubscription = async () => {
    try {
      setLoading(true);
      const response = await fetch(`${API_BASE_URL}/subscriptions/current`, {
        headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` },
      });
      const data = await response.json();
      setCurrentSubscription(data);
    } catch (err) {
      console.error('Error fetching subscription:', err);
      setError('Failed to load subscription details');
    } finally {
      setLoading(false);
    }
  };

  const handleUpgradeClick = (plan) => {
    setSelectedPlan(plan);
    setShowPaymentModal(true);
  };

  const handlePaymentComplete = async () => {
    try {
      // Mark subscription as paid in backend
      const response = await fetch(`${API_BASE_URL}/subscriptions/upgrade`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
        body: JSON.stringify({ tier: selectedPlan.tier }),
      });

      if (!response.ok) throw new Error('Failed to upgrade subscription');

      const data = await response.json();
      setCurrentSubscription(data);
      setShowPaymentModal(false);
      setError(null);
    } catch (err) {
      console.error('Error upgrading subscription:', err);
      setError(err.message);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 p-6 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading subscription details...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 p-6">
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <div className="mb-12">
          <h1 className="text-4xl font-bold text-gray-900 mb-2">Subscription Plans</h1>
          <p className="text-gray-600">Choose the perfect plan for your needs</p>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700">
            {error}
          </div>
        )}

        {/* Pricing Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-12">
          {/* Free Plan */}
          <div className="bg-white rounded-lg border border-gray-200 p-8 hover:shadow-lg transition">
            <div className="mb-6">
              <h2 className="text-2xl font-bold text-gray-900 mb-2">Free</h2>
              <p className="text-gray-600">Perfect for getting started</p>
            </div>

            <div className="mb-6">
              <p className="text-4xl font-bold text-gray-900">₹0<span className="text-lg text-gray-600">/month</span></p>
            </div>

            <ul className="space-y-3 mb-8">
              <li className="flex items-center text-gray-700">
                <svg className="w-5 h-5 text-green-500 mr-3" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                </svg>
                3 events per month
              </li>
              <li className="flex items-center text-gray-700">
                <svg className="w-5 h-5 text-green-500 mr-3" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                </svg>
                5 applications per month
              </li>
              <li className="flex items-center text-gray-700">
                <svg className="w-5 h-5 text-green-500 mr-3" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                </svg>
                Basic profile
              </li>
              <li className="flex items-center text-gray-700">
                <svg className="w-5 h-5 text-green-500 mr-3" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                </svg>
                Community support
              </li>
            </ul>

            <button
              disabled={currentSubscription?.tier === 'FREE'}
              className="w-full py-3 bg-gray-200 text-gray-900 rounded-lg font-medium hover:bg-gray-300 transition disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {currentSubscription?.tier === 'FREE' ? 'Current Plan' : 'Downgrade'}
            </button>
          </div>

          {/* Premium Plan */}
          <div className="bg-gradient-to-br from-blue-600 to-blue-700 rounded-lg border-2 border-blue-600 p-8 text-white hover:shadow-lg transition relative">
            <div className="absolute top-4 right-4 bg-yellow-400 text-blue-900 px-3 py-1 rounded-full text-sm font-bold">
              POPULAR
            </div>

            <div className="mb-6">
              <h2 className="text-2xl font-bold mb-2">Premium</h2>
              <p className="text-blue-100">For serious volunteers & organizers</p>
            </div>

            <div className="mb-6">
              <p className="text-4xl font-bold">₹99<span className="text-lg text-blue-100">/month</span></p>
            </div>

            <ul className="space-y-3 mb-8">
              <li className="flex items-center">
                <svg className="w-5 h-5 text-yellow-300 mr-3" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                </svg>
                Unlimited events
              </li>
              <li className="flex items-center">
                <svg className="w-5 h-5 text-yellow-300 mr-3" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                </svg>
                Unlimited applications
              </li>
              <li className="flex items-center">
                <svg className="w-5 h-5 text-yellow-300 mr-3" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                </svg>
                Priority support
              </li>
              <li className="flex items-center">
                <svg className="w-5 h-5 text-yellow-300 mr-3" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                </svg>
                Advanced analytics
              </li>
              <li className="flex items-center">
                <svg className="w-5 h-5 text-yellow-300 mr-3" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                </svg>
                Verification badge
              </li>
            </ul>

            <button
              onClick={() => handleUpgradeClick({ tier: 'PREMIUM', price: 99 })}
              disabled={currentSubscription?.tier === 'PREMIUM'}
              className="w-full py-3 bg-white text-blue-600 rounded-lg font-bold hover:bg-blue-50 transition disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {currentSubscription?.tier === 'PREMIUM' ? 'Current Plan' : 'Upgrade Now'}
            </button>
          </div>
        </div>

        {/* Current Subscription Info */}
        {currentSubscription && (
          <div className="bg-white rounded-lg border border-gray-200 p-6">
            <h3 className="text-lg font-bold text-gray-900 mb-4">Your Subscription</h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <p className="text-gray-600 text-sm">Current Plan</p>
                <p className="text-xl font-bold text-gray-900">{currentSubscription.tier}</p>
              </div>
              <div>
                <p className="text-gray-600 text-sm">Status</p>
                <p className="text-xl font-bold text-green-600">{currentSubscription.active ? 'Active' : 'Inactive'}</p>
              </div>
              <div>
                <p className="text-gray-600 text-sm">Renewal Date</p>
                <p className="text-xl font-bold text-gray-900">
                  {new Date(currentSubscription.endDate).toLocaleDateString()}
                </p>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Payment Modal */}
      {showPaymentModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-md w-full p-8">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">Complete Payment</h2>
            <p className="text-gray-600 mb-6">
              Upgrade to <strong>{selectedPlan?.tier}</strong> for <strong>₹{selectedPlan?.price}/month</strong>
            </p>

            {/* UPI QR Code */}
            <div className="mb-6 text-center">
              <p className="text-gray-600 text-sm mb-3">Scan QR Code with any UPI app</p>
              <img
                src={UPI_CONFIG.upiQrCode}
                alt="UPI QR Code"
                className="w-48 h-48 mx-auto border border-gray-300 rounded-lg"
              />
            </div>

            {/* UPI ID */}
            <div className="mb-6 p-4 bg-gray-50 rounded-lg">
              <p className="text-gray-600 text-sm mb-2">Or pay directly using UPI ID:</p>
              <a
                href={`upi://pay?pa=${UPI_CONFIG.upiId}&pn=Vently&am=${selectedPlan?.price}&tn=Premium%20Subscription`}
                className="text-blue-600 hover:text-blue-700 font-mono text-lg break-all"
              >
                {UPI_CONFIG.upiId}
              </a>
            </div>

            {/* Instructions */}
            <div className="mb-6 p-4 bg-blue-50 rounded-lg">
              <p className="text-sm text-gray-700">
                <strong>After payment:</strong> Your subscription will be activated within 5 minutes. If not, please contact support.
              </p>
            </div>

            {/* Buttons */}
            <div className="flex gap-3">
              <button
                onClick={() => setShowPaymentModal(false)}
                className="flex-1 py-2 bg-gray-200 text-gray-900 rounded-lg font-medium hover:bg-gray-300 transition"
              >
                Cancel
              </button>
              <button
                onClick={handlePaymentComplete}
                className="flex-1 py-2 bg-green-600 text-white rounded-lg font-medium hover:bg-green-700 transition"
              >
                I've Paid ✓
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SubscriptionPage;
