import axios from './axios';

const API_BASE = '/api/v1/disputes';

export const disputeApi = {
  // Create a dispute
  createDispute: async (disputeData) => {
    const response = await axios.post(API_BASE, disputeData);
    return response.data;
  },

  // Upload evidence for a dispute
  uploadEvidence: async (disputeId, files) => {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append('files', file);
    });

    const response = await axios.post(`${API_BASE}/${disputeId}/evidence`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  // Get user's disputes
  getMyDisputes: async (page = 0, size = 10, status = null) => {
    const response = await axios.get(`${API_BASE}/my-disputes`, {
      params: { page, size, ...(status && { status }) },
    });
    return response.data;
  },

  // Get dispute details
  getDisputeDetails: async (disputeId) => {
    const response = await axios.get(`${API_BASE}/${disputeId}`);
    return response.data;
  },

  // Get all disputes (admin only)
  getAllDisputes: async (page = 0, size = 10, status = null) => {
    const response = await axios.get(API_BASE, {
      params: { page, size, ...(status && { status }) },
    });
    return response.data;
  },
};
