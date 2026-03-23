import React, { useState } from 'react';
import { Modal } from '../shared/Modal';
import { adminApi } from '../../api/adminApi';

export const DisputeResolutionModal = ({ dispute, onClose, onSuccess }) => {
  const [decision, setDecision] = useState('');
  const [notes, setNotes] = useState('');
  const [paymentAdjustment, setPaymentAdjustment] = useState('0');
  const [penaltyAdjustment, setPenaltyAdjustment] = useState('0');
  const [confirmed, setConfirmed] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!decision || !notes.trim() || !confirmed) {
      setError('Please fill in all fields and confirm');
      return;
    }

    try {
      setLoading(true);
      await adminApi.resolveDispute(dispute.id, {
        decision,
        resolutionNotes: notes.trim(),
        paymentAdjustment: parseFloat(paymentAdjustment),
        penaltyAdjustment: parseInt(penaltyAdjustment),
      });
      onSuccess();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to resolve dispute');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <Modal isOpen={true} onClose={onClose} title="Resolve Dispute" size="2xl">
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Dispute Details */}
        <div className="bg-slate-700 rounded-lg p-4 space-y-3">
          <h3 className="font-semibold text-white mb-3">Dispute Details</h3>
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <p className="text-slate-400">Dispute ID</p>
              <p className="text-white font-mono">#{dispute.id}</p>
            </div>
            <div>
              <p className="text-slate-400">Status</p>
              <p className="text-white">{dispute.status}</p>
            </div>
            <div>
              <p className="text-slate-400">Raised By</p>
              <p className="text-white">{dispute.raisedByName}</p>
            </div>
            <div>
              <p className="text-slate-400">Against</p>
              <p className="text-white">{dispute.againstUserName}</p>
            </div>
            <div>
              <p className="text-slate-400">Event</p>
              <p className="text-white">{dispute.eventTitle || 'N/A'}</p>
            </div>
            <div>
              <p className="text-slate-400">Created</p>
              <p className="text-white text-xs">{formatDate(dispute.createdAt)}</p>
            </div>
          </div>
          <div>
            <p className="text-slate-400 text-sm mb-1">Description</p>
            <p className="text-white text-sm bg-slate-800 p-2 rounded">{dispute.description}</p>
          </div>
          {dispute.evidenceUrls && dispute.evidenceUrls.length > 0 && (
            <div>
              <p className="text-slate-400 text-sm mb-2">Evidence Files ({dispute.evidenceUrls.length})</p>
              <div className="flex flex-wrap gap-2">
                {dispute.evidenceUrls.map((url, idx) => (
                  <a
                    key={idx}
                    href={url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-blue-400 hover:text-blue-300 text-sm underline"
                  >
                    File {idx + 1}
                  </a>
                ))}
              </div>
            </div>
          )}
        </div>

        {error && (
          <div className="p-3 bg-red-900 border border-red-700 rounded text-red-200 text-sm">
            {error}
          </div>
        )}

        {/* Resolution Form */}
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-300 mb-2">
              Resolution Decision
            </label>
            <select
              value={decision}
              onChange={(e) => setDecision(e.target.value)}
              className="w-full px-4 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white focus:outline-none focus:border-blue-500"
            >
              <option value="">Select decision...</option>
              <option value="FAVOR_RAISED_BY">Favor Dispute Raiser</option>
              <option value="FAVOR_AGAINST_USER">Favor Against User</option>
              <option value="PARTIAL_RESOLUTION">Partial Resolution</option>
              <option value="DISMISSED">Dismiss Dispute</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-300 mb-2">
              Resolution Notes
            </label>
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Explain the resolution decision..."
              rows="4"
              className="w-full px-4 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:border-blue-500"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">
                Payment Adjustment ($)
              </label>
              <input
                type="number"
                step="0.01"
                value={paymentAdjustment}
                onChange={(e) => setPaymentAdjustment(e.target.value)}
                placeholder="0.00"
                className="w-full px-4 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:border-blue-500"
              />
              <p className="text-xs text-slate-400 mt-1">Positive = refund, Negative = charge</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">
                Penalty Adjustment (No-Shows)
              </label>
              <input
                type="number"
                value={penaltyAdjustment}
                onChange={(e) => setPenaltyAdjustment(e.target.value)}
                placeholder="0"
                className="w-full px-4 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:border-blue-500"
              />
              <p className="text-xs text-slate-400 mt-1">Positive = add, Negative = remove</p>
            </div>
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
              I confirm this resolution
            </label>
          </div>
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
            className="flex-1 px-4 py-2 bg-green-600 hover:bg-green-700 disabled:opacity-50 rounded-lg font-medium transition"
          >
            {loading ? 'Resolving...' : 'Resolve Dispute'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
