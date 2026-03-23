import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Modal } from '../../components/shared/Modal';
import { formatCurrency, formatDate } from '../../utils/formatters';
import { API_BASE_URL } from '../../utils/constants';

const PaymentDeposit = () => {
  const { eventId } = useParams();
  const navigate = useNavigate();
  const [event, setEvent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);
  const [isSuccessModalOpen, setIsSuccessModalOpen] = useState(false);
  const [cardData, setCardData] = useState({
    cardNumber: '',
    expiryDate: '',
    cvc: '',
    cardholderName: '',
  });
  const [cardErrors, setCardErrors] = useState({});

  useEffect(() => {
    fetchEventDetails();
  }, [eventId]);

  const fetchEventDetails = async () => {
    setLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/events/${eventId}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
      });

      if (!response.ok) throw new Error('Failed to fetch event');

      const data = await response.json();
      setEvent(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleCardChange = (e) => {
    const { name, value } = e.target;
    let formattedValue = value;

    if (name === 'cardNumber') {
      formattedValue = value.replace(/\s/g, '').replace(/(\d{4})/g, '$1 ').trim();
    } else if (name === 'expiryDate') {
      formattedValue = value.replace(/\D/g, '').replace(/(\d{2})(\d{2})/, '$1/$2');
    } else if (name === 'cvc') {
      formattedValue = value.replace(/\D/g, '').slice(0, 4);
    }

    setCardData((prev) => ({
      ...prev,
      [name]: formattedValue,
    }));

    if (cardErrors[name]) {
      setCardErrors((prev) => ({
        ...prev,
        [name]: '',
      }));
    }
  };

  const validateCard = () => {
    const errors = {};

    if (!cardData.cardNumber.replace(/\s/g, '')) {
      errors.cardNumber = 'Card number is required';
    } else if (cardData.cardNumber.replace(/\s/g, '').length !== 16) {
      errors.cardNumber = 'Card number must be 16 digits';
    }

    if (!cardData.expiryDate) {
      errors.expiryDate = 'Expiry date is required';
    } else if (!/^\d{2}\/\d{2}$/.test(cardData.expiryDate)) {
      errors.expiryDate = 'Format: MM/YY';
    }

    if (!cardData.cvc) {
      errors.cvc = 'CVC is required';
    } else if (cardData.cvc.length < 3) {
      errors.cvc = 'CVC must be 3-4 digits';
    }

    if (!cardData.cardholderName.trim()) {
      errors.cardholderName = 'Cardholder name is required';
    }

    return errors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    const errors = validateCard();
    if (Object.keys(errors).length > 0) {
      setCardErrors(errors);
      return;
    }

    setIsProcessing(true);
    try {
      const response = await fetch(`${API_BASE_URL}/payments/deposit`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
        body: JSON.stringify({
          eventId,
          amount: event.requiredVolunteers * event.paymentPerVolunteer,
          cardData,
        }),
      });

      if (!response.ok) throw new Error('Payment failed');

      setIsSuccessModalOpen(true);
      setTimeout(() => {
        navigate(`/events/${eventId}`);
      }, 2000);
    } catch (err) {
      setError(err.message);
    } finally {
      setIsProcessing(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
        <div className="flex items-center justify-center py-32">
          <svg className="animate-spin h-12 w-12 text-blue-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
        </div>
      </div>
    );
  }

  if (error || !event) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
        <div className="max-w-7xl mx-auto px-4 py-16 text-center">
          <h1 className="text-3xl font-bold text-white mb-4">Payment Error</h1>
          <p className="text-slate-400 mb-6">{error || 'Unable to load event details'}</p>
          <button
            onClick={() => navigate('/my-events')}
            className="px-6 py-2 bg-gradient-to-r from-blue-500 to-indigo-600 text-white font-medium rounded-lg hover:from-blue-600 hover:to-indigo-700 transition"
          >
            Back to Events
          </button>
        </div>
      </div>
    );
  }

  const totalAmount = event.requiredVolunteers * event.paymentPerVolunteer;
  const platformFee = totalAmount * 0.05; // 5% platform fee
  const finalAmount = totalAmount + platformFee;

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
      {/* Background decorative elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-20 right-0 w-96 h-96 bg-blue-500 rounded-full mix-blend-multiply filter blur-3xl opacity-10 animate-pulse"></div>
        <div className="absolute bottom-0 left-0 w-96 h-96 bg-indigo-500 rounded-full mix-blend-multiply filter blur-3xl opacity-10 animate-pulse"></div>
      </div>

      <div className="relative max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <button
          onClick={() => navigate(`/events/${eventId}`)}
          className="flex items-center gap-2 text-blue-400 hover:text-blue-300 transition mb-6"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          Back to Event
        </button>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Payment Form */}
          <div className="lg:col-span-2">
            <div className="bg-slate-800 border border-slate-700 rounded-2xl shadow-2xl p-8">
              <h1 className="text-3xl font-bold text-white mb-2">Pay Deposit</h1>
              <p className="text-slate-400 mb-8">Complete the payment to confirm your event</p>

              {/* Error Alert */}
              {error && (
                <div className="mb-6 p-4 bg-red-500/10 border border-red-500/30 rounded-lg">
                  <p className="text-red-400 text-sm font-medium">{error}</p>
                </div>
              )}

              {/* Event Summary */}
              <div className="mb-8 p-4 bg-blue-500/10 border border-blue-500/30 rounded-lg">
                <h3 className="text-white font-semibold mb-3">{event.title}</h3>
                <div className="space-y-2 text-sm text-slate-300">
                  <div className="flex justify-between">
                    <span>Date:</span>
                    <span>{formatDate(event.date, 'long')}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Volunteers:</span>
                    <span>{event.requiredVolunteers}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Payment per volunteer:</span>
                    <span>{formatCurrency(event.paymentPerVolunteer)}</span>
                  </div>
                </div>
              </div>

              {/* Payment Form */}
              <form onSubmit={handleSubmit} className="space-y-6">
                {/* Cardholder Name */}
                <div>
                  <label htmlFor="cardholderName" className="block text-sm font-medium text-slate-300 mb-2">
                    Cardholder Name
                  </label>
                  <input
                    id="cardholderName"
                    type="text"
                    name="cardholderName"
                    value={cardData.cardholderName}
                    onChange={handleCardChange}
                    placeholder="John Doe"
                    className={`w-full px-4 py-3 bg-slate-700 border rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition ${
                      cardErrors.cardholderName ? 'border-red-500' : 'border-slate-600'
                    }`}
                  />
                  {cardErrors.cardholderName && (
                    <p className="mt-1 text-sm text-red-400">{cardErrors.cardholderName}</p>
                  )}
                </div>

                {/* Card Number */}
                <div>
                  <label htmlFor="cardNumber" className="block text-sm font-medium text-slate-300 mb-2">
                    Card Number
                  </label>
                  <input
                    id="cardNumber"
                    type="text"
                    name="cardNumber"
                    value={cardData.cardNumber}
                    onChange={handleCardChange}
                    placeholder="1234 5678 9012 3456"
                    maxLength="19"
                    className={`w-full px-4 py-3 bg-slate-700 border rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition ${
                      cardErrors.cardNumber ? 'border-red-500' : 'border-slate-600'
                    }`}
                  />
                  {cardErrors.cardNumber && (
                    <p className="mt-1 text-sm text-red-400">{cardErrors.cardNumber}</p>
                  )}
                </div>

                {/* Expiry and CVC */}
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label htmlFor="expiryDate" className="block text-sm font-medium text-slate-300 mb-2">
                      Expiry Date
                    </label>
                    <input
                      id="expiryDate"
                      type="text"
                      name="expiryDate"
                      value={cardData.expiryDate}
                      onChange={handleCardChange}
                      placeholder="MM/YY"
                      maxLength="5"
                      className={`w-full px-4 py-3 bg-slate-700 border rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition ${
                        cardErrors.expiryDate ? 'border-red-500' : 'border-slate-600'
                      }`}
                    />
                    {cardErrors.expiryDate && (
                      <p className="mt-1 text-sm text-red-400">{cardErrors.expiryDate}</p>
                    )}
                  </div>

                  <div>
                    <label htmlFor="cvc" className="block text-sm font-medium text-slate-300 mb-2">
                      CVC
                    </label>
                    <input
                      id="cvc"
                      type="text"
                      name="cvc"
                      value={cardData.cvc}
                      onChange={handleCardChange}
                      placeholder="123"
                      maxLength="4"
                      className={`w-full px-4 py-3 bg-slate-700 border rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition ${
                        cardErrors.cvc ? 'border-red-500' : 'border-slate-600'
                      }`}
                    />
                    {cardErrors.cvc && (
                      <p className="mt-1 text-sm text-red-400">{cardErrors.cvc}</p>
                    )}
                  </div>
                </div>

                {/* Security Notice */}
                <div className="p-4 bg-slate-700/50 border border-slate-600 rounded-lg flex items-start gap-3">
                  <svg className="w-5 h-5 text-green-400 flex-shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M5.293 9.707a1 1 0 010-1.414l4-4a1 1 0 111.414 1.414L7.414 9l3.293 3.293a1 1 0 01-1.414 1.414l-4-4z" clipRule="evenodd" />
                  </svg>
                  <p className="text-sm text-slate-300">
                    Your payment information is secure and encrypted. We never store your full card details.
                  </p>
                </div>

                {/* Submit Button */}
                <button
                  type="submit"
                  disabled={isProcessing}
                  className="w-full py-3 px-4 bg-gradient-to-r from-blue-500 to-indigo-600 text-white font-semibold rounded-lg hover:from-blue-600 hover:to-indigo-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:ring-offset-slate-800 transition disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isProcessing ? (
                    <span className="flex items-center justify-center">
                      <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      Processing...
                    </span>
                  ) : (
                    `Pay ${formatCurrency(finalAmount)}`
                  )}
                </button>
              </form>
            </div>
          </div>

          {/* Order Summary */}
          <div className="lg:col-span-1">
            <div className="bg-slate-800 border border-slate-700 rounded-2xl shadow-2xl p-6 sticky top-24">
              <h3 className="text-xl font-bold text-white mb-6">Order Summary</h3>

              <div className="space-y-4 mb-6 pb-6 border-b border-slate-700">
                <div className="flex justify-between text-slate-300">
                  <span>Subtotal</span>
                  <span>{formatCurrency(totalAmount)}</span>
                </div>
                <div className="flex justify-between text-slate-300">
                  <span>Platform Fee (5%)</span>
                  <span>{formatCurrency(platformFee)}</span>
                </div>
              </div>

              <div className="flex justify-between items-center mb-6">
                <span className="text-lg font-semibold text-white">Total</span>
                <span className="text-2xl font-bold text-blue-400">{formatCurrency(finalAmount)}</span>
              </div>

              {/* Info Box */}
              <div className="p-4 bg-slate-700/50 border border-slate-600 rounded-lg text-sm text-slate-300 space-y-2">
                <p>✓ Secure payment processing</p>
                <p>✓ Instant confirmation</p>
                <p>✓ Refund eligible</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Success Modal */}
      <Modal
        isOpen={isSuccessModalOpen}
        onClose={() => setIsSuccessModalOpen(false)}
        title="Payment Successful"
        size="md"
      >
        <div className="text-center py-4">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-green-500/20 border border-green-500/30 rounded-full mb-4">
            <svg className="w-8 h-8 text-green-400" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
            </svg>
          </div>
          <h3 className="text-xl font-bold text-white mb-2">Payment Confirmed</h3>
          <p className="text-slate-400 mb-4">Your deposit has been processed successfully.</p>
          <p className="text-sm text-slate-500">Redirecting to event details...</p>
        </div>
      </Modal>
    </div>
  );
};


export default PaymentDeposit;