import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { disputeApi } from '../../api/disputeApi';
import { formatDate } from '../../utils/formatters';

const DISPUTE_STATUSES = {
  OPEN: { label: 'Open', color: 'bg-blue-500/20 border-blue-500/30 text-blue-400' },
  UNDER_REVIEW: { label: 'Under Review', color: 'bg-yellow-500/20 border-yellow-500/30 text-yellow-400' },
  RESOLVED: { label: 'Resolved', color: 'bg-green-500/20 border-green-500/30 text-green-400' },
  CLOSED: { label: 'Closed', color: 'bg-slate-500/20 border-slate-500/30 text-slate-400' },
};

const DisputeDetails = () => {
  const { disputeId } = useParams();
  const navigate = useNavigate();
  const [dispute, setDispute] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchDisputeDetails();
  }, [disputeId]);

  const fetchDisputeDetails = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await disputeApi.getDisputeDetails(disputeId);
      setDispute(response);
    } catch (err) {
      setError('Failed to load dispute details');
      console.error('Error fetching dispute:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
        <div className="flex items-center justify-center py-32">
          <svg className="animate-spin h-12 w-12 text-blue-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
        </div>
      </div>
    );
  }

  if (error || !dispute) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
        <div className="max-w-4xl mx-auto px-4 py-16 text-center">
          <h1 className="text-3xl font-bold text-white mb-4">Dispute not found</h1>
          <p className="text-slate-400 mb-6">{error || 'The dispute you are looking for does not exist.'}</p>
          <button
            onClick={() => navigate('/disputes')}
            className="px-6 py-2 bg-gradient-to-r from-blue-500 to-indigo-600 text-white font-medium rounded-lg hover:from-blue-600 hover:to-indigo-700 transition"
          >
            Back to Disputes
          </button>
        </div>
      </div>
    );
  }

  const statusInfo = DISPUTE_STATUSES[dispute.status] || DISPUTE_STATUSES.OPEN;

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
      {/* Background decorative elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-20 right-0 w-96 h-96 bg-red-500 rounded-full mix-blend-multiply filter blur-3xl opacity-10 animate-pulse"></div>
        <div className="absolute bottom-0 left-0 w-96 h-96 bg-orange-500 rounded-full mix-blend-multiply filter blur-3xl opacity-10 animate-pulse"></div>
      </div>

      <div className="relative max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Back Button */}
        <button
          onClick={() => navigate('/disputes')}
          className="flex items-center gap-2 text-blue-400 hover:text-blue-300 transition mb-6"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          Back to Disputes
        </button>

        {/* Header */}
        <div className="bg-slate-800 border border-slate-700 rounded-2xl p-8 mb-8">
          <div className="flex items-start justify-between mb-6">
            <div>
              <h1 className="text-3xl font-bold text-white mb-2">
                {dispute.event?.title}
              </h1>
              <p className="text-slate-400">
                Dispute ID: #{dispute.id}
              </p>
            </div>
            <div className={`px-4 py-2 rounded-lg border text-sm font-medium ${statusInfo.color}`}>
              {statusInfo.label}
            </div>
          </div>

          {/* Timeline */}
          <div className="space-y-4 text-sm">
            <div className="flex items-center gap-4">
              <div className="flex-shrink-0">
                <svg className="w-5 h-5 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
              <div>
                <p className="text-slate-400">Submitted</p>
                <p className="text-white font-semibold">{formatDate(dispute.createdAt, 'long')}</p>
              </div>
            </div>

            {dispute.resolvedAt && (
              <div className="flex items-center gap-4">
                <div className="flex-shrink-0">
                  <svg className="w-5 h-5 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div>
                  <p className="text-slate-400">Resolved</p>
                  <p className="text-white font-semibold">{formatDate(dispute.resolvedAt, 'long')}</p>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Content Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-8">
            {/* Description */}
            <div className="bg-slate-800 border border-slate-700 rounded-2xl p-8">
              <h2 className="text-xl font-bold text-white mb-4">Description</h2>
              <p className="text-slate-300 leading-relaxed whitespace-pre-wrap">
                {dispute.description}
              </p>
            </div>

            {/* Evidence */}
            {dispute.evidenceUrls && dispute.evidenceUrls.length > 0 && (
              <div className="bg-slate-800 border border-slate-700 rounded-2xl p-8">
                <h2 className="text-xl font-bold text-white mb-4">Evidence</h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {dispute.evidenceUrls.map((url, index) => {
                    const isImage = /\.(jpg|jpeg|png|gif|webp)$/i.test(url);
                    return (
                      <div key={index} className="bg-slate-700/50 rounded-lg overflow-hidden border border-slate-600">
                        {isImage ? (
                          <a href={url} target="_blank" rel="noopener noreferrer" className="block">
                            <img
                              src={url}
                              alt={`Evidence ${index + 1}`}
                              className="w-full h-48 object-cover hover:opacity-80 transition"
                            />
                          </a>
                        ) : (
                          <a
                            href={url}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="flex items-center justify-center h-48 hover:bg-slate-600 transition"
                          >
                            <div className="text-center">
                              <svg className="w-12 h-12 text-blue-400 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                              </svg>
                              <p className="text-sm text-blue-400 font-medium">View Document</p>
                            </div>
                          </a>
                        )}
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Resolution */}
            {dispute.resolution && (
              <div className="bg-slate-800 border border-slate-700 rounded-2xl p-8">
                <h2 className="text-xl font-bold text-white mb-4">Resolution</h2>
                <div className="p-4 bg-green-500/10 border border-green-500/30 rounded-lg">
                  <p className="text-slate-300 leading-relaxed whitespace-pre-wrap">
                    {dispute.resolution}
                  </p>
                </div>
              </div>
            )}
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Event Info */}
            <div className="bg-slate-800 border border-slate-700 rounded-2xl p-6">
              <h3 className="text-lg font-bold text-white mb-4">Event Details</h3>
              <div className="space-y-3">
                <div>
                  <p className="text-xs text-slate-400 mb-1">Title</p>
                  <p className="text-white font-semibold">{dispute.event?.title}</p>
                </div>
                <div>
                  <p className="text-xs text-slate-400 mb-1">Date</p>
                  <p className="text-white">{formatDate(dispute.event?.date, 'short')}</p>
                </div>
                <div>
                  <p className="text-xs text-slate-400 mb-1">Location</p>
                  <p className="text-white">{dispute.event?.location}</p>
                </div>
              </div>
            </div>

            {/* Status Info */}
            <div className="bg-slate-800 border border-slate-700 rounded-2xl p-6">
              <h3 className="text-lg font-bold text-white mb-4">Status</h3>
              <div className={`px-4 py-2 rounded-lg border text-center font-medium ${statusInfo.color}`}>
                {statusInfo.label}
              </div>
              <p className="text-xs text-slate-400 mt-3">
                {dispute.status === 'OPEN' && 'Your dispute is awaiting review by our team.'}
                {dispute.status === 'UNDER_REVIEW' && 'Our team is currently reviewing your dispute.'}
                {dispute.status === 'RESOLVED' && 'Your dispute has been resolved.'}
                {dispute.status === 'CLOSED' && 'Your dispute has been closed.'}
              </p>
            </div>

            {/* Dispute Info */}
            <div className="bg-slate-800 border border-slate-700 rounded-2xl p-6">
              <h3 className="text-lg font-bold text-white mb-4">Information</h3>
              <div className="space-y-3 text-sm">
                <div>
                  <p className="text-slate-400">Dispute ID</p>
                  <p className="text-white font-mono">#{dispute.id}</p>
                </div>
                <div>
                  <p className="text-slate-400">Submitted</p>
                  <p className="text-white">{formatDate(dispute.createdAt, 'short')}</p>
                </div>
                {dispute.evidenceUrls && (
                  <div>
                    <p className="text-slate-400">Evidence Files</p>
                    <p className="text-white">{dispute.evidenceUrls.length} file{dispute.evidenceUrls.length !== 1 ? 's' : ''}</p>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};


export default DisputeDetails;