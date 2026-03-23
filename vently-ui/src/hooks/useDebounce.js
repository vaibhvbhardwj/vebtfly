import { useState, useEffect } from 'react';

export const useDebounce = (value, delay = 500) => {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
};

// Hook for debounced callback
export const useDebouncedCallback = (callback, delay = 500) => {
  const [timeoutId, setTimeoutId] = useState(null);

  const debouncedCallback = (...args) => {
    if (timeoutId) {
      clearTimeout(timeoutId);
    }

    const newTimeoutId = setTimeout(() => {
      callback(...args);
    }, delay);

    setTimeoutId(newTimeoutId);
  };

  return debouncedCallback;
};

// Hook for throttled callback
export const useThrottledCallback = (callback, delay = 500) => {
  const [lastCallTime, setLastCallTime] = useState(0);

  const throttledCallback = (...args) => {
    const now = Date.now();
    if (now - lastCallTime >= delay) {
      callback(...args);
      setLastCallTime(now);
    }
  };

  return throttledCallback;
};
