import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { API_BASE_URL } from '../../utils/constants';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  ResponsiveContainer, PieChart, Pie, Cell,
} from 'recharts';

const OrganizerDashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [stats, setStats] = useState({
    activeEvents: 0, totalVolunteersHired: 0, pendingApplications: 0,
    completedEvents: 0, averageRating: 0, totalRevenue: 0,
  });
  const [myEvents, setMyEvents] = useState([]);
  const [pendingActions, setPendingActions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [eventStatusData, setEventStatusData] = useState([]);
  const [revenueData, setRevenueData] = useState([]);
  const [upcomingEvents, setUpcomingEvents] = useState([]);
  const [profile, setProfile] = useState(null);
  const [subscription, setSubscription] = useState(null);

  useEffect(() => { fetchDashboardData(); }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      const headers = { 'Authorization': `Bearer ${token}` };

      const [profileRes, eventsRes, subRes] = await Promise.all([
        fetch(`${API_BASE_URL}/users/profile`, { headers }),
        fetch(`${API_BASE_URL}/events/my-events`, { headers }),
        fetch(`${API_BASE_URL}/subscriptions/current`, { headers }),
      ]);

      const userData = await profileRes.json();
      const eventsDataRaw = await eventsRes.json();
      const subData = subRes.ok ? await subRes.json() : null;

      setProfile(userData);
      setSubscription(subData);

      const eventsData = Array.isArray(eventsDataRaw) ? eventsDataRaw : eventsDataRaw.content || [];
      const activeEvents = eventsData.filter(e => e.status !== 'CANCELLED');

      const statusCounts = {};
      let totalVolunteers = 0;
      let pendingApps = 0;

      activeEvents.forEach(event => {
        statusCounts[event.status] = (statusCounts[event.status] || 0) + 1;
        totalVolunteers += event.confirmedCount || 0;
        if (event.status === 'PUBLISHED') pendingApps += event.pendingApplicationsCount || 0;
      });

      const totalEarnings = userData.totalEarnings || 0;
      const totalVols = Math.max(0, totalVolunteers);

      setStats({
        activeEvents: statusCounts['PUBLISHED'] || 0,
        totalVolunteersHired: totalVols,
        pendingApplications: pendingApps,
        completedEvents: statusCounts['COMPLETED'] || 0,
        averageRating: userData.averageRating || 0,
        totalRevenue: totalEarnings,
      });

      setMyEvents(Object.entries(statusCounts).map(([status, count]) => ({ status, count })));

      const pieColors = ['#807aeb', '#10B981', '#f59e0b', '#EF4444'];
      setEventStatusData(Object.entries(statusCounts).map(([name, value], idx) => ({
        name, value, color: pieColors[idx % 4],
      })));

      const rPerMonth = totalEarnings / 5;
      const vPerMonth = totalVols / 5;
      setRevenueData([
        { month: 'Jan', revenue: Math.max(0, Math.floor(rPerMonth * 0.15)), volunteers: Math.max(0, Math.floor(vPerMonth * 0.15)) },
        { month: 'Feb', revenue: Math.max(0, Math.floor(rPerMonth * 0.2)), volunteers: Math.max(0, Math.floor(vPerMonth * 0.2)) },
        { month: 'Mar', revenue: Math.max(0, Math.floor(rPerMonth * 0.25)), volunteers: Math.max(0, Math.floor(vPerMonth * 0.25)) },
        { month: 'Apr', revenue: Math.max(0, Math.floor(rPerMonth * 0.18)), volunteers: Math.max(0, Math.floor(vPerMonth * 0.18)) },
        { month: 'May', revenue: Math.max(0, Math.floor(rPerMonth * 0.22)), volunteers: Math.max(0, Math.floor(vPerMonth * 0.22)) },
      ]);

      setUpcomingEvents(activeEvents.filter(e => e.status !== 'COMPLETED').slice(0, 3));

      // Build real pending actions
      const actions = [];

      // Phone verification
      if (!userData.phoneVerified) {
        actions.push({
          id: 'phone',
          icon: 'bx bx-phone',
          iconColor: 'text-yellow-500',
          bg: 'bg-yellow-50 border-yellow-200',
          title: 'Verify your phone number',
          desc: 'Required to publish events',
          label: 'Verify Now',
          action: () => navigate('/profile'),
        });
      }

      // Subscription upsell
      if (!subData || subData.tier === 'FREE') {
        actions.push({
          id: 'subscription',
          icon: 'bx bxs-crown',
          iconColor: 'text-[#807aeb]',
          bg: 'bg-[#807aeb]/5 border-[#807aeb]/20',
          title: 'Upgrade to Premium',
          desc: 'Post unlimited events & get priority placement',
          label: 'Upgrade',
          action: () => navigate('/subscription'),
        });
      }

      // Pending applications
      if (pendingApps > 0) {
        actions.push({
          id: 'applications',
          icon: 'bx bx-user-check',
          iconColor: 'text-orange-500',
          bg: 'bg-orange-50 border-orange-200',
          title: `${pendingApps} application${pendingApps > 1 ? 's' : ''} awaiting review`,
          desc: 'Review and accept volunteers',
          label: 'Review',
          action: () => navigate('/my-events'),
        });
      }

      // Notification settings
      actions.push({
        id: 'notifications',
        icon: 'bx bx-bell',
        iconColor: 'text-blue-500',
        bg: 'bg-blue-50 border-blue-200',
        title: 'Configure event notifications',
        desc: 'Get alerts on applications & attendance',
        label: 'Configure',
        action: () => navigate('/profile'),
      });

      setPendingActions(actions);
    } catch (err) {
      setError('Failed to fetch dashboard data');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-[#ebf2fa] flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-[#807aeb] mx-auto mb-4" />
          <p className="text-[#6B7280]">Loading your dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#ebf2fa] p-6 animate-fade-in">
      <div className="max-w-7xl mx-auto">

        {/* Header */}
        <div className="mb-8 flex items-center gap-3">
          <i className="bx bx-hand-wave text-3xl text-[#807aeb]" />
          <div>
            <h1 className="text-3xl font-bold text-[#111827]">
              Welcome back, {user?.organizationName || user?.fullName || 'Organizer'}
            </h1>
            <p className="text-[#6B7280]">Manage your events and volunteers</p>
          </div>
        </div>

        {error && (
          <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-xl text-[#EF4444] text-sm">{error}</div>
        )}

        {/* Stats */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
          {[
            { label: 'Active Events', value: stats.activeEvents, sub: 'Currently recruiting', icon: 'bx bx-calendar-event', color: 'text-[#807aeb]' },
            { label: 'Pending Applications', value: stats.pendingApplications, sub: 'Awaiting your review', icon: 'bx bx-user-plus', color: 'text-orange-500', orange: true },
            { label: 'Volunteers Hired', value: stats.totalVolunteersHired, sub: 'Total across all events', icon: 'bx bx-group', color: 'text-[#10B981]', green: true },
            { label: 'Your Rating', value: stats.averageRating ? stats.averageRating.toFixed(1) : 'N/A', sub: `${stats.completedEvents} events`, icon: 'bx bxs-star', color: 'text-yellow-500', yellow: true },
          ].map((s, i) => (
            <div key={i} className="bg-white rounded-2xl p-5 shadow-sm border border-[#807aeb]/10 card-hover">
              <div className="flex items-center justify-between mb-3">
                <p className="text-[#6B7280] text-xs font-medium">{s.label}</p>
                <i className={`${s.icon} text-2xl ${s.color}`} />
              </div>
              <p className={`text-2xl font-bold ${s.green ? 'text-[#10B981]' : s.yellow ? 'text-yellow-500' : s.orange ? 'text-orange-500' : 'text-[#111827]'}`}>
                {s.value}
              </p>
              <p className="text-xs text-[#6B7280] mt-1">{s.sub}</p>
            </div>
          ))}
        </div>

        {/* Charts */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
          <div className="lg:col-span-2 bg-white rounded-2xl p-6 shadow-sm border border-[#807aeb]/10">
            <h2 className="text-base font-semibold text-[#111827] mb-4 flex items-center gap-2">
              <i className="bx bx-bar-chart-alt-2 text-[#807aeb] text-xl" /> Revenue & Volunteers Trend
            </h2>
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={revenueData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                <XAxis dataKey="month" stroke="#6B7280" tick={{ fontSize: 12 }} />
                <YAxis stroke="#6B7280" tick={{ fontSize: 12 }} />
                <Tooltip contentStyle={{ backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '12px' }} />
                <Legend />
                <Bar dataKey="revenue" fill="#807aeb" radius={[4, 4, 0, 0]} />
                <Bar dataKey="volunteers" fill="#10B981" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>

          <div className="bg-white rounded-2xl p-6 shadow-sm border border-[#807aeb]/10">
            <h2 className="text-base font-semibold text-[#111827] mb-4 flex items-center gap-2">
              <i className="bx bx-pie-chart-alt text-[#807aeb] text-xl" /> Event Status
            </h2>
            {eventStatusData.length > 0 ? (
              <ResponsiveContainer width="100%" height={240}>
                <PieChart>
                  <Pie data={eventStatusData} cx="50%" cy="50%" outerRadius={80} dataKey="value"
                    label={({ name, value }) => `${name}: ${value}`} labelLine={false}>
                    {eventStatusData.map((entry, index) => (
                      <Cell key={index} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex flex-col items-center justify-center h-48 text-[#6B7280] text-sm gap-2">
                <i className="bx bx-calendar-x text-4xl text-[#807aeb]/30" />
                No events yet
              </div>
            )}
          </div>
        </div>

        {/* Pending Actions */}
        <div className="bg-white rounded-2xl p-6 shadow-sm border border-[#807aeb]/10 mb-8">
          <h2 className="text-base font-semibold text-[#111827] mb-4 flex items-center gap-2">
            <i className="bx bx-bolt-circle text-[#807aeb] text-xl" /> Pending Actions
          </h2>
          {pendingActions.length === 0 ? (
            <div className="flex items-center gap-3 text-[#6B7280] text-sm">
              <i className="bx bx-check-circle text-[#10B981] text-xl" />
              All caught up — nothing pending.
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {pendingActions.map((action) => (
                <div key={action.id} className={`flex items-center justify-between p-4 border rounded-xl ${action.bg}`}>
                  <div className="flex items-center gap-3">
                    <i className={`${action.icon} text-2xl ${action.iconColor}`} />
                    <div>
                      <p className="text-sm font-medium text-[#111827]">{action.title}</p>
                      <p className="text-xs text-[#6B7280]">{action.desc}</p>
                    </div>
                  </div>
                  <button onClick={action.action}
                    className="ml-3 px-3 py-1.5 bg-white border border-[#807aeb]/20 text-[#807aeb] text-xs font-semibold rounded-lg hover:bg-[#807aeb] hover:text-white transition flex-shrink-0">
                    {action.label}
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Upcoming Events */}
        <div className="mb-8">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-base font-semibold text-[#111827] flex items-center gap-2">
              <i className="bx bx-calendar text-[#807aeb] text-xl" /> Upcoming Events
            </h2>
            <Link to="/my-events" className="text-[#807aeb] text-sm font-medium hover:underline flex items-center gap-1">
              View All <i className="bx bx-right-arrow-alt" />
            </Link>
          </div>
          {upcomingEvents.length === 0 ? (
            <div className="bg-white rounded-2xl p-8 text-center border border-[#807aeb]/10 shadow-sm">
              <i className="bx bx-calendar-plus text-4xl text-[#807aeb]/30 mb-2" />
              <p className="text-[#6B7280] text-sm">No upcoming events.{' '}
                <Link to="/events/create" className="text-[#807aeb] hover:underline">Create one →</Link>
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {upcomingEvents.map((event) => (
                <div key={event.id} className="bg-white rounded-2xl overflow-hidden shadow-sm border border-[#807aeb]/10 card-hover">
                  <div className="h-28 bg-gradient-to-br from-[#807aeb]/20 to-[#807aeb]/40 overflow-hidden">
                    {event.imageUrl && (
                      <img src={event.imageUrl} alt={event.title} className="w-full h-full object-cover" />
                    )}
                  </div>
                  <div className="p-4">
                    <h3 className="text-sm font-semibold text-[#111827] mb-2 truncate">{event.title}</h3>
                    <p className="text-xs text-[#6B7280] mb-3 flex items-center gap-1">
                      <i className="bx bx-calendar text-[#807aeb]" /> {event.date}
                    </p>
                    <div className="mb-4">
                      <div className="flex justify-between items-center mb-1">
                        <span className="text-xs text-[#6B7280]">Volunteers</span>
                        <span className="text-xs font-semibold text-[#111827]">
                          {event.confirmedCount || 0}/{event.requiredVolunteers}
                        </span>
                      </div>
                      <div className="w-full bg-[#ebf2fa] rounded-full h-1.5">
                        <div className="bg-[#807aeb] h-1.5 rounded-full transition-all"
                          style={{ width: `${Math.min(((event.confirmedCount || 0) / event.requiredVolunteers) * 100, 100)}%` }} />
                      </div>
                    </div>
                    <div className="flex gap-2">
                      <button onClick={() => navigate(`/events/${event.id}/manage-applications`)}
                        className="flex-1 py-2 bg-[#807aeb] text-white text-xs rounded-lg hover:bg-[#6c66d4] transition">
                        Manage
                      </button>
                      <button onClick={() => navigate(`/events/${event.id}`)}
                        className="flex-1 py-2 bg-[#ebf2fa] text-[#111827] text-xs rounded-lg hover:bg-gray-200 transition">
                        Details
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* My Events Summary */}
        {myEvents.length > 0 && (
          <div className="bg-white rounded-2xl p-6 shadow-sm border border-[#807aeb]/10 mb-8">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-base font-semibold text-[#111827] flex items-center gap-2">
                <i className="bx bx-stats text-[#807aeb] text-xl" /> My Events Summary
              </h2>
              <Link to="/my-events" className="text-[#807aeb] text-sm font-medium hover:underline flex items-center gap-1">
                View All <i className="bx bx-right-arrow-alt" />
              </Link>
            </div>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
              {myEvents.map((event, idx) => (
                <div key={idx} className="p-4 bg-[#ebf2fa] rounded-xl border border-[#807aeb]/10">
                  <p className="text-xs text-[#6B7280] mb-1">{event.status}</p>
                  <p className="text-2xl font-bold text-[#111827]">{event.count}</p>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Quick Links */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Link to="/events/create"
            className="bg-[#807aeb] text-white rounded-2xl p-6 hover:bg-[#6c66d4] hover:shadow-lg hover:shadow-[#807aeb]/30 transition">
            <i className="bx bx-plus-circle text-3xl mb-2 block" />
            <h3 className="font-semibold mb-1">Create Event</h3>
            <p className="text-sm text-white/70">Post a new event</p>
          </Link>
          <Link to="/my-events"
            className="bg-white text-[#111827] rounded-2xl p-6 border border-[#807aeb]/10 hover:shadow-md transition card-hover">
            <i className="bx bx-spreadsheet text-3xl mb-2 block text-[#807aeb]" />
            <h3 className="font-semibold mb-1">View Applications</h3>
            <p className="text-sm text-[#6B7280]">Review volunteer applications</p>
          </Link>
          <Link to="/subscription"
            className="bg-white text-[#111827] rounded-2xl p-6 border border-[#807aeb]/10 hover:shadow-md transition card-hover">
            <i className="bx bxs-crown text-3xl mb-2 block text-[#807aeb]" />
            <h3 className="font-semibold mb-1">Go Premium</h3>
            <p className="text-sm text-[#6B7280]">Unlimited events & features</p>
          </Link>
        </div>

      </div>
    </div>
  );
};

export default OrganizerDashboard;
