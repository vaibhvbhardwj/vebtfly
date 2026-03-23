import React, { useState, useEffect } from 'react';
import { adminApi } from '../../api/adminApi';
import { Pagination } from '../../components/shared/Pagination';
import { DisputeResolutionModal } from '../../components/admin/DisputeResolutionModal';

const STATUS_STYLES = {
  OPEN:         { background: '#EDE9FE', color: '#7C3AED' },
  UNDER_REVIEW: { background: '#FEF3C7', color: '#D97706' },
  RESOLVED:     { background: '#D1FAE5', color: '#059669' },
  CLOSED:       { background: '#F3F4F6', color: '#6B7280' },
};

const DisputeManagement = () => {
  const [disputes, setDisputes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [statusFilter, setStatusFilter] = useState('');
  const [selectedDispute, setSelectedDispute] = useState(null);
  const [showResolutionModal, setShowResolutionModal] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const PAGE_SIZE = 10;

  useEffect(() => { fetchDisputes(); }, [currentPage, statusFilter]);

  const fetchDisputes = async () => {
    try {
      setLoading(true);
      const filters = {};
      if (statusFilter) filters.status = statusFilter;
      const response = await adminApi.getAllDisputes(currentPage, PAGE_SIZE, filters);
      setDisputes(response.content || []);
      setTotalPages(response.totalPages || 1);
    } catch {
      setErrorMessage('Failed to fetch disputes');
    } finally {
      setLoading(false);
    }
  };

  const handleViewDetails = async (dispute) => {
    try {
      const details = await adminApi.getDisputeDetails(dispute.id);
      setSelectedDispute(details);
      setShowResolutionModal(true);
    } catch {
      setErrorMessage('Failed to fetch dispute details');
    }
  };

  const handleResolutionSuccess = () => {
    setShowResolutionModal(false);
    setSelectedDispute(null);
    setSuccessMessage('Dispute resolved successfully');
    fetchDisputes();
    setTimeout(() => setSuccessMessage(''), 3000);
  };

  const formatDate = (d) => new Date(d).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });

  const statusBadge = (status) => (
    <span className="px-2 py-1 rounded-full text-xs font-medium" style={STATUS_STYLES[status] || { background: '#F3F4F6', color: '#6B7280' }}>
      {status}
    </span>
  );

  return (
    <div className="min-h-screen p-6 pb-24" style={{ background: '#ebf2fa' }}>
      <div className="max-w-7xl mx-auto">

        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold mb-1" style={{ color: '#111827' }}>Dispute Management</h1>
          <p style={{ color: '#6B7280' }}>Review and resolve user disputes</p>
        </div>

        {successMessage && (
          <div className="mb-4 p-4 rounded-xl border text-sm" style={{ background: '#D1FAE5', borderColor: '#059669', color: '#059669' }}>
            {successMessage}
          </div>
        )}
        {errorMessage && (
          <div className="mb-4 p-4 rounded-xl border text-sm" style={{ background: '#FEE2E2', borderColor: '#EF4444', color: '#EF4444' }}>
            {errorMessage}
          </div>
        )}

        {/* Filter */}
        <div className="rounded-2xl p-6 shadow-sm mb-6" style={{ background: '#fff', border: '1.5px solid #e5e7eb' }}>
          <div className="flex gap-4 items-end">
            <div className="w-64">
              <label className="block text-sm font-medium mb-2" style={{ color: '#6B7280' }}>Status Filter</label>
              <select value={statusFilter} onChange={(e) => { setStatusFilter(e.target.value); setCurrentPage(1); }}
                className="w-full px-4 py-2 rounded-xl border text-sm outline-none"
                style={{ borderColor: '#e5e7eb', color: '#111827', background: '#fff' }}>
                <option value="">All Disputes</option>
                <option value="OPEN">Open</option>
                <option value="UNDER_REVIEW">Under Review</option>
                <option value="RESOLVED">Resolved</option>
                <option value="CLOSED">Closed</option>
              </select>
            </div>
          </div>
        </div>

        {/* Table */}
        <div className="rounded-2xl shadow-sm overflow-hidden" style={{ background: '#fff', border: '1.5px solid #e5e7eb' }}>
          {loading ? (
            <div className="p-8 text-center" style={{ color: '#6B7280' }}>Loading disputes...</div>
          ) : disputes.length === 0 ? (
            <div className="p-8 text-center" style={{ color: '#6B7280' }}>No disputes found</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead style={{ background: '#F9FAFB', borderBottom: '1px solid #e5e7eb' }}>
                  <tr>
                    {['ID', 'Event', 'Raised By', 'Against', 'Status', 'Created', 'Actions'].map((h) => (
                      <th key={h} className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wide" style={{ color: '#6B7280' }}>{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {disputes.map((dispute) => (
                    <tr key={dispute.id} style={{ borderBottom: '1px solid #f3f4f6' }}
                      onMouseEnter={(e) => e.currentTarget.style.background = '#F9FAFB'}
                      onMouseLeave={(e) => e.currentTarget.style.background = 'transparent'}>
                      <td className="px-6 py-4 text-sm font-mono" style={{ color: '#6B7280' }}>#{dispute.id}</td>
                      <td className="px-6 py-4 text-sm" style={{ color: '#111827' }}>{dispute.eventTitle || 'N/A'}</td>
                      <td className="px-6 py-4 text-sm" style={{ color: '#111827' }}>{dispute.raisedByName}</td>
                      <td className="px-6 py-4 text-sm" style={{ color: '#111827' }}>{dispute.againstUserName}</td>
                      <td className="px-6 py-4 text-sm">{statusBadge(dispute.status)}</td>
                      <td className="px-6 py-4 text-sm" style={{ color: '#6B7280' }}>{formatDate(dispute.createdAt)}</td>
                      <td className="px-6 py-4 text-sm">
                        <button onClick={() => handleViewDetails(dispute)}
                          className="px-4 py-2 rounded-xl text-sm font-medium text-white transition hover:opacity-90"
                          style={{ background: '#807aeb' }}>
                          View Details
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        <Pagination currentPage={currentPage} totalPages={totalPages} onPageChange={setCurrentPage} loading={loading} />
      </div>

      {showResolutionModal && selectedDispute && (
        <DisputeResolutionModal
          dispute={selectedDispute}
          onClose={() => { setShowResolutionModal(false); setSelectedDispute(null); }}
          onSuccess={handleResolutionSuccess}
        />
      )}
    </div>
  );
};

export default DisputeManagement;
