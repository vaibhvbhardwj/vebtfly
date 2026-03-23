import axios from './axios';

const API_BASE = '/api/v1/subscriptions';

export const subscriptionApi = {
  // Get current subscription
  getCurrentSubscription: async () => {
    const response = await axios.get(`${API_BASE}/current`);
    return response.data;
  },

  // Upgrade to premium
  upgradeSubscription: async (tier) => {
    const response = await axios.post(`${API_BASE}/upgrade`, { tier });
    return response.data;
  },

  // Cancel subscription
  cancelSubscription: async () => {
    const response = await axios.post(`${API_BASE}/cancel`);
    return response.data;
  },

  // Get usage statistics
  getUsageStatistics: async () => {
    const response = await axios.get(`${API_BASE}/usage`);
    return response.data;
  },
};
