import axios from 'axios';
import { useAuthStore } from '../store/authStore';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

// Create axios instance
const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  timeout: 30000,
});

// Request interceptor - Add JWT token to all requests
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token') || useAuthStore.getState().token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // Log request for debugging
    if (import.meta.env.DEV) {
      console.log(`[API Request] ${config.method.toUpperCase()} ${config.url}`);
    }
    
    return config;
  },
  (error) => {
    console.error('[API Request Error]', error);
    return Promise.reject(error);
  }
);

// Response interceptor - Handle errors globally
axiosInstance.interceptors.response.use(
  (response) => {
    // Log response for debugging
    if (import.meta.env.DEV) {
      console.log(`[API Response] ${response.status} ${response.config.url}`);
    }
    return response;
  },
  (error) => {
    const { response, message } = error;

    // Handle 401 Unauthorized - redirect to login
    if (response?.status === 401) {
      console.error('[API Error] Unauthorized - redirecting to login');
      useAuthStore.getState().logout();
      localStorage.removeItem('token');
      window.location.href = '/login';
      return Promise.reject(new Error('Session expired. Please login again.'));
    }

    // Handle 403 Forbidden - could be expired JWT or no permission
    if (response?.status === 403) {
      const token = localStorage.getItem('token');
      if (!token) {
        // No token at all - redirect to login
        useAuthStore.getState().logout();
        window.location.href = '/login';
        return Promise.reject(new Error('Session expired. Please login again.'));
      }
      // Has token but 403 - check if it's an expired JWT
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        if (payload.exp && payload.exp * 1000 < Date.now()) {
          // Token is expired - logout and redirect
          useAuthStore.getState().logout();
          localStorage.removeItem('token');
          window.location.href = '/login';
          return Promise.reject(new Error('Session expired. Please login again.'));
        }
      } catch (e) {
        // Can't parse token - treat as expired
        useAuthStore.getState().logout();
        localStorage.removeItem('token');
        window.location.href = '/login';
        return Promise.reject(new Error('Session expired. Please login again.'));
      }
      console.error('[API Error] Forbidden - access denied');
      return Promise.reject(new Error('You do not have permission to perform this action.'));
    }

    // Handle 404 Not Found
    if (response?.status === 404) {
      console.error('[API Error] Not Found');
      return Promise.reject(new Error('The requested resource was not found.'));
    }

    // Handle 400 Bad Request
    if (response?.status === 400) {
      const errorMessage = response.data?.message || 'Invalid request. Please check your input.';
      console.error('[API Error] Bad Request:', errorMessage);
      return Promise.reject(new Error(errorMessage));
    }

    // Handle 500 Server Error - emit event with traceId for global popup
    if (response?.status === 500) {
      const traceId = response.data?.traceId;
      const msg = response.data?.message || 'Server error. Please try again later.';
      console.error('[API Error] Server Error', traceId);
      window.dispatchEvent(new CustomEvent('api:error', { detail: { message: msg, traceId } }));
      return Promise.reject(new Error(msg));
    }

    // Handle network errors
    if (!response) {
      console.error('[API Error] Network Error:', message);
      return Promise.reject(new Error('Network error. Please check your connection.'));
    }

    // Generic error handling
    const errorMessage = response.data?.message || message || 'An error occurred';
    const traceId = response?.data?.traceId;
    if (traceId) {
      window.dispatchEvent(new CustomEvent('api:error', { detail: { message: errorMessage, traceId } }));
    }
    console.error('[API Error]', errorMessage);
    return Promise.reject(new Error(errorMessage));
  }
);

export default axiosInstance;
