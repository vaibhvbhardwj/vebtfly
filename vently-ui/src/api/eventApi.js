import axios from './axios';

const API_BASE = '/api/v1/events';

export const eventApi = {
  // Create event
  createEvent: async (eventData) => {
    const response = await axios.post(API_BASE, eventData);
    return response.data;
  },

  // Update event
  updateEvent: async (eventId, eventData) => {
    const response = await axios.put(`${API_BASE}/${eventId}`, eventData);
    return response.data;
  },

  // Get event by ID
  getEventById: async (eventId) => {
    const response = await axios.get(`${API_BASE}/${eventId}`);
    return response.data;
  },

  // Get all published events with filters
  getPublishedEvents: async (page = 1, size = 12, filters = {}) => {
    const params = new URLSearchParams({
      page: page - 1,
      size,
      ...filters,
    });
    const response = await axios.get(`${API_BASE}?${params}`);
    return response.data;
  },

  // Get my events (organizer)
  getMyEvents: async (page = 1, size = 12) => {
    const response = await axios.get(`${API_BASE}/my-events`, {
      params: {
        page: page - 1,
        size,
      },
    });
    return response.data;
  },

  // Publish event
  publishEvent: async (eventId) => {
    const response = await axios.post(`${API_BASE}/${eventId}/publish`);
    return response.data;
  },

  // Cancel event
  cancelEvent: async (eventId, reason) => {
    const response = await axios.post(`${API_BASE}/${eventId}/cancel`, { reason });
    return response.data;
  },

  // Delete event (draft only)
  deleteEvent: async (eventId) => {
    const response = await axios.delete(`${API_BASE}/${eventId}`);
    return response.data;
  },

  // Search events
  searchEvents: async (query, page = 1, size = 12) => {
    const response = await axios.get(`${API_BASE}/search`, {
      params: {
        q: query,
        page: page - 1,
        size,
      },
    });
    return response.data;
  },

  // Get event applications
  getEventApplications: async (eventId, page = 1, size = 20) => {
    const response = await axios.get(`${API_BASE}/${eventId}/applications`, {
      params: {
        page: page - 1,
        size,
      },
    });
    return response.data;
  },
};
