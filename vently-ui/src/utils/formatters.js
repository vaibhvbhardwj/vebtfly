import { CURRENCY, EVENT_STATUS, APPLICATION_STATUS, DISPUTE_STATUS } from './constants';

// Format currency
export const formatCurrency = (amount) => {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: CURRENCY.CODE,
    maximumFractionDigits: 0,
  }).format(amount);
};

// Format date
export const formatDate = (date, format = 'short') => {
  if (!date) return '';
  
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  
  if (format === 'short') {
    return dateObj.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  }
  
  if (format === 'long') {
    return dateObj.toLocaleDateString('en-US', {
      weekday: 'long',
      month: 'long',
      day: 'numeric',
      year: 'numeric',
    });
  }
  
  if (format === 'time') {
    return dateObj.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
    });
  }
  
  if (format === 'datetime') {
    return dateObj.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }
  
  return dateObj.toLocaleDateString('en-US');
};

// Format time
export const formatTime = (date) => {
  if (!date) return '';
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  return dateObj.toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit',
  });
};

// Format relative time (e.g., "2 hours ago")
export const formatRelativeTime = (date) => {
  if (!date) return '';
  
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  const now = new Date();
  const diffMs = now - dateObj;
  const diffSecs = Math.floor(diffMs / 1000);
  const diffMins = Math.floor(diffSecs / 60);
  const diffHours = Math.floor(diffMins / 60);
  const diffDays = Math.floor(diffHours / 24);

  if (diffSecs < 60) return 'just now';
  if (diffMins < 60) return `${diffMins} minute${diffMins > 1 ? 's' : ''} ago`;
  if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
  if (diffDays < 7) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
  
  return formatDate(dateObj, 'short');
};

// Format event status
export const formatEventStatus = (status) => {
  const statusMap = {
    [EVENT_STATUS.DRAFT]: 'Draft',
    [EVENT_STATUS.PUBLISHED]: 'Published',
    [EVENT_STATUS.DEPOSIT_PAID]: 'Deposit Paid',
    [EVENT_STATUS.IN_PROGRESS]: 'In Progress',
    [EVENT_STATUS.COMPLETED]: 'Completed',
    [EVENT_STATUS.CANCELLED]: 'Cancelled',
  };
  return statusMap[status] || status;
};

// Get event status color
export const getEventStatusColor = (status) => {
  const colorMap = {
    [EVENT_STATUS.DRAFT]: 'gray',
    [EVENT_STATUS.PUBLISHED]: 'blue',
    [EVENT_STATUS.DEPOSIT_PAID]: 'green',
    [EVENT_STATUS.IN_PROGRESS]: 'purple',
    [EVENT_STATUS.COMPLETED]: 'green',
    [EVENT_STATUS.CANCELLED]: 'red',
  };
  return colorMap[status] || 'gray';
};

// Format application status
export const formatApplicationStatus = (status) => {
  const statusMap = {
    [APPLICATION_STATUS.PENDING]: 'Pending',
    [APPLICATION_STATUS.ACCEPTED]: 'Accepted',
    [APPLICATION_STATUS.CONFIRMED]: 'Confirmed',
    [APPLICATION_STATUS.DECLINED]: 'Declined',
    [APPLICATION_STATUS.REJECTED]: 'Rejected',
    [APPLICATION_STATUS.CANCELLED]: 'Cancelled',
  };
  return statusMap[status] || status;
};

// Get application status color
export const getApplicationStatusColor = (status) => {
  const colorMap = {
    [APPLICATION_STATUS.PENDING]: 'yellow',
    [APPLICATION_STATUS.ACCEPTED]: 'blue',
    [APPLICATION_STATUS.CONFIRMED]: 'green',
    [APPLICATION_STATUS.DECLINED]: 'red',
    [APPLICATION_STATUS.REJECTED]: 'red',
    [APPLICATION_STATUS.CANCELLED]: 'gray',
  };
  return colorMap[status] || 'gray';
};

// Format dispute status
export const formatDisputeStatus = (status) => {
  const statusMap = {
    [DISPUTE_STATUS.OPEN]: 'Open',
    [DISPUTE_STATUS.UNDER_REVIEW]: 'Under Review',
    [DISPUTE_STATUS.RESOLVED]: 'Resolved',
    [DISPUTE_STATUS.CLOSED]: 'Closed',
  };
  return statusMap[status] || status;
};

// Get dispute status color
export const getDisputeStatusColor = (status) => {
  const colorMap = {
    [DISPUTE_STATUS.OPEN]: 'red',
    [DISPUTE_STATUS.UNDER_REVIEW]: 'yellow',
    [DISPUTE_STATUS.RESOLVED]: 'green',
    [DISPUTE_STATUS.CLOSED]: 'gray',
  };
  return colorMap[status] || 'gray';
};

// Format phone number
export const formatPhoneNumber = (phone) => {
  if (!phone) return '';
  const cleaned = phone.replace(/\D/g, '');
  if (cleaned.length === 10) {
    return `(${cleaned.slice(0, 3)}) ${cleaned.slice(3, 6)}-${cleaned.slice(6)}`;
  }
  return phone;
};

// Format file size
export const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
};

// Format percentage
export const formatPercentage = (value, decimals = 0) => {
  return `${(value * 100).toFixed(decimals)}%`;
};

// Format number with commas
export const formatNumber = (num) => {
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
};

// Truncate text
export const truncateText = (text, maxLength = 100) => {
  if (!text) return '';
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};

// Capitalize first letter
export const capitalize = (str) => {
  if (!str) return '';
  return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
};

// Format name (capitalize each word)
export const formatName = (name) => {
  if (!name) return '';
  return name
    .split(' ')
    .map((word) => capitalize(word))
    .join(' ');
};

// Format rating with stars
export const formatRating = (rating) => {
  if (!rating) return 'No rating';
  return `${rating.toFixed(1)} ★`;
};

// Format countdown timer
export const formatCountdown = (endDate) => {
  const now = new Date();
  const end = typeof endDate === 'string' ? new Date(endDate) : endDate;
  const diffMs = end - now;

  if (diffMs <= 0) return 'Expired';

  const diffSecs = Math.floor(diffMs / 1000);
  const diffMins = Math.floor(diffSecs / 60);
  const diffHours = Math.floor(diffMins / 60);
  const diffDays = Math.floor(diffHours / 24);

  if (diffDays > 0) return `${diffDays}d ${diffHours % 24}h`;
  if (diffHours > 0) return `${diffHours}h ${diffMins % 60}m`;
  if (diffMins > 0) return `${diffMins}m ${diffSecs % 60}s`;
  return `${diffSecs}s`;
};
