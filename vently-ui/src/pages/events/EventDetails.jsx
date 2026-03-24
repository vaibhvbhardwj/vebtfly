import { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';
import { useParams, useNavigate } from 'react-router-dom';
import { useEvents } from '../../hooks/useEvents';
import { useAuth } from '../../hooks/useAuth';
import { RatingStars } from '../../components/shared/RatingStars';
import { formatDate, formatCurrency, formatRelativeTime } from '../../utils/formatters';
import { USER_ROLES } from '../../utils/constants';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

const EventDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { selectedEvent, fetchEventById, clearSelectedEvent } = useEvents();
  const { user } = useAuth();
  const [pageLoading, setPageLoading] = useState(true);
  const [pageError, setPageError] = useState(null);
  const [isApplyModalOpen, setIsApplyModalOpen] = useState(false);
  const [isApplying, setIsApplying] = useState(false);
  const [applySuccess, setApplySuccess] = useState(false);
  const [isStarting, setIsStarting] = useState(false);
  const [volunteerProfile, setVolunteerProfile] = useState(null);

  useEffect(() => {
    if (id) {
      setPageLoading(true);
      setPageError(null);
      fetchEventById(id)
        .catch(err => setPageError(err?.message || 'Failed to load event'))
        .finally(() => setPageLoading(false));
    }
    return () => clearSelectedEvent();
  }, [id]);

  // Fetch volunteer profile when modal opens
  useEffect(() => {
    if (isApplyModalOpen && user?.role === 'VOLUNTEER' && !volunteerProfile) {
      fetch(`${API_BASE}/users/profile`, {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
      })
        .then(r => r.ok ? r.json() : null)
        .then(data => { if (data) setVolunteerProfile(data); })
        .catch(() => {});
    }
  }, [isApplyModalOpen, user, volunteerProfile]);

  const handleApply = async () => {
    setIsApplying(true);
    try {
      const response = await fetch(`${API_BASE}/applications`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
        body: JSON.stringify({ eventId: selectedEvent.id }),
      });
      if (!response.ok) {
        const errData = await response.json().catch(() => ({}));
        throw new Error(errData.message || 'Failed to apply');
      }
      setApplySuccess(true);
    } catch (err) {
      console.error('Error applying:', err);
      alert('Could not submit application: ' + err.message);
    } finally {
      setIsApplying(false);
    }
  };

  const handleCloseApplyModal = () => {
    setIsApplyModalOpen(false);
    setApplySuccess(false);
    fetchEventById(id).catch(() => {});
  };

  const handleStartEvent = async () => {
    setIsStarting(true);
    try {
      await fetch(`${API_BASE}/events/${selectedEvent.id}/start`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
      });
      fetchEventById(id).catch(() => {});
    } catch (err) {
      console.error('Failed to start event:', err);
    } finally {
      setIsStarting(false);
    }
  };

  if (pageLoading) {
    return (
      <div className="min-h-screen bg-[#ebf2fa] flex items-center justify-center">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-[#807aeb]" />
      </div>
    );
  }

  if (pageError || !selectedEvent) {
    return (
      <div className="min-h-screen bg-[#ebf2fa] flex items-center justify-center">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-[#111827] mb-3">Event not found</h1>
          <p className="text-[#6B7280] mb-6">{pageError || 'The event you are looking for does not exist.'}</p>
          <button onClick={() => navigate('/events')}
            className="px-6 py-2 bg-[#807aeb] text-white font-medium rounded-xl hover:bg-[#6b64d4] transition">
            Back to Events
          </button>
        </div>
      </div>
    );
  }

  const remainingSlots = selectedEvent.requiredVolunteers - (selectedEvent.confirmedCount || 0);
  const capacityPercentage = ((selectedEvent.confirmedCount || 0) / selectedEvent.requiredVolunteers) * 100;
  const isEventFull = remainingSlots <= 0;
  const isOrganizerView = user?.role === USER_ROLES.ORGANIZER && user?.id === selectedEvent.organizerId;
  const isInProgress = selectedEvent.status === 'IN_PROGRESS';
  const isDepositPaid = selectedEvent.status === 'DEPOSIT_PAID';

  return (
    <div className="min-h-screen bg-[#ebf2fa] py-8 pb-24 animate-fade-in">
      <div className="max-w-3xl mx-auto px-4 sm:px-6">
        <button onClick={() => navigate('/events')}
          className="flex items-center gap-2 text-[#807aeb] hover:text-[#6b64d4] transition mb-6 text-sm font-medium">
          ← Back to Events
        </button>

        <div className="bg-white rounded-2xl overflow-hidden shadow-sm border border-[#807aeb]/10 mb-6">
          {/* Hero — full natural height */}
          <div className="relative w-full bg-gradient-to-br from-[#807aeb]/30 to-[#807aeb]/60 overflow-hidden">
            {selectedEvent.imageUrl ? (
              <img src={selectedEvent.imageUrl} alt={selectedEvent.title} className="w-full h-auto block" />
            ) : (
              <div className="w-full h-72 flex items-center justify-center text-6xl opacity-30">🎪</div>
            )}
            <div className="absolute top-4 right-4 px-3 py-1 bg-white/90 text-[#807aeb] rounded-full text-sm font-semibold">
              {selectedEvent.status}
            </div>
          </div>

          <div className="p-8">
            <h1 className="text-3xl font-bold text-[#111827] mb-4">{selectedEvent.title}</h1>

            {/* Organizer */}
            <div className="flex items-center gap-4 mb-6 pb-6 border-b border-[#ebf2fa]">
              <div className="w-11 h-11 bg-[#807aeb] rounded-full flex items-center justify-center text-white font-bold">
                {selectedEvent.organizer?.name?.charAt(0).toUpperCase()}
              </div>
              <div className="flex-1">
                <p className="text-[#111827] font-semibold">{selectedEvent.organizer?.name}</p>
                <div className="flex items-center gap-2">
                  <RatingStars rating={selectedEvent.organizer?.averageRating || 0} size="sm" />
                  <span className="text-xs text-[#6B7280]">
                    {selectedEvent.organizer?.averageRating?.toFixed(1)} ({selectedEvent.organizer?.ratingCount || 0} reviews)
                  </span>
                </div>
              </div>
              {selectedEvent.organizer?.verificationBadge && (
                <span className="text-xs bg-[#807aeb]/10 text-[#807aeb] px-3 py-1 rounded-full border border-[#807aeb]/20 font-medium">
                  ✓ Verified
                </span>
              )}
            </div>

            {/* Key Details */}
            <div className="grid grid-cols-2 gap-4 mb-6">
              <div className="bg-[#ebf2fa] rounded-xl p-4">
                <p className="text-xs text-[#6B7280] mb-1">Date & Time</p>
                <p className="text-[#111827] font-medium text-sm">{formatDate(selectedEvent.date, 'long')} at {selectedEvent.time}</p>
              </div>
              <div className="bg-[#ebf2fa] rounded-xl p-4">
                <p className="text-xs text-[#6B7280] mb-1">Location</p>
                <p className="text-[#111827] font-medium text-sm">📍 {selectedEvent.location}</p>
              </div>
              <div className="bg-[#ebf2fa] rounded-xl p-4">
                <p className="text-xs text-[#6B7280] mb-1">Stipend</p>
                {selectedEvent.paymentPerMaleVolunteer || selectedEvent.paymentPerFemaleVolunteer ? (
                  <div className="space-y-1">
                    {selectedEvent.paymentPerMaleVolunteer && (
                      <div className="flex items-center gap-2">
                        <span className="text-xs text-blue-500 font-medium">♂ Boys</span>
                        <span className="text-[#10B981] font-bold">{formatCurrency(selectedEvent.paymentPerMaleVolunteer)}</span>
                      </div>
                    )}
                    {selectedEvent.paymentPerFemaleVolunteer && (
                      <div className="flex items-center gap-2">
                        <span className="text-xs text-pink-500 font-medium">♀ Girls</span>
                        <span className="text-[#10B981] font-bold">{formatCurrency(selectedEvent.paymentPerFemaleVolunteer)}</span>
                      </div>
                    )}
                  </div>
                ) : (
                  <p className="text-[#10B981] font-bold text-lg">{formatCurrency(selectedEvent.paymentPerVolunteer)}</p>
                )}
              </div>
              <div className="bg-[#ebf2fa] rounded-xl p-4">
                <p className="text-xs text-[#6B7280] mb-1">Category</p>
                <span className="bg-[#807aeb]/10 text-[#807aeb] px-3 py-1 rounded-full text-sm font-medium border border-[#807aeb]/20">
                  {selectedEvent.category}
                </span>
              </div>
            </div>

            {/* Capacity */}
            <div className="mb-6">
              <div className="flex justify-between text-sm mb-2">
                <span className="text-[#6B7280]">Volunteers</span>
                <span className="font-semibold text-[#111827]">{selectedEvent.confirmedCount || 0}/{selectedEvent.requiredVolunteers}</span>
              </div>
              <div className="w-full h-2 bg-[#ebf2fa] rounded-full">
                <div className="h-2 bg-[#807aeb] rounded-full transition-all" style={{ width: `${Math.min(capacityPercentage, 100)}%` }} />
              </div>
              {isEventFull
                ? <p className="text-xs text-[#EF4444] mt-1">Event is full</p>
                : <p className="text-xs text-[#10B981] mt-1">{remainingSlots} slots available</p>}
            </div>

            {/* Description */}
            <div className="mb-6">
              <h2 className="text-lg font-bold text-[#111827] mb-3">About this event</h2>
              <p className="text-[#6B7280] leading-relaxed whitespace-pre-wrap">{selectedEvent.description}</p>
            </div>

            {/* Deadline */}
            <div className="p-4 bg-[#807aeb]/5 border border-[#807aeb]/20 rounded-xl mb-6">
              <p className="text-xs text-[#6B7280] mb-1">Application Deadline</p>
              <p className="text-[#111827] font-semibold">{formatDate(selectedEvent.applicationDeadline, 'long')}</p>
              <p className="text-xs text-[#6B7280] mt-1">{formatRelativeTime(selectedEvent.applicationDeadline)}</p>
            </div>

            {!isOrganizerView && user?.role === USER_ROLES.VOLUNTEER && (
              <button
                onClick={() => setIsApplyModalOpen(true)}
                disabled={isEventFull}
                className="w-full py-3 bg-[#807aeb] text-white font-semibold rounded-xl hover:bg-[#6b64d4] hover:shadow-lg hover:shadow-[#807aeb]/30 transition disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isEventFull ? 'Event is Full' : 'Apply Now'}
              </button>
            )}
            {isOrganizerView && isDepositPaid && (
              <button
                onClick={handleStartEvent}
                disabled={isStarting}
                className="w-full py-3 bg-[#807aeb] text-white font-semibold rounded-xl hover:bg-[#6b64d4] transition disabled:opacity-50 mb-3"
              >
                {isStarting ? 'Starting...' : 'Start Event'}
              </button>
            )}
            {isOrganizerView && isInProgress && (
              <button
                onClick={() => navigate(`/attendance/${selectedEvent.id}`)}
                className="w-full py-3 bg-[#10B981] text-white font-semibold rounded-xl hover:bg-[#059669] transition"
              >
                Take Attendance
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Apply Modal — rendered via portal directly into document.body */}
      {isApplyModalOpen && createPortal(
        <div
          onClick={applySuccess ? undefined : handleCloseApplyModal}
          style={{
            position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
            zIndex: 99999,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            padding: '16px',
            backgroundColor: 'rgba(0,0,0,0.55)',
          }}
        >
          <div
            onClick={e => e.stopPropagation()}
            style={{
              background: '#fff', borderRadius: '16px',
              width: '100%', maxWidth: '440px',
              maxHeight: '90vh', overflowY: 'auto',
              boxShadow: '0 25px 60px rgba(0,0,0,0.3)',
            }}
          >
            {/* Header */}
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '20px 24px 16px', borderBottom: '1px solid #ebf2fa' }}>
              <span style={{ fontSize: '17px', fontWeight: 700, color: '#111827' }}>
                {applySuccess ? 'Application Submitted' : 'Apply for Event'}
              </span>
              {!applySuccess && (
                <button onClick={handleCloseApplyModal} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#6B7280', fontSize: '20px', lineHeight: 1, padding: 0 }}>✕</button>
              )}
            </div>

            {/* Body */}
            <div style={{ padding: '24px' }}>
              {applySuccess ? (
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '16px' }}>
                  {/* Avatar with checkmark */}
                  <div style={{ position: 'relative' }}>
                    <div style={{ width: 80, height: 80, borderRadius: 16, overflow: 'hidden', background: '#807aeb', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                      {volunteerProfile?.profilePictureUrl
                        ? <img src={volunteerProfile.profilePictureUrl} alt="You" style={{ width: '100%', height: '100%', objectFit: 'cover' }} crossOrigin="anonymous" />
                        : <span style={{ color: 'white', fontSize: 32, fontWeight: 700 }}>{(volunteerProfile?.fullName || user?.name || 'V')?.charAt(0).toUpperCase()}</span>
                      }
                    </div>
                    <div style={{ position: 'absolute', bottom: -8, right: -8, width: 28, height: 28, borderRadius: '50%', background: '#10B981', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                      <svg width="14" height="14" fill="none" stroke="white" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                      </svg>
                    </div>
                  </div>

                  <div style={{ textAlign: 'center' }}>
                    <p style={{ fontSize: 18, fontWeight: 700, color: '#111827', margin: '0 0 4px' }}>Application Submitted!</p>
                    <p style={{ fontSize: 14, color: '#6B7280', margin: 0 }}>
                      You've applied for <strong style={{ color: '#111827' }}>{selectedEvent.title}</strong>
                    </p>
                  </div>

                  {/* Volunteer details */}
                  {volunteerProfile && (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3,1fr)', gap: 8, width: '100%' }}>
                      {volunteerProfile.gender && (
                        <div style={{ background: '#ebf2fa', borderRadius: 12, padding: '10px 8px', textAlign: 'center' }}>
                          <p style={{ fontSize: 11, color: '#6B7280', margin: '0 0 2px' }}>Gender</p>
                          <p style={{ fontSize: 13, fontWeight: 600, color: '#111827', margin: 0, textTransform: 'capitalize' }}>{volunteerProfile.gender}</p>
                        </div>
                      )}
                      {volunteerProfile.dateOfBirth && (
                        <div style={{ background: '#ebf2fa', borderRadius: 12, padding: '10px 8px', textAlign: 'center' }}>
                          <p style={{ fontSize: 11, color: '#6B7280', margin: '0 0 2px' }}>Age</p>
                          <p style={{ fontSize: 13, fontWeight: 600, color: '#111827', margin: 0 }}>
                            {Math.floor((new Date() - new Date(volunteerProfile.dateOfBirth)) / (365.25 * 24 * 60 * 60 * 1000))} yrs
                          </p>
                        </div>
                      )}
                      <div style={{ background: '#ebf2fa', borderRadius: 12, padding: '10px 8px', textAlign: 'center' }}>
                        <p style={{ fontSize: 11, color: '#6B7280', margin: '0 0 2px' }}>Rating</p>
                        <p style={{ fontSize: 13, fontWeight: 600, color: '#111827', margin: 0 }}>
                          {volunteerProfile.averageRating > 0 ? volunteerProfile.averageRating.toFixed(1) : '—'}
                        </p>
                      </div>
                    </div>
                  )}

                  <p style={{ fontSize: 12, color: '#6B7280', textAlign: 'center', margin: 0 }}>
                    The organizer will review your application and get back to you.
                  </p>

                  <button
                    onClick={handleCloseApplyModal}
                    style={{ width: '100%', padding: '11px', background: '#807aeb', color: 'white', fontWeight: 600, borderRadius: 12, border: 'none', cursor: 'pointer', fontSize: 15 }}
                  >
                    Done
                  </button>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
                  <p style={{ color: '#6B7280', margin: 0 }}>
                    Apply for <strong style={{ color: '#111827' }}>{selectedEvent.title}</strong>?
                  </p>
                  <p style={{ fontSize: 14, color: '#6B7280', margin: 0 }}>
                    You'll be notified once the organizer reviews your application.
                  </p>
                  <div style={{ display: 'flex', gap: 12, paddingTop: 8 }}>
                    <button
                      onClick={handleCloseApplyModal}
                      style={{ flex: 1, padding: '10px', background: '#ebf2fa', color: '#111827', fontWeight: 500, borderRadius: 12, border: 'none', cursor: 'pointer', fontSize: 14 }}
                    >
                      Cancel
                    </button>
                    <button
                      onClick={handleApply}
                      disabled={isApplying}
                      style={{ flex: 1, padding: '10px', background: '#807aeb', color: 'white', fontWeight: 500, borderRadius: 12, border: 'none', cursor: isApplying ? 'not-allowed' : 'pointer', opacity: isApplying ? 0.6 : 1, fontSize: 14 }}
                    >
                      {isApplying ? 'Applying...' : 'Apply'}
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>,
        document.body
      )}
    </div>
  );
};

export default EventDetails;
