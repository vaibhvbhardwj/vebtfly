import React, { useState } from 'react';
import { Modal } from '../shared/Modal';
import { adminApi } from '../../api/adminApi';

export const BanUserModal = ({ user, onClose, onSuccess }) => {
  const [reason, setReason] = useState('');
  const [confirmed, setConfirmed] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!reason.trim() || !confirmed) {
      setError('Please fill in all fields and confirm');
      return;
    }

    try {
      setLoading(true);
      await adminApi.banUser(user.id, {
        reason: reason.trim(),
      });
      onSuccess();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to ban user');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={true} onClose={onClose} title="Ban User" size="md">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <p className="text-sm text-slate-300 mb-4">
            Permanently banning <span className="font-semibold text-white">{user.fullName}</span>
          </p>
          <div className="p-3 bg-red-900/30 border border-red-700 rounded text-red-200 text-sm">
            ⚠️ This action is permanent and cannot be undone. The user will be unable to access the platform.
          </div>
        </div>

        {error && (
          <div className="p-3 bg-red-900 border border-red-700 rounded text-red-200 text-sm">
            {error}
          </div>
        )}

        <div>
          <label className="block text-sm font-medium text-slate-300 mb-2">
            Reason for Ban
          </label>
          <textarea
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="Enter reason for permanent ban..."
            rows="4"
            className="w-full px-4 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:border-blue-500"
          />
        </div>

        <div className="flex items-center gap-2">
          <input
            type="checkbox"
            id="confirm"
            checked={confirmed}
            onChange={(e) => setConfirmed(e.target.checked)}
            className="w-4 h-4 rounded border-slate-600 bg-slate-700 cursor-pointer"
          />
          <label htmlFor="confirm" className="text-sm text-slate-300 cursor-pointer">
            I confirm this permanent ban
          </label>
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
            disabled={loading || !confirmed}
            className="flex-1 px-4 py-2 bg-red-600 hover:bg-red-700 disabled:opacity-50 rounded-lg font-medium transition"
          >
            {loading ? 'Banning...' : 'Ban User Permanently'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
