// Email validation
export const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

// Password validation
export const validatePassword = (password) => {
  // At least 8 characters, 1 uppercase, 1 lowercase, 1 number, 1 special char
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
  return passwordRegex.test(password);
};

// Get password strength
export const getPasswordStrength = (password) => {
  if (!password) return 'weak';
  
  let strength = 0;
  if (password.length >= 8) strength++;
  if (password.length >= 12) strength++;
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength++;
  if (/\d/.test(password)) strength++;
  if (/[@$!%*?&]/.test(password)) strength++;

  if (strength <= 1) return 'weak';
  if (strength <= 2) return 'fair';
  if (strength <= 3) return 'good';
  if (strength <= 4) return 'strong';
  return 'very-strong';
};

// Phone validation
export const validatePhone = (phone) => {
  const phoneRegex = /^[\d\s\-+()]{10,}$/;
  return phoneRegex.test(phone.replace(/\s/g, ''));
};

// Date validation
export const validateDate = (date) => {
  return date instanceof Date && !isNaN(date);
};

// Date range validation
export const validateDateRange = (startDate, endDate) => {
  if (!validateDate(startDate) || !validateDate(endDate)) {
    return false;
  }
  return startDate <= endDate;
};

// URL validation
export const validateUrl = (url) => {
  try {
    new URL(url);
    return true;
  } catch {
    return false;
  }
};

// File type validation
export const validateFileType = (file, allowedTypes) => {
  return allowedTypes.includes(file.type);
};

// File size validation
export const validateFileSize = (file, maxSize) => {
  return file.size <= maxSize;
};

// File validation (type and size)
export const validateFile = (file, allowedTypes, maxSize) => {
  if (!validateFileType(file, allowedTypes)) {
    return { valid: false, error: 'Invalid file type' };
  }
  if (!validateFileSize(file, maxSize)) {
    return { valid: false, error: 'File size exceeds limit' };
  }
  return { valid: true };
};

// Rating validation
export const validateRating = (rating) => {
  return Number.isInteger(rating) && rating >= 1 && rating <= 5;
};

// Required field validation
export const validateRequired = (value) => {
  if (typeof value === 'string') {
    return value.trim().length > 0;
  }
  return value !== null && value !== undefined;
};

// Min length validation
export const validateMinLength = (value, minLength) => {
  return value.length >= minLength;
};

// Max length validation
export const validateMaxLength = (value, maxLength) => {
  return value.length <= maxLength;
};

// Number range validation
export const validateNumberRange = (value, min, max) => {
  const num = Number(value);
  return !isNaN(num) && num >= min && num <= max;
};

// Form validation helper
export const validateForm = (formData, rules) => {
  const errors = {};

  Object.keys(rules).forEach((field) => {
    const rule = rules[field];
    const value = formData[field];

    if (rule.required && !validateRequired(value)) {
      errors[field] = `${rule.label || field} is required`;
      return;
    }

    if (value && rule.type === 'email' && !validateEmail(value)) {
      errors[field] = 'Invalid email format';
      return;
    }

    if (value && rule.type === 'password' && !validatePassword(value)) {
      errors[field] = 'Password must be at least 8 characters with uppercase, lowercase, number, and special character';
      return;
    }

    if (value && rule.type === 'phone' && !validatePhone(value)) {
      errors[field] = 'Invalid phone number';
      return;
    }

    if (value && rule.minLength && !validateMinLength(value, rule.minLength)) {
      errors[field] = `${rule.label || field} must be at least ${rule.minLength} characters`;
      return;
    }

    if (value && rule.maxLength && !validateMaxLength(value, rule.maxLength)) {
      errors[field] = `${rule.label || field} must not exceed ${rule.maxLength} characters`;
      return;
    }

    if (value && rule.min !== undefined && rule.max !== undefined) {
      if (!validateNumberRange(value, rule.min, rule.max)) {
        errors[field] = `${rule.label || field} must be between ${rule.min} and ${rule.max}`;
      }
    }

    if (rule.custom && !rule.custom(value)) {
      errors[field] = rule.customError || `${rule.label || field} is invalid`;
    }
  });

  return errors;
};
