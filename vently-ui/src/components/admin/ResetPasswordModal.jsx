import React, { useState } from 'react';
import { Modal } from '../shared/Modal';
import { adminApi } from '../../api/adminApi';

export const ResetPasswordModal = ({ user, onClose }) => {
  const [confirmed, setConfirmed] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [tempPassword, setTempPassword] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!confirmed) {
      setError('Please confirm the password reset');
      return;
    }

    try {
      setLoading(true);
      const response = await adminApi.resetPassword(user.id);
      setTempPassword(response.temporaryPassword || 'Password reset sent to user email');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to reset password');
    } finally {
      setLoading(false);
    }
  };

  if (tempPassword) {
    return (
      <Modal isOpen={true} onClose={onClose} title="Password Reset Successful" size="md">
        <div className="space-y-4">
          <div className="p-4 bg-green-900/30 border border-green-700 rounded text-green-200">
            ✓ Password has been reset successfully
          </div>
          <div>
            <p className="text-sm text-slate-300 mb-2">Temporary Password:</p>
            <div className="p-3 bg-slate-700 border border-slate-600 rounded font-mono text-sm break-all">
              {tempPassword}
            </div>
            <p className="text-xs text-slate-400 mt-2">
              A password reset email has been sent to {user.email}
            </p>
          </div>
          <button
            onClick={onClose}
            className="w-full px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg font-medium transition"
          >
            Close
          </button>
        </div>
      </Modal>
    );
  }

  return (
    <Modal isOpen={true} onClose={onClose} title="Reset Password" size="md">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <p className="text-sm text-slate-300 mb-4">
            Reset password for <span className="font-semibold text-white">{user.fullName}</span>
          </p>
          <div className="p-3 bg-blue-900/30 border border-blue-700 rounded text-blue-200 text-sm">
            ℹ️ A temporary password will be generated and sent to the user's email address.
          </div>
        </div>

        {error && (
          <div className="p-3 bg-red-900 border border-red-700 rounded text-red-200 text-sm">
            {error}
          </div>
        )}

        <div className="flex items-center gap-2">
          <input
            type="checkbox"
            id="confirm"
            checked={confirmed}
            onChange={(e) => setConfirmed(e.target.checked)}
            className="w-4 h-4 rounded border-slate-600 bg-slate-700 cursor-pointer"
          />
          <label htmlFor="confirm" className="text-sm text-slate-300 cursor-pointer">
            I confirm to reset this user's password
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
            className="flex-1 px-4 py-2 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 rounded-lg font-medium transition"
          >
            {loading ? 'Resetting...' : 'Reset Password'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
