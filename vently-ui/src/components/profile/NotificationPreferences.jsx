import React, { useState, useEffect } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { API_BASE_URL } from '../../utils/constants';

export const NotificationPreferences = () => {
  useAuth();
  const [preferences, setPreferences] = useState({
    emailApplicationStatus: true,
    emailPaymentNotifications: true,
    emailEventCancellations: true,
    emailDisputeUpdates: true,
    emailRatingNotifications: true,
    emailPromotions: false,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    fetchPreferences();
  }, []);

  const fetchPreferences = async () => {
    try {
      setLoading(true);
      const response = await fetch(`${API_BASE_URL}/notifications/preferences`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
      });

      if (!response.ok) throw new Error('Failed to fetch preferences');

      const data = await response.json();
      setPreferences(data);
    } catch (err) {
      console.error('Error fetching preferences:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleToggle = (key) => {
    setPreferences((prev) => ({
      ...prev,
      [key]: !prev[key],
    }));
    setSuccess(false);
  };

  const handleSave = async () => {
    try {
      setLoading(true);
      setError(null);
      setSuccess(false);

      const response = await fetch(`${API_BASE_URL}/notifications/preferences`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
        body: JSON.stringify(preferences),
      });

      if (!response.ok) throw new Error('Failed to save preferences');

      setSuccess(true);
      setTimeout(() => setSuccess(false), 3000);
    } catch (err) {
      console.error('Error saving preferences:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const preferenceOptions = [
    {
      key: 'emailApplicationStatus',
      label: 'Application Status Updates',
      description: 'Receive emails when your applications are accepted, rejected, or need confirmation',
    },
    {
      key: 'emailPaymentNotifications',
      label: 'Payment Notifications',
      description: 'Receive emails about deposits, payouts, and refunds',
    },
    {
      key: 'emailEventCancellations',
      label: 'Event Cancellations',
      description: 'Receive emails when events you applied to are cancelled',
    },
    {
      key: 'emailDisputeUpdates',
      label: 'Dispute Updates',
      description: 'Receive emails about dispute resolutions and updates',
    },
    {
      key: 'emailRatingNotifications',
      label: 'Rating Notifications',
      description: 'Receive emails when you receive new ratings or reviews',
    },
    {
      key: 'emailPromotions',
      label: 'Promotional Emails',
      description: 'Receive emails about new features, promotions, and platform updates',
    },
  ];

  return (
    <div className="bg-slate-800 rounded-lg border border-slate-700 p-6">
      <h2 className="text-xl font-bold text-white mb-6">Email Notification Preferences</h2>

      {error && (
        <div className="mb-4 p-4 bg-red-500/10 border border-red-500/20 rounded-lg">
          <p className="text-red-400 text-sm">{error}</p>
        </div>
      )}

      {success && (
        <div className="mb-4 p-4 bg-green-500/10 border border-green-500/20 rounded-lg">
          <p className="text-green-400 text-sm">Preferences saved successfully!</p>
        </div>
      )}

      <div className="space-y-4 mb-6">
        {preferenceOptions.map((option) => (
          <div key={option.key} className="flex items-start gap-4 p-4 bg-slate-900 rounded-lg hover:bg-slate-900/80 transition duration-200">
            <div className="flex-1">
              <label className="flex items-center gap-3 cursor-pointer">
                <input
                  type="checkbox"
                  checked={preferences[option.key]}
                  onChange={() => handleToggle(option.key)}
                  disabled={loading}
                  className="w-5 h-5 rounded border-slate-600 text-blue-600 focus:ring-blue-500 cursor-pointer"
                />
                <div>
                  <p className="text-white font-medium">{option.label}</p>
                  <p className="text-slate-400 text-sm">{option.description}</p>
                </div>
              </label>
            </div>
          </div>
        ))}
      </div>

      <div className="flex gap-3">
        <button
          onClick={handleSave}
          disabled={loading}
          className="px-6 py-2 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 transition duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {loading ? 'Saving...' : 'Save Preferences'}
        </button>
        <button
          onClick={fetchPreferences}
          disabled={loading}
          className="px-6 py-2 bg-slate-700 text-slate-300 rounded-lg font-medium hover:bg-slate-600 transition duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Reset
        </button>
      </div>
    </div>
  );
};
