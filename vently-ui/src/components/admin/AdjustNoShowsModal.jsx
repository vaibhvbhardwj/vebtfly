import React, { useState } from 'react';
import { Modal } from '../shared/Modal';
import { adminApi } from '../../api/adminApi';

export const AdjustNoShowsModal = ({ user, onClose, onSuccess }) => {
  const [adjustment, setAdjustment] = useState('0');
  const [reason, setReason] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const newTotal = (user.noShowCount || 0) + parseInt(adjustment);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!reason.trim()) {
      setError('Please provide a reason for adjustment');
      return;
    }

    try {
      setLoading(true);
      await adminApi.adjustNoShows(user.id, {
        newCount: newTotal,
        reason: reason.trim(),
      });
      onSuccess();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to adjust no-shows');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={true} onClose={onClose} title="Adjust No-Shows" size="md">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <p className="text-sm text-slate-300 mb-4">
            Adjusting no-shows for <span className="font-semibold text-white">{user.fullName}</span>
          </p>
          <div className="p-3 bg-slate-700 rounded text-sm">
            <p className="text-slate-300">Current No-Shows: <span className="font-semibold text-white">{user.noShowCount || 0}</span></p>
            <p className="text-slate-300 mt-1">New Total: <span className="font-semibold text-white">{newTotal}</span></p>
          </div>
        </div>

        {error && (
          <div className="p-3 bg-red-900 border border-red-700 rounded text-red-200 text-sm">
            {error}
          </div>
        )}

        {newTotal >= 3 && newTotal < 5 && (
          <div className="p-3 bg-yellow-900/30 border border-yellow-700 rounded text-yellow-200 text-sm">
            ⚠️ User will be suspended at 3 no-shows
          </div>
        )}

        {newTotal >= 5 && (
          <div className="p-3 bg-red-900/30 border border-red-700 rounded text-red-200 text-sm">
            ⚠️ User will be permanently banned at 5 no-shows
          </div>
        )}

        <div>
          <label className="block text-sm font-medium text-slate-300 mb-2">
            Adjustment (can be negative)
          </label>
          <input
            type="number"
            value={adjustment}
            onChange={(e) => setAdjustment(e.target.value)}
            placeholder="Enter adjustment amount"
            className="w-full px-4 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:border-blue-500"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-300 mb-2">
            Reason for Adjustment
          </label>
          <textarea
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="Enter reason for adjustment (e.g., dispute resolution)..."
            rows="3"
            className="w-full px-4 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:border-blue-500"
          />
        </div>

        <div className="flex gap-3 pt-4">
          <button
            type="button"
            onClick={onClose}
            className="flex-1 px-4 py-2 bg-slate-700 hover:bg-slate-600 rounded-lg font-medium transition"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={loading}
            className="flex-1 px-4 py-2 bg-purple-600 hover:bg-purple-700 disabled:opacity-50 rounded-lg font-medium transition"
          >
            {loading ? 'Adjusting...' : 'Adjust No-Shows'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
