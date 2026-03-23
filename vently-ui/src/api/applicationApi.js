import axios from './axios';

const API_BASE = '/api/v1/applications';

export const applicationApi = {
  // Submit application
  submitApplication: async (eventId) => {
    const response = await axios.post(API_BASE, { eventId });
    return response.data;
  },

  // Withdraw application
  withdrawApplication: async (applicationId) => {
    const response = await axios.delete(`${API_BASE}/${applicationId}`);
    return response.data;
  },

  // Accept application (organizer)
  acceptApplication: async (applicationId) => {
    const response = await axios.post(`${API_BASE}/${applicationId}/accept`);
    return response.data;
  },

  // Reject application (organizer)
  rejectApplication: async (applicationId) => {
    const response = await axios.post(`${API_BASE}/${applicationId}/reject`);
    return response.data;
  },

  // Confirm application (volunteer)
  confirmApplication: async (applicationId) => {
    const response = await axios.post(`${API_BASE}/${applicationId}/confirm`);
    return response.data;
  },

  // Decline application (volunteer)
  declineApplication: async (applicationId) => {
    const response = await axios.post(`${API_BASE}/${applicationId}/decline`);
    return response.data;
  },

  // Get my applications (volunteer)
  getMyApplications: async (page = 1, size = 12, status = null) => {
    const params = {
      page: page - 1,
      size,
    };
    if (status) {
      params.status = status;
    }
    const response = await axios.get(`${API_BASE}/my-applications`, { params });
    return response.data;
  },

  // Get application details
  getApplicationDetails: async (applicationId) => {
    const response = await axios.get(`${API_BASE}/${applicationId}`);
    return response.data;
  },
};
