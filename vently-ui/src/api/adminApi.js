import axiosInstance from './axiosConfig';

// Named object export — used by admin pages as adminApi.xxx(...)
export const adminApi = {
  // ── Users ──────────────────────────────────────────────────────────────────
  getAllUsers: async (page = 1, size = 10, filters = {}) => {
    const { search, role, status, verified } = filters;
    const res = await axiosInstance.get('/admin/users', {
      params: { page: page - 1, size, search, role, status, verified },
    });
    return res.data;
  },

  getUserById: async (userId) => {
    const res = await axiosInstance.get(`/admin/users/${userId}`);
    return res.data;
  },

  verifyUser: async (userId) => {
    const res = await axiosInstance.post(`/admin/users/${userId}/verify`);
    return res.data;
  },

  suspendUser: async (userId, payload) => {
    // Backend expects { durationInDays, reason }
    const res = await axiosInstance.post(`/admin/users/${userId}/suspend`, {
      durationInDays: payload.durationDays ?? payload.durationInDays,
      reason: payload.reason,
    });
    return res.data;
  },

  banUser: async (userId, payload) => {
    const res = await axiosInstance.post(`/admin/users/${userId}/ban`, payload);
    return res.data;
  },

  resetPassword: async (userId) => {
    const res = await axiosInstance.post(`/admin/users/${userId}/reset-password`);
    return res.data;
  },

  adjustNoShows: async (userId, payload) => {
    // Backend expects { newCount, reason } — frontend sends { adjustment, reason }
    // Calculate newCount from current + adjustment if needed
    const res = await axiosInstance.put(`/admin/users/${userId}/no-shows`, {
      newCount: payload.newCount ?? payload.adjustment,
      reason: payload.reason,
    });
    return res.data;
  },

  // ── Disputes ───────────────────────────────────────────────────────────────
  // Actual endpoints live under /disputes/admin/...
  getAllDisputes: async (page = 1, size = 10, filters = {}) => {
    const res = await axiosInstance.get('/disputes/admin/open', {
      params: { page: page - 1, size },
    });
    // Returns a Spring Page object directly
    return {
      content: res.data?.content ?? [],
      totalPages: res.data?.totalPages ?? 1,
      totalElements: res.data?.totalElements ?? 0,
    };
  },

  getDisputeDetails: async (disputeId) => {
    const res = await axiosInstance.get(`/disputes/${disputeId}`);
    return res.data;
  },

  resolveDispute: async (disputeId, payload) => {
    // Backend uses query params: resolution, paymentAdjustment, noShowAdjustment
    const res = await axiosInstance.post(`/disputes/admin/${disputeId}/resolve`, null, {
      params: {
        resolution: payload.resolutionNotes ?? payload.resolution,
        paymentAdjustment: payload.paymentAdjustment,
        noShowAdjustment: payload.penaltyAdjustment,
      },
    });
    return res.data;
  },

  // ── Analytics ──────────────────────────────────────────────────────────────
  // Backend takes startDate/endDate (LocalDate), not a "days" param.
  // We compute the dates here and flatten the nested response for the UI.
  getAnalytics: async (daysOrOptions = 30) => {
    const days = typeof daysOrOptions === 'object' ? (daysOrOptions.days ?? 30) : daysOrOptions;
    const endDate = new Date();
    const startDate = new Date();
    startDate.setDate(endDate.getDate() - days);
    const fmt = (d) => d.toISOString().split('T')[0]; // YYYY-MM-DD

    const res = await axiosInstance.get('/admin/analytics', {
      params: { startDate: fmt(startDate), endDate: fmt(endDate) },
    });

    // Backend returns { platformAnalytics: {...}, startDate, endDate }
    // Flatten so the UI can use analytics.totalUsers etc.
    const pa = res.data?.platformAnalytics ?? res.data ?? {};
    return {
      totalUsers: pa.totalUsers ?? 0,
      totalVolunteers: pa.totalVolunteers ?? 0,
      totalOrganizers: pa.totalOrganizers ?? 0,
      totalEvents: pa.totalEvents ?? 0,
      completedEvents: pa.completedEvents ?? 0,
      cancelledEvents: pa.cancelledEvents ?? 0,
      totalRevenue: pa.totalRevenue ?? 0,
      platformFees: pa.platformFeesCollected ?? 0,
      openDisputes: pa.openDisputes ?? 0,
      eventCompletionRate: pa.totalEvents
        ? ((pa.completedEvents ?? 0) / pa.totalEvents) * 100
        : 0,
    };
  },

  // ── Audit Logs ─────────────────────────────────────────────────────────────
  getAuditLogs: async (page = 1, size = 20, filters = {}) => {
    const { action, search, dateFrom, dateTo, userId } = filters;
    const res = await axiosInstance.get('/admin/audit-logs', {
      params: {
        page: page - 1,
        size,
        action,
        userId,
        startDate: dateFrom,
        endDate: dateTo,
      },
    });
    // Backend returns { auditLogs: [...], totalPages, totalElements, ... }
    return {
      content: res.data?.auditLogs ?? [],
      totalPages: res.data?.totalPages ?? 1,
      totalElements: res.data?.totalElements ?? 0,
    };
  },

  // ── Error trace lookup ─────────────────────────────────────────────────────
  getErrorByTraceId: async (traceId) => {
    const res = await axiosInstance.get(`/admin/errors/${traceId}`);
    return res.data;
  },
};

// Individual named exports (kept for backward compat)
export const getAnalytics = (days = 30) =>
  axiosInstance.get('/admin/analytics');
export const getAuditLogs = (page = 0, size = 20) =>
  axiosInstance.get('/admin/audit-logs', { params: { page, size } });
