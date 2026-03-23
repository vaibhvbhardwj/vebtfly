import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export const useAuthStore = create(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      loading: false,
      error: null,

      // Login action
      login: async (email, password) => {
        set({ loading: true, error: null });
        try {
          const response = await fetch('/api/v1/auth/authenticate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password }),
          });

          if (!response.ok) {
            throw new Error('Login failed');
          }

          const data = await response.json();
          // API returns flat object: { token, userId, email, role, fullName, ... }
          localStorage.setItem('token', data.token);
          localStorage.setItem('user', JSON.stringify(data));
          set({
            user: data,
            token: data.token,
            isAuthenticated: true,
            loading: false,
          });
          return data;
        } catch (err) {
          set({ error: err.message, loading: false });
          throw err;
        }
      },

      // Register action
      register: async (email, password, role, name) => {
        set({ loading: true, error: null });
        try {
          const response = await fetch('/api/v1/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password, role, name }),
          });

          if (!response.ok) {
            throw new Error('Registration failed');
          }

          const data = await response.json();
          // API returns flat object: { token, userId, email, role, fullName, ... }
          localStorage.setItem('token', data.token);
          localStorage.setItem('user', JSON.stringify(data));
          set({
            user: data,
            token: data.token,
            isAuthenticated: true,
            loading: false,
          });
          return data;
        } catch (err) {
          set({ error: err.message, loading: false });
          throw err;
        }
      },

      // Logout action
      logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        set({
          user: null,
          token: null,
          isAuthenticated: false,
          error: null,
        });
      },

      // Update user action
      updateUser: (userData) => {
        set((state) => ({
          user: { ...state.user, ...userData },
        }));
      },

      // Set token (for hydration)
      setToken: (token) => {
        set({ token });
      },

      // Set user (for hydration)
      setUser: (user) => {
        set({ user, isAuthenticated: !!user });
      },

      // Clear error
      clearError: () => {
        set({ error: null });
      },

      // Hydrate from localStorage
      hydrate: () => {
        const storedUser = localStorage.getItem('user');
        const storedToken = localStorage.getItem('token');
        if (storedUser && storedToken) {
          try {
            const user = JSON.parse(storedUser);
            set({
              user,
              token: storedToken,
              isAuthenticated: true,
            });
          } catch (e) {
            console.error('Failed to hydrate auth store:', e);
          }
        }
      },
    }),
    {
      name: 'auth-store',
      version: 1,
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);
