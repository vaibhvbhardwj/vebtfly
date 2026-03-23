import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Modal } from '../../components/shared/Modal';
import { formatDate, formatCurrency, formatApplicationStatus, getApplicationStatusColor, formatCountdown } from '../../utils/formatters';
import { APPLICATION_STATUS, API_BASE_URL } from '../../utils/constants';

const MyApplications = () => {
  const navigate = useNavigate();
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedApplication, setSelectedApplication] = useState(null);
  const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
  const [isWithdrawModalOpen, setIsWithdrawModalOpen] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);

  useEffect(() => { fetchApplications(); }, []);

  const fetchApplications = async () => {
    setLoading(true); setError('');
    try {
      const response = await fetch(`${API_BASE_URL}/applications/my-applications`, {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
      });
      if (!response.ok) throw new Error('Failed to fetch applications');
      const data = await response.json();
      const appList = data.content || (Array.isArray(data) ? data : []);
      setApplications(appList);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmApplication = async () => {
    if (!selectedApplication) return;
    setIsProcessing(true);
    try {
      const response = await fetch(`${API_BASE_URL}/applications/${selectedApplication.id}/confirm`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
      });
      if (!response.ok) throw new Error('Failed to confirm application');
      setIsConfirmModalOpen(false);
      fetchApplications();
    } catch (err) {
      setError(err.message);
    } finally {
      setIsProcessing(false);
    }
  };

  const handleWithdrawApplication = async () => {
    if (!selectedApplication) return;
    setIsProcessing(true);
    try {
      const response = await fetch(`${API_BASE_URL}/applications/${selectedApplication.id}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
      });
      if (!response.ok) throw new Error('Failed to withdraw application');
      setIsWithdrawModalOpen(false);
      fetchApplications();
    } catch (err) {
      setError(err.message);
    } finally {
      setIsProcessing(false);
    }
  };

  const groupedApplications = {
    [APPLICATION_STATUS.PENDING]: applications.filter((a) => a.status === APPLICATION_STATUS.PENDING),
    [APPLICATION_STATUS.ACCEPTED]: applications.filter((a) => a.status === APPLICATION_STATUS.ACCEPTED),
    [APPLICATION_STATUS.CONFIRMED]: applications.filter((a) => a.status === APPLICATION_STATUS.CONFIRMED),
    [APPLICATION_STATUS.REJECTED]: applications.filter((a) => a.status === APPLICATION_STATUS.REJECTED),
    [APPLICATION_STATUS.DECLINED]: applications.filter((a) => a.status === APPLICATION_STATUS.DECLINED),
  };

  const statusDotColor = (status) => {
    const map = {
      PENDING: 'bg-yellow-400',
      ACCEPTED: 'bg-[#807aeb]',
      CONFIRMED: 'bg-[#10B981]',
      REJECTED: 'bg-[#EF4444]',
      DECLINED: 'bg-[#6B7280]',
    };
    return map[status] || 'bg-[#6B7280]';
  };

  const ApplicationRow = ({ application }) => {
    const isAccepted = application.status === APPLICATION_STATUS.ACCEPTED;
    const isPending = application.status === APPLICATION_STATUS.PENDING;

    return (
      <div className="bg-white rounded-2xl border border-[#807aeb]/10 p-5 shadow-sm card-hover">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div className="flex-1 min-w-0">
            <h3 className="text-base font-semibold text-[#111827] truncate hover:text-[#807aeb] cursor-pointer transition"
              onClick={() => navigate(`/events/${application.eventId}`)}>
              {application.eventTitle}
            </h3>
            <div className="flex flex-wrap items-center gap-3 mt-2 text-sm text-[#6B7280]">
              <span className="flex items-center gap-1">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
                {formatDate(application.eventDateTime, 'short')}
              </span>
              <span className="flex items-center gap-1">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                {application.eventLocation}
              </span>
            </div>
          </div>

          <div className="flex flex-col sm:items-end gap-2">
            <span className="px-3 py-1 bg-[#807aeb]/10 text-[#807aeb] text-xs font-semibold rounded-full border border-[#807aeb]/20 w-fit">
              {formatApplicationStatus(application.status)}
            </span>

            {isAccepted && application.acceptedAt && (
              <p className="text-xs text-yellow-600 font-medium">
                Confirm by: {formatCountdown(new Date(new Date(application.acceptedAt).getTime() + 48 * 60 * 60 * 1000))}
              </p>
            )}

            <div className="flex gap-2">
              {isAccepted && (
                <>
                  <button onClick={() => { setSelectedApplication(application); setIsConfirmModalOpen(true); }}
                    className="px-3 py-1.5 bg-[#10B981]/10 text-[#10B981] text-xs font-medium rounded-lg hover:bg-[#10B981]/20 transition">
                    Confirm
                  </button>
                  <button onClick={() => { setSelectedApplication(application); setIsWithdrawModalOpen(true); }}
                    className="px-3 py-1.5 bg-red-50 text-[#EF4444] text-xs font-medium rounded-lg hover:bg-red-100 transition">
                    Decline
                  </button>
                </>
              )}
              {isPending && (
                <button onClick={() => { setSelectedApplication(application); setIsWithdrawModalOpen(true); }}
                  className="px-3 py-1.5 bg-red-50 text-[#EF4444] text-xs font-medium rounded-lg hover:bg-red-100 transition">
                  Withdraw
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="min-h-screen bg-[#ebf2fa] animate-fade-in">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-[#111827] mb-1">My Applications</h1>
          <p className="text-[#6B7280]">Track your event applications and confirmations</p>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-xl flex items-center justify-between">
            <p className="text-[#EF4444] text-sm">{error}</p>
            <button onClick={() => setError('')} className="text-[#EF4444] hover:opacity-70 ml-4">✕</button>
          </div>
        )}

        {loading && (
          <div className="flex items-center justify-center py-16">
            <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-[#807aeb]" />
          </div>
        )}

        {/* Summary strip */}
        {!loading && applications.length > 0 && (
          <div className="mb-6 p-4 bg-white rounded-2xl border border-[#807aeb]/10 shadow-sm flex flex-wrap gap-4 text-sm">
            <span className="text-[#6B7280]">Total: <span className="font-semibold text-[#111827]">{applications.length}</span></span>
            <span className="text-[#6B7280]">Pending: <span className="font-semibold text-[#111827]">{groupedApplications[APPLICATION_STATUS.PENDING]?.length || 0}</span></span>
            <span className="text-[#6B7280]">Accepted: <span className="font-semibold text-[#111827]">{groupedApplications[APPLICATION_STATUS.ACCEPTED]?.length || 0}</span></span>
            <span className="text-[#6B7280]">Confirmed: <span className="font-semibold text-[#111827]">{groupedApplications[APPLICATION_STATUS.CONFIRMED]?.length || 0}</span></span>
          </div>
        )}

        {!loading && applications.length > 0 && (
          <div className="space-y-8">
            {Object.entries(groupedApplications).map(([status, apps]) =>
              apps.length > 0 && (
                <div key={status}>
                  <h2 className="text-base font-semibold text-[#111827] mb-3 flex items-center gap-2">
                    <span className={`w-2.5 h-2.5 rounded-full ${statusDotColor(status)}`} />
                    {formatApplicationStatus(status)} ({apps.length})
                  </h2>
                  <div className="space-y-3">
                    {apps.map((application) => (
                      <ApplicationRow key={application.id} application={application} />
                    ))}
                  </div>
                </div>
              )
            )}
          </div>
        )}

        {!loading && applications.length === 0 && (
          <div className="text-center py-16">
            <div className="text-5xl mb-4">📋</div>
            <h3 className="text-xl font-semibold text-[#111827] mb-2">No applications yet</h3>
            <p className="text-[#6B7280] mb-6">Start exploring events and apply to opportunities</p>
            <button onClick={() => navigate('/events')}
              className="px-6 py-2 bg-[#807aeb] text-white font-medium rounded-xl hover:bg-[#6b64d4] transition">
              Browse Events
            </button>
          </div>
        )}
      </div>

      <Modal isOpen={isConfirmModalOpen} onClose={() => setIsConfirmModalOpen(false)} title="Confirm Application" size="md">
        <div className="space-y-4">
          <p className="text-[#6B7280]">
            Confirm your participation in <span className="font-semibold text-[#111827]">{selectedApplication?.eventTitle}</span>?
          </p>
          <p className="text-sm text-[#6B7280]">This locks in your spot for the event.</p>
          <div className="flex gap-3 pt-2">
            <button onClick={() => setIsConfirmModalOpen(false)}
              className="flex-1 py-2 bg-[#ebf2fa] text-[#111827] font-medium rounded-xl hover:bg-gray-200 transition">
              Cancel
            </button>
            <button onClick={handleConfirmApplication} disabled={isProcessing}
              className="flex-1 py-2 bg-[#10B981] text-white font-medium rounded-xl hover:bg-[#059669] transition disabled:opacity-50">
              {isProcessing ? 'Confirming...' : 'Confirm'}
            </button>
          </div>
        </div>
      </Modal>

      <Modal isOpen={isWithdrawModalOpen} onClose={() => setIsWithdrawModalOpen(false)} title="Withdraw Application" size="md">
        <div className="space-y-4">
          <p className="text-[#6B7280]">
            Withdraw from <span className="font-semibold text-[#111827]">{selectedApplication?.eventTitle}</span>?
          </p>
          <p className="text-sm text-[#6B7280]">This action cannot be undone. The organizer will be notified.</p>
          <div className="flex gap-3 pt-2">
            <button onClick={() => setIsWithdrawModalOpen(false)}
              className="flex-1 py-2 bg-[#ebf2fa] text-[#111827] font-medium rounded-xl hover:bg-gray-200 transition">
              Cancel
            </button>
            <button onClick={handleWithdrawApplication} disabled={isProcessing}
              className="flex-1 py-2 bg-[#EF4444] text-white font-medium rounded-xl hover:bg-red-600 transition disabled:opacity-50">
              {isProcessing ? 'Withdrawing...' : 'Withdraw'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default MyApplications;
