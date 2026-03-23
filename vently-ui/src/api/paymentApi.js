import axios from './axios';

const API_BASE = '/api/v1/payments';

export const paymentApi = {
  // Create deposit intent
  createDepositIntent: async (eventId) => {
    const response = await axios.post(`${API_BASE}/deposit`, { eventId });
    return response.data;
  },

  // Confirm deposit payment
  confirmDeposit: async (paymentIntentId) => {
    const response = await axios.post(`${API_BASE}/confirm`, { paymentIntentId });
    return response.data;
  },

  // Get payment history
  getPaymentHistory: async (page = 1, size = 20, filters = {}) => {
    const params = new URLSearchParams({
      page: page - 1,
      size,
      ...filters,
    });
    const response = await axios.get(`${API_BASE}/history?${params}`);
    return response.data;
  },

  // Get payout history (volunteer)
  getPayoutHistory: async (page = 1, size = 20) => {
    const response = await axios.get(`${API_BASE}/payouts`, {
      params: {
        page: page - 1,
        size,
      },
    });
    return response.data;
  },

  // Get payment details
  getPaymentDetails: async (paymentId) => {
    const response = await axios.get(`${API_BASE}/${paymentId}`);
    return response.data;
  },
};
