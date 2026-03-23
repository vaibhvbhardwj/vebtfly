// API Configuration
export const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

// User Roles
export const USER_ROLES = {
  VOLUNTEER: 'VOLUNTEER',
  ORGANIZER: 'ORGANIZER',
  ADMIN: 'ADMIN',
};

// Event Status
export const EVENT_STATUS = {
  DRAFT: 'DRAFT',
  PUBLISHED: 'PUBLISHED',
  DEPOSIT_PAID: 'DEPOSIT_PAID',
  IN_PROGRESS: 'IN_PROGRESS',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
};

// Application Status
export const APPLICATION_STATUS = {
  PENDING: 'PENDING',
  ACCEPTED: 'ACCEPTED',
  CONFIRMED: 'CONFIRMED',
  DECLINED: 'DECLINED',
  REJECTED: 'REJECTED',
  CANCELLED: 'CANCELLED',
};

// Payment Status
export const PAYMENT_STATUS = {
  PENDING: 'PENDING',
  COMPLETED: 'COMPLETED',
  FAILED: 'FAILED',
  REFUNDED: 'REFUNDED',
};

// Dispute Status
export const DISPUTE_STATUS = {
  OPEN: 'OPEN',
  UNDER_REVIEW: 'UNDER_REVIEW',
  RESOLVED: 'RESOLVED',
  CLOSED: 'CLOSED',
};

// Account Status
export const ACCOUNT_STATUS = {
  ACTIVE: 'ACTIVE',
  SUSPENDED: 'SUSPENDED',
  BANNED: 'BANNED',
};

// Subscription Tiers
export const SUBSCRIPTION_TIERS = {
  FREE: 'FREE',
  PREMIUM: 'PREMIUM',
};

// Tier Limits
export const TIER_LIMITS = {
  FREE: {
    maxEvents: 3,
    maxApplications: 5,
  },
  PREMIUM: {
    maxEvents: Infinity,
    maxApplications: Infinity,
  },
};

// Notification Types
export const NOTIFICATION_TYPES = {
  APPLICATION_SUBMITTED: 'APPLICATION_SUBMITTED',
  APPLICATION_ACCEPTED: 'APPLICATION_ACCEPTED',
  APPLICATION_REJECTED: 'APPLICATION_REJECTED',
  APPLICATION_CONFIRMED: 'APPLICATION_CONFIRMED',
  APPLICATION_DECLINED: 'APPLICATION_DECLINED',
  PAYMENT_RECEIVED: 'PAYMENT_RECEIVED',
  PAYMENT_RELEASED: 'PAYMENT_RELEASED',
  EVENT_CANCELLED: 'EVENT_CANCELLED',
  RATING_RECEIVED: 'RATING_RECEIVED',
  DISPUTE_CREATED: 'DISPUTE_CREATED',
  DISPUTE_RESOLVED: 'DISPUTE_RESOLVED',
};

// Date Formats
export const DATE_FORMATS = {
  DISPLAY: 'MMM dd, yyyy',
  DISPLAY_TIME: 'MMM dd, yyyy HH:mm',
  INPUT: 'yyyy-MM-dd',
  ISO: "yyyy-MM-dd'T'HH:mm:ss.SSSxxx",
};

// Currency
export const CURRENCY = {
  SYMBOL: '₹',
  CODE: 'INR',
};

// File Upload Constraints
export const FILE_CONSTRAINTS = {
  PROFILE_PICTURE: {
    maxSize: 5 * 1024 * 1024, // 5MB
    allowedTypes: ['image/jpeg', 'image/png'],
    allowedExtensions: ['.jpg', '.jpeg', '.png'],
  },
  EVIDENCE: {
    maxSize: 10 * 1024 * 1024, // 10MB
    allowedTypes: ['image/jpeg', 'image/png', 'application/pdf'],
    allowedExtensions: ['.jpg', '.jpeg', '.png', '.pdf'],
  },
  EXCEL: {
    maxSize: 5 * 1024 * 1024, // 5MB
    allowedTypes: [
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      'application/vnd.ms-excel',
    ],
    allowedExtensions: ['.xlsx', '.xls'],
  },
};

// Pagination
export const PAGINATION = {
  DEFAULT_PAGE_SIZE: 12,
  ADMIN_PAGE_SIZE: 20,
};

// Rating
export const RATING = {
  MIN: 1,
  MAX: 5,
  RATING_WINDOW_DAYS: 7,
};

// No-Show Penalties
export const NO_SHOW_PENALTIES = {
  SUSPENSION_THRESHOLD: 3,
  SUSPENSION_DURATION_DAYS: 30,
  BAN_THRESHOLD: 5,
};

// Confirmation Window
export const CONFIRMATION_WINDOW_HOURS = 48;

// Rate Limiting
export const RATE_LIMITS = {
  AUTH_ATTEMPTS_PER_MINUTE: 5,
};
