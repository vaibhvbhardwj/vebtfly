import React, { useState, useEffect } from 'react';
import {
  LineChart, Line, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
} from 'recharts';
import { adminApi } from '../../api/adminApi';

const COLORS = ['#807aeb', '#a78bfa', '#ec4899', '#f59e0b', '#10b981', '#06b6d4'];

const Analytics = () => {
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [dateRange, setDateRange] = useState('30');

  useEffect(() => { fetchAnalytics(); }, [dateRange]);

  const fetchAnalytics = async () => {
    try {
      setLoading(true);
      setError('');
      const response = await adminApi.getAnalytics({ days: parseInt(dateRange) });
      setAnalytics(response);
    } catch (err) {
      setError('Failed to fetch analytics');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ background: '#ebf2fa' }}>
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 mx-auto mb-4" style={{ borderColor: '#807aeb' }}></div>
          <p style={{ color: '#6B7280' }}>Loading analytics...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen p-6 pb-24" style={{ background: '#ebf2fa' }}>
      <div className="max-w-7xl mx-auto">

        {/* Header */}
        <div className="mb-8 flex justify-between items-start">
          <div>
            <h1 className="text-3xl font-bold mb-1" style={{ color: '#111827' }}>Analytics Dashboard</h1>
            <p style={{ color: '#6B7280' }}>Platform metrics and trends</p>
          </div>
          <div>
            <label className="block text-sm font-medium mb-2" style={{ color: '#6B7280' }}>Date Range</label>
            <select
              value={dateRange}
              onChange={(e) => setDateRange(e.target.value)}
              className="px-4 py-2 rounded-xl border text-sm outline-none"
              style={{ borderColor: '#e5e7eb', color: '#111827', background: '#fff' }}
            >
              <option value="7">Last 7 days</option>
              <option value="30">Last 30 days</option>
              <option value="90">Last 90 days</option>
              <option value="365">Last year</option>
            </select>
          </div>
        </div>

        {error && (
          <div className="mb-6 p-4 rounded-xl border text-sm" style={{ background: '#FEE2E2', borderColor: '#EF4444', color: '#EF4444' }}>
            {error}
          </div>
        )}

        {!analytics && !error && (
          <div className="p-6 rounded-2xl text-sm" style={{ background: '#fff', border: '1.5px solid #e5e7eb', color: '#6B7280' }}>
            No analytics data available
          </div>
        )}

        {analytics && (
          <>
            {/* Key Metrics */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
              {[
                { label: 'Total Users', value: analytics.totalUsers || 0, icon: 'bx-group', sub: `${analytics.totalVolunteers || 0} volunteers · ${analytics.totalOrganizers || 0} organizers` },
                { label: 'Total Events', value: analytics.totalEvents || 0, icon: 'bx-calendar-event', sub: `${analytics.completedEvents || 0} completed` },
                { label: 'Total Revenue', value: `₹${(analytics.totalRevenue || 0).toFixed(0)}`, icon: 'bx-rupee', sub: `Fees: ₹${(analytics.platformFees || 0).toFixed(0)}` },
                { label: 'Open Disputes', value: analytics.openDisputes || 0, icon: 'bx-shield-x', sub: 'Pending resolution', danger: true },
              ].map((card) => (
                <div key={card.label} className="rounded-2xl p-6 shadow-sm" style={{ background: '#fff', border: `1.5px solid ${card.danger ? '#EF4444' : '#e5e7eb'}` }}>
                  <div className="flex items-center gap-3 mb-3">
                    <div className="w-10 h-10 rounded-xl flex items-center justify-center" style={{ background: card.danger ? '#FEE2E2' : '#EDE9FE' }}>
                      <i className={`bx ${card.icon} text-xl`} style={{ color: card.danger ? '#EF4444' : '#807aeb' }}></i>
                    </div>
                    <p className="text-sm font-medium" style={{ color: '#6B7280' }}>{card.label}</p>
                  </div>
                  <p className="text-3xl font-bold mb-1" style={{ color: '#111827' }}>{card.value}</p>
                  <p className="text-xs" style={{ color: '#9CA3AF' }}>{card.sub}</p>
                </div>
              ))}
            </div>

            {/* Charts */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
              {analytics.userGrowthData && analytics.userGrowthData.length > 0 && (
                <div className="rounded-2xl p-6 shadow-sm" style={{ background: '#fff', border: '1.5px solid #e5e7eb' }}>
                  <h2 className="text-lg font-semibold mb-4" style={{ color: '#111827' }}>User Growth Trend</h2>
                  <ResponsiveContainer width="100%" height={280}>
                    <LineChart data={analytics.userGrowthData}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                      <XAxis dataKey="date" stroke="#9CA3AF" tick={{ fontSize: 12 }} />
                      <YAxis stroke="#9CA3AF" tick={{ fontSize: 12 }} />
                      <Tooltip contentStyle={{ borderRadius: 12, border: '1px solid #e5e7eb' }} />
                      <Legend />
                      <Line type="monotone" dataKey="volunteers" stroke="#807aeb" strokeWidth={2} dot={false} />
                      <Line type="monotone" dataKey="organizers" stroke="#ec4899" strokeWidth={2} dot={false} />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              )}

              {analytics.revenueTrendData && analytics.revenueTrendData.length > 0 && (
                <div className="rounded-2xl p-6 shadow-sm" style={{ background: '#fff', border: '1.5px solid #e5e7eb' }}>
                  <h2 className="text-lg font-semibold mb-4" style={{ color: '#111827' }}>Revenue Trends</h2>
                  <ResponsiveContainer width="100%" height={280}>
                    <BarChart data={analytics.revenueTrendData}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                      <XAxis dataKey="date" stroke="#9CA3AF" tick={{ fontSize: 12 }} />
                      <YAxis stroke="#9CA3AF" tick={{ fontSize: 12 }} />
                      <Tooltip contentStyle={{ borderRadius: 12, border: '1px solid #e5e7eb' }} />
                      <Legend />
                      <Bar dataKey="revenue" fill="#807aeb" radius={[4, 4, 0, 0]} />
                      <Bar dataKey="fees" fill="#a78bfa" radius={[4, 4, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              )}
            </div>

            {/* Event Completion, Ratings, No-Shows */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
              {analytics.eventCompletionData && (
                <div className="rounded-2xl p-6 shadow-sm" style={{ background: '#fff', border: '1.5px solid #e5e7eb' }}>
                  <h2 className="text-lg font-semibold mb-4" style={{ color: '#111827' }}>Event Completion Rate</h2>
                  <ResponsiveContainer width="100%" height={220}>
                    <PieChart>
                      <Pie data={analytics.eventCompletionData} cx="50%" cy="50%" outerRadius={80} dataKey="value"
                        label={({ name, value }) => `${name}: ${value}%`} labelLine={false}>
                        {analytics.eventCompletionData.map((_, index) => (
                          <Cell key={index} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip contentStyle={{ borderRadius: 12, border: '1px solid #e5e7eb' }} />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              )}

              {analytics.averageRatingsByRole && (
                <div className="rounded-2xl p-6 shadow-sm" style={{ background: '#fff', border: '1.5px solid #e5e7eb' }}>
                  <h2 className="text-lg font-semibold mb-4" style={{ color: '#111827' }}>Average Ratings by Role</h2>
                  <div className="space-y-4">
                    {analytics.averageRatingsByRole.map((role, idx) => (
                      <div key={idx}>
                        <div className="flex justify-between mb-1">
                          <span className="text-sm" style={{ color: '#6B7280' }}>{role.role}</span>
                          <span className="text-sm font-semibold" style={{ color: '#111827' }}>{role.rating.toFixed(1)} ⭐</span>
                        </div>
                        <div className="w-full rounded-full h-2" style={{ background: '#EDE9FE' }}>
                          <div className="h-2 rounded-full" style={{ width: `${(role.rating / 5) * 100}%`, background: '#807aeb' }}></div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {analytics.noShowStats && (
                <div className="rounded-2xl p-6 shadow-sm" style={{ background: '#fff', border: '1.5px solid #e5e7eb' }}>
                  <h2 className="text-lg font-semibold mb-4" style={{ color: '#111827' }}>No-Show Statistics</h2>
                  <div className="space-y-4">
                    {[
                      { label: 'Total No-Shows', value: analytics.noShowStats.total || 0, color: '#111827' },
                      { label: 'No-Show Rate', value: analytics.noShowStats.rate ? `${analytics.noShowStats.rate.toFixed(1)}%` : 'N/A', color: '#111827' },
                      { label: 'Users Suspended', value: analytics.noShowStats.suspended || 0, color: '#F59E0B' },
                      { label: 'Users Banned', value: analytics.noShowStats.banned || 0, color: '#EF4444' },
                    ].map((stat) => (
                      <div key={stat.label}>
                        <p className="text-sm mb-1" style={{ color: '#6B7280' }}>{stat.label}</p>
                        <p className="text-2xl font-bold" style={{ color: stat.color }}>{stat.value}</p>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>

            {/* User Distribution */}
            {analytics.userDistribution && (
              <div className="rounded-2xl p-6 shadow-sm" style={{ background: '#fff', border: '1.5px solid #e5e7eb' }}>
                <h2 className="text-lg font-semibold mb-4" style={{ color: '#111827' }}>User Distribution by Role</h2>
                <ResponsiveContainer width="100%" height={280}>
                  <BarChart data={analytics.userDistribution}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                    <XAxis dataKey="role" stroke="#9CA3AF" tick={{ fontSize: 12 }} />
                    <YAxis stroke="#9CA3AF" tick={{ fontSize: 12 }} />
                    <Tooltip contentStyle={{ borderRadius: 12, border: '1px solid #e5e7eb' }} />
                    <Bar dataKey="count" fill="#807aeb" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

export default Analytics;
