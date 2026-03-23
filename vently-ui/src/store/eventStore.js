import { create } from 'zustand';

export const useEventStore = create((set, get) => ({
  events: [],
  filteredEvents: [],
  selectedEvent: null,
  loading: false,
  error: null,
  filters: {
    dateRange: { start: null, end: null },
    location: '',
    paymentRange: { min: 0, max: 10000 },
    category: '',
    searchQuery: '',
  },
  pagination: {
    page: 1,
    pageSize: 12,
    total: 0,
  },

  // Fetch events with filters
  fetchEvents: async (filters = {}, page = 1) => {
    set({ loading: true, error: null });
    try {
      const params = new URLSearchParams();
      params.append('page', page - 1);
      params.append('size', get().pagination.pageSize);

      if (filters.dateStart) params.append('dateStart', filters.dateStart);
      if (filters.dateEnd) params.append('dateEnd', filters.dateEnd);
      if (filters.location) params.append('location', filters.location);
      if (filters.minPayment) params.append('minPayment', filters.minPayment);
      if (filters.maxPayment) params.append('maxPayment', filters.maxPayment);
      if (filters.category) params.append('category', filters.category);
      if (filters.search) params.append('search', filters.search);
      if (filters.sort) params.append('sort', filters.sort);

      const headers = {
        'Content-Type': 'application/json',
      };
      
      const token = localStorage.getItem('token');
      if (token) {
        headers.Authorization = `Bearer ${token}`;
      }

      const response = await fetch(`/api/v1/events?${params}`, { headers });

      if (!response.ok) throw new Error('Failed to fetch events');

      const data = await response.json();
      
      set({
        events: data.content || [],
        filteredEvents: data.content || [],
        pagination: {
          page,
          pageSize: get().pagination.pageSize,
          total: data.totalElements || 0,
        },
        loading: false,
      });
    } catch (err) {
      set({ error: err.message, loading: false });
    }
  },

  // Fetch single event
  fetchEventById: async (eventId) => {
    set({ loading: true, error: null });
    try {
      if (!eventId) {
        throw new Error('Event ID is required');
      }

      const response = await fetch(`/api/v1/events/${eventId}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `Failed to fetch event (${response.status})`);
      }

      const data = await response.json();
      
      if (!data || !data.id) {
        throw new Error('Invalid event data received from server');
      }

      set({ selectedEvent: data, loading: false });
      return data;
    } catch (err) {
      console.error('Error fetching event:', err);
      set({ error: err.message, loading: false });
      throw err;
    }
  },

  // Update filters
  setFilters: (newFilters) => {
    set((state) => ({
      filters: { ...state.filters, ...newFilters },
      pagination: { ...state.pagination, page: 1 },
    }));
  },

  // Reset filters
  resetFilters: () => {
    set({
      filters: {
        dateRange: { start: null, end: null },
        location: '',
        paymentRange: { min: 0, max: 10000 },
        category: '',
        searchQuery: '',
      },
      pagination: { page: 1, pageSize: 12, total: 0 },
    });
  },

  // Set pagination
  setPagination: (page) => {
    set((state) => ({
      pagination: { ...state.pagination, page },
    }));
  },

  // Clear selected event
  clearSelectedEvent: () => {
    set({ selectedEvent: null });
  },

  // Clear error
  clearError: () => {
    set({ error: null });
  },
}));
