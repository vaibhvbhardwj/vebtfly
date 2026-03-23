import axios from './axios';

const API_BASE = '/api/v1/auth';

export const authApi = {
  // Login
  login: async (email, password) => {
    const response = await axios.post(`${API_BASE}/authenticate`, { email, password });
    return response.data;
  },

  // Register
  register: async (email, password, role, name) => {
    const response = await axios.post(`${API_BASE}/register`, {
      email,
      password,
      role,
      name,
    });
    return response.data;
  },

  // Verify email
  verifyEmail: async (token) => {
    const response = await axios.post(`${API_BASE}/verify-email`, { token });
    return response.data;
  },

  // Resend verification email
  resendVerificationEmail: async (email) => {
    const response = await axios.post(`${API_BASE}/resend-verification`, { email });
    return response.data;
  },

  // Forgot password
  forgotPassword: async (email) => {
    const response = await axios.post(`${API_BASE}/forgot-password`, { email });
    return response.data;
  },

  // Reset password
  resetPassword: async (token, newPassword) => {
    const response = await axios.post(`${API_BASE}/reset-password`, { token, newPassword });
    return response.data;
  },

  // Refresh token
  refreshToken: async () => {
    const response = await axios.post(`${API_BASE}/refresh-token`);
    return response.data;
  },
};
