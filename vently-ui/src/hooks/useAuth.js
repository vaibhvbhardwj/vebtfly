import { useAuthStore } from '../store/authStore';
import { useCallback, useEffect, useState } from 'react';

export const useAuth = () => {
  const [isReady, setIsReady] = useState(false);
  const {
    user,
    token,
    isAuthenticated,
    loading,
    error,
    login,
    register,
    logout,
    updateUser,
    setToken,
    setUser,
    clearError,
    hydrate,
  } = useAuthStore();

  // Hydrate from localStorage on mount
  useEffect(() => {
    hydrate();
    setIsReady(true);
  }, [hydrate]);

  const handleLogin = useCallback(
    async (email, password) => {
      return login(email, password);
    },
    [login]
  );

  const handleRegister = useCallback(
    async (email, password, role, name) => {
      return register(email, password, role, name);
    },
    [register]
  );

  const handleLogout = useCallback(() => {
    logout();
    localStorage.removeItem('token');
  }, [logout]);

  const handleUpdateUser = useCallback(
    (userData) => {
      updateUser(userData);
    },
    [updateUser]
  );

  const handleClearError = useCallback(() => {
    clearError();
  }, [clearError]);

  return {
    user,
    token,
    isAuthenticated: isReady && isAuthenticated,
    loading: !isReady || loading,
    error,
    login: handleLogin,
    register: handleRegister,
    logout: handleLogout,
    updateUser: handleUpdateUser,
    setToken,
    setUser,
    clearError: handleClearError,
  };
};
