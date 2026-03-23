// Get user role from JWT token
export const getUserRoleFromToken = (token) => {
  if (!token) return null;
  
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    
    const decoded = JSON.parse(atob(parts[1]));
    return decoded.role || null;
  } catch (err) {
    console.error('Error decoding token:', err);
    return null;
  }
};

// Get user ID from JWT token
export const getUserIdFromToken = (token) => {
  if (!token) return null;
  
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    
    const decoded = JSON.parse(atob(parts[1]));
    return decoded.sub || null;
  } catch (err) {
    console.error('Error decoding token:', err);
    return null;
  }
};

// Check if token is expired
export const isTokenExpired = (token) => {
  if (!token) return true;
  
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return true;
    
    const decoded = JSON.parse(atob(parts[1]));
    const expirationTime = decoded.exp * 1000; // Convert to milliseconds
    return Date.now() >= expirationTime;
  } catch (err) {
    console.error('Error checking token expiration:', err);
    return true;
  }
};

// Get token from localStorage
export const getToken = () => {
  return localStorage.getItem('token');
};

// Set token in localStorage
export const setToken = (token) => {
  localStorage.setItem('token', token);
};

// Remove token from localStorage
export const removeToken = () => {
  localStorage.removeItem('token');
};

// Get user from localStorage
export const getUser = () => {
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user) : null;
};

// Set user in localStorage
export const setUser = (user) => {
  localStorage.setItem('user', JSON.stringify(user));
};

// Remove user from localStorage
export const removeUser = () => {
  localStorage.removeItem('user');
};

// Clear all auth data
export const clearAuthData = () => {
  removeToken();
  removeUser();
};

// Build query string from object
export const buildQueryString = (params) => {
  const queryParams = new URLSearchParams();
  
  Object.keys(params).forEach((key) => {
    const value = params[key];
    if (value !== null && value !== undefined && value !== '') {
      queryParams.append(key, value);
    }
  });
  
  return queryParams.toString();
};

// Parse query string to object
export const parseQueryString = (queryString) => {
  const params = new URLSearchParams(queryString);
  const obj = {};
  
  params.forEach((value, key) => {
    obj[key] = value;
  });
  
  return obj;
};

// Debounce function
export const debounce = (func, delay) => {
  let timeoutId;
  
  return (...args) => {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => {
      func(...args);
    }, delay);
  };
};

// Throttle function
export const throttle = (func, limit) => {
  let inThrottle;
  
  return (...args) => {
    if (!inThrottle) {
      func(...args);
      inThrottle = true;
      setTimeout(() => {
        inThrottle = false;
      }, limit);
    }
  };
};

// Deep clone object
export const deepClone = (obj) => {
  return JSON.parse(JSON.stringify(obj));
};

// Merge objects
export const mergeObjects = (target, source) => {
  return { ...target, ...source };
};

// Check if object is empty
export const isEmptyObject = (obj) => {
  return Object.keys(obj).length === 0;
};

// Get object value by path
export const getValueByPath = (obj, path) => {
  return path.split('.').reduce((current, prop) => current?.[prop], obj);
};

// Set object value by path
export const setValueByPath = (obj, path, value) => {
  const keys = path.split('.');
  let current = obj;
  
  for (let i = 0; i < keys.length - 1; i++) {
    const key = keys[i];
    if (!(key in current)) {
      current[key] = {};
    }
    current = current[key];
  }
  
  current[keys[keys.length - 1]] = value;
  return obj;
};

// Generate unique ID
export const generateId = () => {
  return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
};

// Check if value is in array
export const isInArray = (value, array) => {
  return array.includes(value);
};

// Filter array by property
export const filterByProperty = (array, property, value) => {
  return array.filter((item) => item[property] === value);
};

// Sort array by property
export const sortByProperty = (array, property, ascending = true) => {
  return [...array].sort((a, b) => {
    if (a[property] < b[property]) return ascending ? -1 : 1;
    if (a[property] > b[property]) return ascending ? 1 : -1;
    return 0;
  });
};

// Group array by property
export const groupByProperty = (array, property) => {
  return array.reduce((groups, item) => {
    const key = item[property];
    if (!groups[key]) {
      groups[key] = [];
    }
    groups[key].push(item);
    return groups;
  }, {});
};

// Flatten nested array
export const flattenArray = (array) => {
  return array.reduce((flat, item) => {
    return flat.concat(Array.isArray(item) ? flattenArray(item) : item);
  }, []);
};

// Remove duplicates from array
export const removeDuplicates = (array, property = null) => {
  if (!property) {
    return [...new Set(array)];
  }
  
  const seen = new Set();
  return array.filter((item) => {
    const value = item[property];
    if (seen.has(value)) {
      return false;
    }
    seen.add(value);
    return true;
  });
};

// Retry function with exponential backoff
export const retryWithBackoff = async (func, maxRetries = 3, delay = 1000) => {
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await func();
    } catch (err) {
      if (i === maxRetries - 1) throw err;
      await new Promise((resolve) => setTimeout(resolve, delay * Math.pow(2, i)));
    }
  }
};

// Sleep function
export const sleep = (ms) => {
  return new Promise((resolve) => setTimeout(resolve, ms));
};

// Check if running on mobile
export const isMobile = () => {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(
    navigator.userAgent
  );
};

// Check if running on touch device
export const isTouchDevice = () => {
  return (
    (typeof window !== 'undefined' &&
      ('ontouchstart' in window ||
        (window.DocumentTouch && typeof document !== 'undefined' && document instanceof window.DocumentTouch))) ||
    false
  );
};

// Scroll to element
export const scrollToElement = (elementId, behavior = 'smooth') => {
  const element = document.getElementById(elementId);
  if (element) {
    element.scrollIntoView({ behavior });
  }
};

// Copy to clipboard
export const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch (err) {
    console.error('Failed to copy to clipboard:', err);
    return false;
  }
};

// Download file
export const downloadFile = (url, filename) => {
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};
