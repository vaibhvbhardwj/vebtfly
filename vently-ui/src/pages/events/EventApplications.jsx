import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Modal } from '../../components/shared/Modal';
import { RatingStars } from '../../components/shared/RatingStars';
import { formatDate, formatApplicationStatus } from '../../utils/formatters';
import { APPLICATION_STATUS, API_BASE_URL } from '../../utils/constants';
import { toast } from '../../components/Toast';

const EventApplications = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [event, setEvent] = useState(null);
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedApplication, setSelectedApplication] = useState(null);
  const [actionModalOpen, setActionModalOpen] = useState(false);
  const [actionType, setActionType] = useState(null);
  const [isProcessing, setIsProcessing] = useState(false);
  const [filterStatus, setFilterStatus] = useState('ALL');

  const [attendanceBlockedModal, setAttendanceBlockedModal] = useState(false);
  const [profilePopup, setProfilePopup] = useState(null); // applicant profile popup

  // Gender helpers — case-insensitive
  const genderBorder = (gender) => {
    const g = gender?.toUpperCase();
    if (g === 'MALE') return 'border-l-4 border-l-blue-400';
    if (g === 'FEMALE') return 'border-l-4 border-l-pink-400';
    return 'border-l-4 border-l-gray-300';
  };
  const genderAvatar = (gender) => {
    const g = gender?.toUpperCase();
    if (g === 'MALE') return 'bg-blue-500';
    if (g === 'FEMALE') return 'bg-pink-500';
    return 'bg-[#807aeb]';
  };
  const genderLabel = (gender) => {
    const g = gender?.toUpperCase();
    if (g === 'MALE') return <span className="text-xs bg-blue-50 text-blue-600 border border-blue-200 px-2 py-0.5 rounded-full">♂ Male</span>;
    if (g === 'FEMALE') return <span className="text-xs bg-pink-50 text-pink-600 border border-pink-200 px-2 py-0.5 rounded-full">♀ Female</span>;
    if (gender) return <span className="text-xs bg-gray-50 text-gray-500 border border-gray-200 px-2 py-0.5 rounded-full capitalize">{gender}</span>;
    return null;
  };

  useEffect(() => { fetchEventAndApplications(); }, [id]);

  const fetchEventAndApplications = async () => {
    setLoading(true);
    setError('');
    try {
      const token = localStorage.getItem('token');
      const headers = { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' };

      const [eventRes, appRes] = await Promise.all([
        fetch(`${API_BASE_URL}/events/${id}`, { headers }),
        fetch(`${API_BASE_URL}/events/${id}/applications`, { headers }),
      ]);

      if (!eventRes.ok) throw new Error('Failed to fetch event');
      if (!appRes.ok) throw new Error('Failed to fetch applications');

      setEvent(await eventRes.json());
      const appData = await appRes.json();
      setApplications(Array.isArray(appData) ? appData : []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleApplicationAction = async () => {
    if (!selectedApplication || !actionType) return;
    setIsProcessing(true);
    try {
      const token = localStorage.getItem('token');
      const res = await fetch(`${API_BASE_URL}/applications/${selectedApplication.id}/${actionType}`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
      });
      if (!res.ok) throw new Error(`Failed to ${actionType} application`);
      setActionModalOpen(false);
      const actionLabels = { accept: 'accepted', reject: 'rejected', decline: 'declined' };
      toast.success(`Application ${actionLabels[actionType] || actionType} successfully`);
      fetchEventAndApplications();
    } catch (err) {
      setError(err.message);
      toast.error(err.message);
    } finally {
      setIsProcessing(false);
    }
  };

  const openActionModal = (application, action) => {
    setSelectedApplication(application);
    setActionType(action);
    setActionModalOpen(true);
  };

  const handleAttendanceClick = () => {
    if (isInProgress || isEventDay) {
      navigate(`/attendance/${id}`);
    } else {
      setAttendanceBlockedModal(true);
    }
  };

  const filteredApplications = filterStatus === 'ALL'
    ? applications
    : applications.filter(app => app.status === filterStatus);

  const statusCounts = {
    ALL: applications.length,
    [APPLICATION_STATUS.PENDING]: applications.filter(a => a.status === APPLICATION_STATUS.PENDING).length,
    [APPLICATION_STATUS.ACCEPTED]: applications.filter(a => a.status === APPLICATION_STATUS.ACCEPTED).length,
    [APPLICATION_STATUS.CONFIRMED]: applications.filter(a => a.status === APPLICATION_STATUS.CONFIRMED).length,
    [APPLICATION_STATUS.REJECTED]: applications.filter(a => a.status === APPLICATION_STATUS.REJECTED).length,
  };

  // Check if today is on or after the event date
  const isEventDay = event ? new Date().toISOString().split('T')[0] >= event.date : false;
  const isInProgress = event?.status === 'IN_PROGRESS';
  const isDepositPaid = event?.status === 'DEPOSIT_PAID';
  const remainingSlots = event ? event.requiredVolunteers - (event.confirmedCount || 0) : 0;

  const statusBadge = (status) => {
    const map = {
      PENDING: 'bg-yellow-100 text-yellow-700',
      ACCEPTED: 'bg-blue-100 text-blue-700',
      CONFIRMED: 'bg-[#10B981]/10 text-[#10B981]',
      REJECTED: 'bg-red-100 text-red-600',
      CANCELLED: 'bg-gray-100 text-gray-500',
    };
    return map[status] || 'bg-gray-100 text-gray-500';
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-[#ebf2fa] flex items-center justify-center">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-[#807aeb]" />
      </div>
    );
  }

  if (!event) {
    return (
      <div className="min-h-screen bg-[#ebf2fa] flex items-center justify-center">
        <div className="text-center">
          <p className="text-[#111827] font-semibold mb-4">Event not found</p>
          <button onClick={() => navigate('/my-events')}
            className="px-6 py-2 bg-[#807aeb] text-white rounded-xl hover:bg-[#6b64d4] transition">
            Back to My Events
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#ebf2fa] py-8 animate-fade-in">
      <div className="max-w-4xl mx-auto px-4 sm:px-6">

        {/* Back */}
        <button onClick={() => navigate('/my-events')}
          className="flex items-center gap-2 text-[#807aeb] hover:text-[#6b64d4] transition mb-6 text-sm font-medium">
          ← Back to My Events
        </button>

        {/* Header */}
        <div className="flex items-start justify-between mb-6 gap-4 flex-wrap">
          <div>
            <h1 className="text-3xl font-bold text-[#111827]">{event.title}</h1>
            <p className="text-[#6B7280] mt-1">Manage applications for this event</p>
          </div>
          {/* Take Attendance button — always visible, blocked before event day */}
          <button
            onClick={handleAttendanceClick}
            className={`flex items-center gap-2 px-5 py-2.5 font-semibold rounded-xl transition shadow-sm text-white ${
              isInProgress || isEventDay ? 'bg-[#10B981] hover:bg-[#059669]' : 'bg-[#807aeb] hover:bg-[#6b64d4]'
            }`}>
            <i className="bx bx-check-circle text-lg"></i>
            Take Attendance
          </button>
        </div>

        {/* Event Summary */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6">
          {[
            { label: 'Event Date', value: formatDate(event.date, 'short'), color: 'text-[#111827]' },
            { label: 'Location', value: event.location, color: 'text-[#111827]' },
            { label: 'Volunteers', value: `${event.confirmedCount || 0}/${event.requiredVolunteers}`, color: 'text-[#807aeb]' },
            { label: 'Remaining Slots', value: remainingSlots, color: remainingSlots > 0 ? 'text-[#10B981]' : 'text-[#EF4444]' },
          ].map(({ label, value, color }) => (
            <div key={label} className="bg-white rounded-2xl border border-[#807aeb]/10 p-4 shadow-sm">
              <p className="text-xs text-[#6B7280] mb-1">{label}</p>
              <p className={`font-semibold text-sm ${color}`}>{value}</p>
            </div>
          ))}
        </div>

        {/* Status badge */}
        <div className="mb-4 flex items-center gap-2">
          <span className="text-xs text-[#6B7280]">Event status:</span>
          <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
            isInProgress ? 'bg-[#10B981]/10 text-[#10B981]' :
            isDepositPaid ? 'bg-[#807aeb]/10 text-[#807aeb]' :
            'bg-gray-100 text-gray-500'
          }`}>{event.status}</span>
          {isEventDay && !isInProgress && (
            <span className="text-xs text-yellow-600 bg-yellow-50 px-2 py-1 rounded-full border border-yellow-200">
              Today is event day — start the event to enable attendance
            </span>
          )}
        </div>

        {/* Error */}
        {error && (
          <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-xl flex items-center justify-between">
            <p className="text-[#EF4444] text-sm">{error}</p>
            <button onClick={() => setError('')} className="text-[#EF4444] ml-3 text-lg leading-none">✕</button>
          </div>
        )}

        {/* Filter Tabs */}
        <div className="flex gap-1 mb-6 bg-white rounded-2xl border border-[#807aeb]/10 p-1 shadow-sm overflow-x-auto">
          {['ALL', APPLICATION_STATUS.PENDING, APPLICATION_STATUS.ACCEPTED, APPLICATION_STATUS.CONFIRMED, APPLICATION_STATUS.REJECTED].map((status) => (
            <button key={status} onClick={() => setFilterStatus(status)}
              className={`flex-1 py-2 px-3 rounded-xl text-xs font-medium transition whitespace-nowrap ${
                filterStatus === status ? 'bg-[#807aeb] text-white' : 'text-[#6B7280] hover:text-[#111827]'
              }`}>
              {status === 'ALL' ? 'All' : formatApplicationStatus(status)} ({statusCounts[status] ?? 0})
            </button>
          ))}
        </div>

        {/* Applications */}
        {filteredApplications.length === 0 ? (
          <div className="bg-white rounded-2xl border border-[#807aeb]/10 p-12 text-center shadow-sm">
            <p className="text-[#6B7280]">No applications found for this filter.</p>
          </div>
        ) : (
          <div className="space-y-3">
            {filteredApplications.map((app) => (
              <div key={app.id} className={`bg-white rounded-2xl border border-[#807aeb]/10 p-5 shadow-sm ${genderBorder(app.volunteerGender)}`}>
                <div className="flex items-start justify-between gap-4 flex-wrap">
                  {/* Volunteer info */}
                  <div className="flex items-center gap-3">
                    <button
                      onClick={() => setProfilePopup(app)}
                      className={`w-11 h-11 ${genderAvatar(app.volunteerGender)} rounded-full flex items-center justify-center text-white font-bold text-base flex-shrink-0 hover:opacity-80 transition ring-2 ring-white hover:ring-[#807aeb]/30`}
                      title="View profile">
                      {app.volunteerName?.charAt(0).toUpperCase()}
                    </button>
                    <div>
                      <div className="flex items-center gap-2 flex-wrap">
                        <button onClick={() => setProfilePopup(app)}
                          className="text-[#111827] font-semibold hover:text-[#807aeb] transition text-left">
                          {app.volunteerName}
                        </button>
                        {genderLabel(app.volunteerGender)}
                      </div>
                      <p className="text-xs text-[#6B7280]">{app.volunteerEmail}</p>
                      <div className="flex items-center gap-2 mt-1">
                        <RatingStars rating={app.volunteerAverageRating || 0} size="sm" />
                        <span className="text-xs text-[#6B7280]">
                          {app.volunteerAverageRating?.toFixed(1) || '0.0'} ({app.volunteerRatingCount || 0} reviews)
                        </span>
                        {app.volunteerVerificationBadge && (
                          <span className="text-xs bg-[#807aeb]/10 text-[#807aeb] px-2 py-0.5 rounded-full border border-[#807aeb]/20">✓ Verified</span>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Status + actions */}
                  <div className="flex flex-col items-end gap-2">
                    <span className={`px-3 py-1 rounded-full text-xs font-semibold ${statusBadge(app.status)}`}>
                      {formatApplicationStatus(app.status)}
                    </span>
                    <div className="text-xs text-[#9CA3AF] text-right">
                      <p>Applied: {formatDate(app.appliedAt, 'short')}</p>
                      {app.acceptedAt && <p>Accepted: {formatDate(app.acceptedAt, 'short')}</p>}
                      {app.confirmedAt && <p>Confirmed: {formatDate(app.confirmedAt, 'short')}</p>}
                    </div>
                    <div className="flex gap-2">
                      {app.status === APPLICATION_STATUS.PENDING && (
                        <>
                          <button onClick={() => openActionModal(app, 'accept')}
                            className="px-3 py-1.5 bg-[#10B981]/10 text-[#10B981] text-xs font-semibold rounded-lg hover:bg-[#10B981]/20 transition">
                            Accept
                          </button>
                          <button onClick={() => openActionModal(app, 'reject')}
                            className="px-3 py-1.5 bg-red-50 text-[#EF4444] text-xs font-semibold rounded-lg hover:bg-red-100 transition">
                            Reject
                          </button>
                        </>
                      )}
                      {app.status === APPLICATION_STATUS.ACCEPTED && (
                        <button onClick={() => openActionModal(app, 'decline')}
                          className="px-3 py-1.5 bg-yellow-50 text-yellow-600 text-xs font-semibold rounded-lg hover:bg-yellow-100 transition">
                          Decline
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Action Modal */}
      <Modal isOpen={actionModalOpen} onClose={() => setActionModalOpen(false)}
        title={`${actionType?.charAt(0).toUpperCase()}${actionType?.slice(1)} Application`} size="md">
        <div className="space-y-4">
          <p className="text-[#6B7280]">
            Are you sure you want to <span className="font-semibold text-[#111827]">{actionType}</span> the application from{' '}
            <span className="font-semibold text-[#111827]">{selectedApplication?.volunteerName}</span>?
          </p>
          {actionType === 'accept' && (
            <p className="text-sm text-[#6B7280]">The volunteer will have 48 hours to confirm their participation.</p>
          )}
          <div className="flex gap-3 pt-2">
            <button onClick={() => setActionModalOpen(false)}
              className="flex-1 py-2 bg-[#ebf2fa] text-[#111827] font-medium rounded-xl hover:bg-gray-200 transition">
              Cancel
            </button>
            <button onClick={handleApplicationAction} disabled={isProcessing}
              className={`flex-1 py-2 text-white font-medium rounded-xl transition disabled:opacity-50 ${
                actionType === 'reject' || actionType === 'decline'
                  ? 'bg-[#EF4444] hover:bg-red-600'
                  : 'bg-[#10B981] hover:bg-[#059669]'
              }`}>
              {isProcessing ? 'Processing...' : `${actionType?.charAt(0).toUpperCase()}${actionType?.slice(1)}`}
            </button>
          </div>
        </div>
      </Modal>
      {/* Attendance Blocked Modal */}
      <Modal isOpen={attendanceBlockedModal} onClose={() => setAttendanceBlockedModal(false)}
        title="Attendance Not Available" size="sm">
        <div className="space-y-4">
          <div className="flex items-center justify-center w-14 h-14 bg-[#807aeb]/10 rounded-full mx-auto">
            <i className="bx bx-calendar-x text-3xl text-[#807aeb]"></i>
          </div>
          <p className="text-center text-[#6B7280] text-sm">
            This event is scheduled for
          </p>
          <p className="text-center text-[#111827] font-bold text-lg">
            {event ? formatDate(event.date, 'long') : ''}
          </p>
          <p className="text-center text-[#6B7280] text-sm">
            You can only take attendance on the day of the event.
          </p>
          <button onClick={() => setAttendanceBlockedModal(false)}
            className="w-full py-2.5 bg-[#807aeb] text-white font-semibold rounded-xl hover:bg-[#6b64d4] transition mt-2">
            Got it
          </button>
        </div>
      </Modal>

      {/* Applicant Profile Popup */}
      {profilePopup && (
        <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4" onClick={() => setProfilePopup(null)}>
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm animate-slide-up overflow-y-auto max-h-[90vh]" onClick={e => e.stopPropagation()}>
            {/* Header */}
            <div className="flex items-center justify-between px-6 pt-5 pb-3">
              <h3 className="text-base font-bold text-[#111827]">Applicant Profile</h3>
              <button onClick={() => setProfilePopup(null)} className="text-[#9CA3AF] hover:text-[#111827] text-xl leading-none">✕</button>
            </div>

            {/* Avatar / Profile Picture */}
            <div className="flex flex-col items-center gap-3 px-6 pb-4">
              <div className={`w-20 h-20 rounded-2xl overflow-hidden flex-shrink-0 ${!profilePopup.volunteerProfilePictureUrl ? genderAvatar(profilePopup.volunteerGender) : ''} flex items-center justify-center`}>
                {profilePopup.volunteerProfilePictureUrl ? (
                  <img src={profilePopup.volunteerProfilePictureUrl} alt={profilePopup.volunteerName}
                    className="w-full h-full object-cover" crossOrigin="anonymous"
                    onError={e => { e.target.style.display = 'none'; e.target.nextSibling.style.display = 'flex'; }} />
                ) : null}
                <div className={`w-full h-full flex items-center justify-center text-white text-3xl font-bold ${profilePopup.volunteerProfilePictureUrl ? 'hidden' : ''}`}>
                  {profilePopup.volunteerName?.charAt(0).toUpperCase()}
                </div>
              </div>
              <div className="text-center">
                <p className="text-lg font-bold text-[#111827]">{profilePopup.volunteerName}</p>
                <p className="text-sm text-[#6B7280]">{profilePopup.volunteerEmail}</p>
                <div className="flex items-center justify-center gap-2 mt-1 flex-wrap">
                  {genderLabel(profilePopup.volunteerGender)}
                  {profilePopup.volunteerVerificationBadge && (
                    <span className="text-xs bg-[#807aeb]/10 text-[#807aeb] px-2 py-0.5 rounded-full border border-[#807aeb]/20">✓ Verified</span>
                  )}
                </div>
              </div>
            </div>

            {/* Identity info */}
            {(profilePopup.volunteerDateOfBirth || profilePopup.volunteerGender) && (
              <div className="mx-6 mb-4 grid grid-cols-2 gap-3">
                {profilePopup.volunteerDateOfBirth && (
                  <div className="bg-[#ebf2fa] rounded-xl p-3">
                    <p className="text-xs text-[#6B7280] mb-0.5">Age</p>
                    <p className="font-semibold text-[#111827] text-sm">
                      {Math.floor((new Date() - new Date(profilePopup.volunteerDateOfBirth)) / (365.25 * 24 * 60 * 60 * 1000))} yrs
                    </p>
                  </div>
                )}
                {profilePopup.volunteerDateOfBirth && (
                  <div className="bg-[#ebf2fa] rounded-xl p-3">
                    <p className="text-xs text-[#6B7280] mb-0.5">Date of Birth</p>
                    <p className="font-semibold text-[#111827] text-sm">
                      {new Date(profilePopup.volunteerDateOfBirth).toLocaleDateString('en-IN')}
                    </p>
                  </div>
                )}
              </div>
            )}

            {/* Stats */}
            <div className="mx-6 mb-4 grid grid-cols-3 gap-3">
              <div className="bg-[#ebf2fa] rounded-xl p-3 text-center">
                <p className="text-xs text-[#6B7280] mb-1">Rating</p>
                <p className="font-bold text-[#111827]">{profilePopup.volunteerAverageRating?.toFixed(1) || '—'}</p>
              </div>
              <div className="bg-[#ebf2fa] rounded-xl p-3 text-center">
                <p className="text-xs text-[#6B7280] mb-1">Reviews</p>
                <p className="font-bold text-[#111827]">{profilePopup.volunteerRatingCount || 0}</p>
              </div>
              <div className="bg-[#ebf2fa] rounded-xl p-3 text-center">
                <p className="text-xs text-[#6B7280] mb-1">No-shows</p>
                <p className={`font-bold ${profilePopup.volunteerNoShowCount > 0 ? 'text-[#EF4444]' : 'text-[#10B981]'}`}>
                  {profilePopup.volunteerNoShowCount || 0}
                </p>
              </div>
            </div>

            {/* Gallery Photos */}
            {profilePopup.volunteerGalleryPhotos?.length > 0 && (
              <div className="mx-6 mb-4">
                <p className="text-xs font-semibold text-[#6B7280] mb-2">Photos</p>
                <div className="grid grid-cols-3 gap-2">
                  {profilePopup.volunteerGalleryPhotos.map((url, idx) => (
                    <img key={idx} src={url} alt={`Photo ${idx + 1}`}
                      className="w-full h-24 object-cover rounded-xl" crossOrigin="anonymous" />
                  ))}
                </div>
              </div>
            )}

            {/* Application status */}
            <div className="mx-6 mb-4 flex items-center justify-between text-sm">
              <span className="text-[#6B7280]">Application status</span>
              <span className={`px-3 py-1 rounded-full text-xs font-semibold ${statusBadge(profilePopup.status)}`}>
                {formatApplicationStatus(profilePopup.status)}
              </span>
            </div>

            {/* Actions */}
            <div className="flex gap-2 px-6 pb-6">
              {profilePopup.status === APPLICATION_STATUS.PENDING && (
                <>
                  <button onClick={() => { setProfilePopup(null); openActionModal(profilePopup, 'accept'); }}
                    className="flex-1 py-2 bg-[#10B981]/10 text-[#10B981] text-sm font-semibold rounded-xl hover:bg-[#10B981]/20 transition">
                    Accept
                  </button>
                  <button onClick={() => { setProfilePopup(null); openActionModal(profilePopup, 'reject'); }}
                    className="flex-1 py-2 bg-red-50 text-[#EF4444] text-sm font-semibold rounded-xl hover:bg-red-100 transition">
                    Reject
                  </button>
                </>
              )}
              {profilePopup.status === APPLICATION_STATUS.ACCEPTED && (
                <button onClick={() => { setProfilePopup(null); openActionModal(profilePopup, 'decline'); }}
                  className="flex-1 py-2 bg-yellow-50 text-yellow-600 text-sm font-semibold rounded-xl hover:bg-yellow-100 transition">
                  Decline
                </button>
              )}
              <button onClick={() => setProfilePopup(null)}
                className="flex-1 py-2 bg-[#ebf2fa] text-[#111827] text-sm font-semibold rounded-xl hover:bg-gray-200 transition">
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default EventApplications;
