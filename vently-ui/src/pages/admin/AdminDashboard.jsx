import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { adminApi } from '../../api/adminApi';
import { API_BASE_URL } from '../../utils/constants';
import { useAuthStore } from '../../store/authStore';

const AdminDashboard = () => {
  const [analytics, setAnalytics] = useState(null);
  const [recentActivity, setRecentActivity] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Trace ID lookup
  const [traceId, setTraceId] = useState('');
  const [traceResult, setTraceResult] = useState(null);
  const [traceLoading, setTraceLoading] = useState(false);
  const [traceError, setTraceError] = useState('');

  const { token } = useAuthStore();

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const response = await adminApi.getAnalytics({ days: 30 });
      setAnalytics(response);
      const auditResponse = await adminApi.getAuditLogs(1, 10);
      setRecentActivity(auditResponse.auditLogs || auditResponse.content || []);
    } catch (err) {
      setError('Failed to fetch dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const lookupTraceId = async () => {
    if (!traceId.trim()) return;
    setTraceLoading(true);
    setTraceError('');
    setTraceResult(null);
    try {
      const res = await fetch(`${API_BASE_URL}/admin/errors/${traceId.trim()}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      const data = await res.json();
      if (!res.ok || !data.found) {
        setTraceError(data.message || 'No error found for this trace ID.');
      } else {
        setTraceResult(data);
      }
    } catch {
      setTraceError('Failed to look up trace ID.');
    } finally {
      setTraceLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ background: '#ebf2fa' }}>
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 mx-auto mb-4" style={{ borderColor: '#807aeb' }}></div>
          <p style={{ color: '#6B7280' }}>Loading dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen p-6 pb-24" style={{ background: '#ebf2fa' }}>
      <div className="max-w-7xl mx-auto">

        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold mb-1" style={{ color: '#111827' }}>Admin Dashboard</h1>
          <p style={{ color: '#6B7280' }}>Platform overview and management tools</p>
        </div>

        {error && (
          <div className="mb-4 p-4 rounded-xl border text-sm" style={{ background: '#FEE2E2', borderColor: '#EF4444', color: '#EF4444' }}>
            {error}
          </div>
        )}

        {/* Summary Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
          {[
            { label: 'Total Users', value: analytics?.totalUsers || 0, icon: 'bx-group', link: '/admin/users', sub: 'Click to manage' },
            { label: 'Total Events', value: analytics?.totalEvents || 0, icon: 'bx-calendar-event', link: '/admin/analytics', sub: `${analytics?.completedEvents || 0} completed` },
            { label: 'Revenue (₹)', value: `₹${(analytics?.totalRevenue || 0).toFixed(0)}`, icon: 'bx-rupee', link: '/admin/analytics', sub: `Fees: ₹${(analytics?.platformFees || 0).toFixed(0)}` },
            { label: 'Open Disputes', value: analytics?.openDisputes || 0, icon: 'bx-shield-x', link: '/admin/disputes', sub: 'Click to resolve', danger: true },
          ].map((card) => (
            <Link
              key={card.label}
              to={card.link}
              className="rounded-2xl p-6 shadow-sm hover:shadow-md transition"
              style={{ background: '#fff', border: `1.5px solid ${card.danger ? '#EF4444' : '#807aeb'}` }}
            >
              <div className="flex items-center gap-3 mb-3">
                <div className="w-10 h-10 rounded-xl flex items-center justify-center" style={{ background: card.danger ? '#FEE2E2' : '#EDE9FE' }}>
                  <i className={`bx ${card.icon} text-xl`} style={{ color: card.danger ? '#EF4444' : '#807aeb' }}></i>
                </div>
                <p className="text-sm font-medium" style={{ color: '#6B7280' }}>{card.label}</p>
              </div>
              <p className="text-3xl font-bold mb-1" style={{ color: '#111827' }}>{card.value}</p>
              <p className="text-xs" style={{ color: '#6B7280' }}>{card.sub}</p>
            </Link>
          ))}
        </div>

        {/* Quick Actions */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
          {[
            { to: '/admin/users', icon: 'bx-user-check', label: 'Manage Users', sub: 'Suspend, ban, verify' },
            { to: '/admin/disputes', icon: 'bx-balance-scale', label: 'Resolve Disputes', sub: 'Review and resolve' },
            { to: '/admin/analytics', icon: 'bx-bar-chart-alt-2', label: 'Analytics', sub: 'Platform metrics' },
            { to: '/admin/audit-logs', icon: 'bx-list-check', label: 'Audit Logs', sub: 'Track activities' },
          ].map((action) => (
            <Link
              key={action.to}
              to={action.to}
              className="rounded-2xl p-4 text-center shadow-sm hover:shadow-md transition"
              style={{ background: '#fff', border: '1.5px solid #e5e7eb' }}
            >
              <div className="w-10 h-10 rounded-xl flex items-center justify-center mx-auto mb-2" style={{ background: '#EDE9FE' }}>
                <i className={`bx ${action.icon} text-xl`} style={{ color: '#807aeb' }}></i>
              </div>
              <p className="font-medium text-sm" style={{ color: '#111827' }}>{action.label}</p>
              <p className="text-xs mt-1" style={{ color: '#6B7280' }}>{action.sub}</p>
            </Link>
          ))}
        </div>

        {/* Trace ID Lookup */}
        <div className="rounded-2xl p-6 shadow-sm mb-8" style={{ background: '#fff', border: '1.5px solid #e5e7eb' }}>
          <div className="flex items-center gap-2 mb-4">
            <i className="bx bx-search-alt text-xl" style={{ color: '#807aeb' }}></i>
            <h2 className="text-lg font-semibold" style={{ color: '#111827' }}>Error Trace Lookup</h2>
          </div>
          <p className="text-sm mb-4" style={{ color: '#6B7280' }}>
            Enter a trace ID reported by a user to see the full error details.
          </p>
          <div className="flex gap-3">
            <input
              type="text"
              value={traceId}
              onChange={(e) => setTraceId(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && lookupTraceId()}
              placeholder="e.g. 3f2a1b4c-..."
              className="flex-1 px-4 py-2 rounded-xl border text-sm outline-none focus:ring-2"
              style={{ borderColor: '#e5e7eb', color: '#111827', '--tw-ring-color': '#807aeb' }}
            />
            <button
              onClick={lookupTraceId}
              disabled={traceLoading}
              className="px-5 py-2 rounded-xl text-sm font-medium text-white transition hover:opacity-90 disabled:opacity-60"
              style={{ background: '#807aeb' }}
            >
              {traceLoading ? 'Looking up...' : 'Look Up'}
            </button>
          </div>

          {traceError && (
            <p className="mt-3 text-sm" style={{ color: '#EF4444' }}>{traceError}</p>
          )}

          {traceResult && (
            <div className="mt-4 rounded-xl p-4" style={{ background: '#F9FAFB', border: '1px solid #e5e7eb' }}>
              <div className="grid grid-cols-2 gap-3 text-sm mb-3">
                <div>
                  <p className="font-medium mb-1" style={{ color: '#6B7280' }}>Error Type</p>
                  <p style={{ color: '#111827' }}>{traceResult.errorType}</p>
                </div>
                <div>
                  <p className="font-medium mb-1" style={{ color: '#6B7280' }}>HTTP Status</p>
                  <p style={{ color: '#111827' }}>{traceResult.httpStatus}</p>
                </div>
                <div>
                  <p className="font-medium mb-1" style={{ color: '#6B7280' }}>Path</p>
                  <p className="font-mono text-xs" style={{ color: '#111827' }}>{traceResult.path}</p>
                </div>
                <div>
                  <p className="font-medium mb-1" style={{ color: '#6B7280' }}>Timestamp</p>
                  <p style={{ color: '#111827' }}>{new Date(traceResult.timestamp).toLocaleString()}</p>
                </div>
                {traceResult.userId && (
                  <div>
                    <p className="font-medium mb-1" style={{ color: '#6B7280' }}>User ID</p>
                    <p style={{ color: '#111827' }}>{traceResult.userId}</p>
                  </div>
                )}
                <div>
                  <p className="font-medium mb-1" style={{ color: '#6B7280' }}>IP Address</p>
                  <p style={{ color: '#111827' }}>{traceResult.ipAddress}</p>
                </div>
              </div>
              <div className="mb-3">
                <p className="font-medium mb-1 text-sm" style={{ color: '#6B7280' }}>Message</p>
                <p className="text-sm" style={{ color: '#111827' }}>{traceResult.message}</p>
              </div>
              {traceResult.stackTrace && (
                <div>
                  <p className="font-medium mb-1 text-sm" style={{ color: '#6B7280' }}>Stack Trace</p>
                  <pre className="text-xs overflow-auto max-h-48 p-3 rounded-lg" style={{ background: '#1e293b', color: '#e2e8f0' }}>
                    {traceResult.stackTrace}
                  </pre>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Quick Stats */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
          {[
            { label: 'Event Completion Rate', value: analytics?.eventCompletionRate ? `${analytics.eventCompletionRate.toFixed(1)}%` : 'N/A', icon: 'bx-check-circle' },
            { label: 'Average User Rating', value: analytics?.averageUserRating ? `${analytics.averageUserRating.toFixed(1)} / 5` : 'N/A', icon: 'bx-star' },
            { label: 'No-Show Rate', value: analytics?.noShowStats?.rate ? `${analytics.noShowStats.rate.toFixed(1)}%` : 'N/A', icon: 'bx-user-x' },
          ].map((stat) => (
            <div key={stat.label} className="rounded-2xl p-6 shadow-sm" style={{ background: '#fff', border: '1.5px solid #e5e7eb' }}>
              <div className="flex items-center gap-2 mb-2">
                <i className={`bx ${stat.icon} text-lg`} style={{ color: '#807aeb' }}></i>
                <p className="text-sm" style={{ color: '#6B7280' }}>{stat.label}</p>
              </div>
              <p className="text-3xl font-bold" style={{ color: '#111827' }}>{stat.value}</p>
            </div>
          ))}
        </div>

        {/* Recent Activity */}
        <div className="rounded-2xl p-6 shadow-sm" style={{ background: '#fff', border: '1.5px solid #e5e7eb' }}>
          <div className="flex justify-between items-center mb-4">
            <div className="flex items-center gap-2">
              <i className="bx bx-history text-xl" style={{ color: '#807aeb' }}></i>
              <h2 className="text-lg font-semibold" style={{ color: '#111827' }}>Recent Activity</h2>
            </div>
            <Link to="/admin/audit-logs" className="text-sm font-medium hover:underline" style={{ color: '#807aeb' }}>
              View All →
            </Link>
          </div>

          {recentActivity.length === 0 ? (
            <p className="text-sm" style={{ color: '#6B7280' }}>No recent activity</p>
          ) : (
            <div className="space-y-2">
              {recentActivity.map((activity) => (
                <div
                  key={activity.id}
                  className="flex items-center justify-between p-3 rounded-xl"
                  style={{ background: '#F9FAFB', border: '1px solid #e5e7eb' }}
                >
                  <div>
                    <p className="text-sm" style={{ color: '#111827' }}>
                      <span className="font-medium">{activity.userName || 'System'}</span>
                      {' '}
                      <span style={{ color: '#6B7280' }}>{activity.action}</span>
                      {' '}
                      <span style={{ color: '#9CA3AF' }}>{activity.entityType}</span>
                    </p>
                    <p className="text-xs mt-0.5" style={{ color: '#9CA3AF' }}>
                      {new Date(activity.timestamp).toLocaleString()}
                    </p>
                  </div>
                  {activity.ipAddress && (
                    <p className="text-xs font-mono" style={{ color: '#9CA3AF' }}>{activity.ipAddress}</p>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>

      </div>
    </div>
  );
};

export default AdminDashboard;
