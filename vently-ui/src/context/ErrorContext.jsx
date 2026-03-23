import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';

const ErrorContext = createContext(null);

export function ErrorProvider({ children }) {
  const [error, setError] = useState(null); // { message, traceId }

  const showError = useCallback((message, traceId) => {
    setError({ message, traceId });
  }, []);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  // Listen for api:error events dispatched by the axios interceptor
  useEffect(() => {
    const handler = (e) => {
      showError(e.detail.message, e.detail.traceId);
    };
    window.addEventListener('api:error', handler);
    return () => window.removeEventListener('api:error', handler);
  }, [showError]);

  return (
    <ErrorContext.Provider value={{ error, showError, clearError }}>
      {children}
    </ErrorContext.Provider>
  );
}

export function useError() {
  return useContext(ErrorContext);
}
