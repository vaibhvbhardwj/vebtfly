import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Pagination } from '../../components/shared/Pagination';
import { disputeApi } from '../../api/disputeApi';
import { formatDate } from '../../utils/formatters';

const DISPUTE_STATUSES = {
  OPEN: { label: 'Open', color: 'bg-blue-500/20 border-blue-500/30 text-blue-400' },
  UNDER_REVIEW: { label: 'Under Review', color: 'bg-yellow-500/20 border-yellow-500/30 text-yellow-400' },
  RESOLVED: { label: 'Resolved', color: 'bg-green-500/20 border-green-500/30 text-green-400' },
  CLOSED: { label: 'Closed', color: 'bg-slate-500/20 border-slate-500/30 text-slate-400' },
};

const DisputeList = () => {
  const navigate = useNavigate();
  const [disputes, setDisputes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedStatus, setSelectedStatus] = useState(null);

  useEffect(() => {
    fetchDisputes(0);
  }, [selectedStatus]);

  const fetchDisputes = async (page) => {
    setLoading(true);
    setError(null);

    try {
      const response = await disputeApi.getMyDisputes(page, 10, selectedStatus);
      setDisputes(response.content || []);
      setCurrentPage(response.number || 0);
      setTotalPages(response.totalPages || 0);
    } catch (err) {
      setError('Failed to load disputes');
      console.error('Error fetching disputes:', err);
    } finally {
      setLoading(false);
    }
  };

  const handlePageChange = (page) => {
    fetchDisputes(page);
  };

  const getStatusBadge = (status) => {
    const statusInfo = DISPUTE_STATUSES[status] || DISPUTE_STATUSES.OPEN;
    return statusInfo;
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
      {/* Background decorative elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-20 right-0 w-96 h-96 bg-red-500 rounded-full mix-blend-multiply filter blur-3xl opacity-10 animate-pulse"></div>
        <div className="absolute bottom-0 left-0 w-96 h-96 bg-orange-500 rounded-full mix-blend-multiply filter blur-3xl opacity-10 animate-pulse"></div>
      </div>

      <div className="relative max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-4xl font-bold text-white mb-2">My Disputes</h1>
            <p className="text-slate-400">
              View and manage your disputes
            </p>
          </div>
          <button
            onClick={() => navigate('/disputes/new')}
            className="px-6 py-2 bg-gradient-to-r from-red-500 to-orange-600 hover:from-red-600 hover:to-orange-700 text-white font-medium rounded-lg transition"
          >
            New Dispute
          </button>
        </div>

        {/* Status Filter */}
        <div className="mb-6 flex gap-2 flex-wrap">
          <button
            onClick={() => setSelectedStatus(null)}
            className={`px-4 py-2 rounded-lg font-medium transition ${
              selectedStatus === null
                ? 'bg-blue-500 text-white'
                : 'bg-slate-700 text-slate-300 hover:bg-slate-600'
            }`}
          >
            All
          </button>
          {Object.entries(DISPUTE_STATUSES).map(([status, info]) => (
            <button
              key={status}
              onClick={() => setSelectedStatus(status)}
              className={`px-4 py-2 rounded-lg font-medium transition ${
                selectedStatus === status
                  ? 'bg-blue-500 text-white'
                  : 'bg-slate-700 text-slate-300 hover:bg-slate-600'
              }`}
            >
              {info.label}
            </button>
          ))}
        </div>

        {/* Error Message */}
        {error && (
          <div className="p-4 bg-red-500/10 border border-red-500/30 rounded-lg mb-6">
            <p className="text-sm text-red-400">{error}</p>
          </div>
        )}

        {/* Loading State */}
        {loading && (
          <div className="flex justify-center py-16">
            <svg className="animate-spin h-12 w-12 text-blue-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
          </div>
        )}

        {/* Empty State */}
        {!loading && disputes.length === 0 && (
          <div className="text-center py-16">
            <svg className="w-16 h-16 text-slate-600 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <h3 className="text-xl font-bold text-white mb-2">No disputes yet</h3>
            <p className="text-slate-400 mb-6">
              {selectedStatus ? 'No disputes with this status' : 'You haven\'t submitted any disputes'}
            </p>
            {!selectedStatus && (
              <button
                onClick={() => navigate('/disputes/new')}
                className="px-6 py-2 bg-gradient-to-r from-red-500 to-orange-600 hover:from-red-600 hover:to-orange-700 text-white font-medium rounded-lg transition"
              >
                Submit a Dispute
              </button>
            )}
          </div>
        )}

        {/* Disputes List */}
        {!loading && disputes.length > 0 && (
          <div className="space-y-4">
            {disputes.map((dispute) => {
              const statusInfo = getStatusBadge(dispute.status);
              return (
                <div
                  key={dispute.id}
                  onClick={() => navigate(`/disputes/${dispute.id}`)}
                  className="bg-slate-800 border border-slate-700 rounded-lg p-6 hover:border-blue-500/50 transition cursor-pointer"
                >
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex-1">
                      <h3 className="text-lg font-bold text-white mb-1">
                        {dispute.event?.title}
                      </h3>
                      <p className="text-sm text-slate-400">
                        Submitted on {formatDate(dispute.createdAt, 'short')}
                      </p>
                    </div>
                    <div className={`px-3 py-1 rounded-full border text-sm font-medium ${statusInfo.color}`}>
                      {statusInfo.label}
                    </div>
                  </div>

                  <p className="text-slate-300 mb-4 line-clamp-2">
                    {dispute.description}
                  </p>

                  <div className="flex items-center justify-between">
                    <div className="flex gap-4 text-sm text-slate-400">
                      {dispute.evidenceUrls && dispute.evidenceUrls.length > 0 && (
                        <div className="flex items-center gap-1">
                          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                          </svg>
                          <span>{dispute.evidenceUrls.length} file{dispute.evidenceUrls.length !== 1 ? 's' : ''}</span>
                        </div>
                      )}
                    </div>
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        navigate(`/disputes/${dispute.id}`);
                      }}
                      className="px-4 py-2 bg-blue-500/20 border border-blue-500/30 text-blue-400 hover:bg-blue-500/30 font-medium rounded-lg transition"
                    >
                      View Details
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {/* Pagination */}
        {!loading && totalPages > 1 && (
          <div className="mt-8">
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={handlePageChange}
            />
          </div>
        )}
      </div>
    </div>
  );
};


export default DisputeList;