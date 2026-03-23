import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Modal } from '../../components/shared/Modal';
import { formatDate, formatCurrency, formatEventStatus, getEventStatusColor } from '../../utils/formatters';
import { EVENT_STATUS, API_BASE_URL } from '../../utils/constants';
import { toast } from '../../components/Toast';

const MyEvents = () => {
  const navigate = useNavigate();
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [isCancelModalOpen, setIsCancelModalOpen] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);

  useEffect(() => {
    fetchEvents();
  }, []);

  const fetchEvents = async () => {
    setLoading(true);
    setError('');
    try {
      const token = localStorage.getItem('token');
      
      const response = await fetch(`${API_BASE_URL}/events/my-events`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        throw new Error(`Failed to fetch events: ${response.status}`);
      }

      const data = await response.json();
      setEvents(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handlePublishEvent = async (eventId) => {
    setIsProcessing(true);
    try {
      const response = await fetch(`${API_BASE_URL}/events/${eventId}/publish`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
      });

      if (!response.ok) throw new Error('Failed to publish event');

      await fetchEvents();
      toast.success('Event published successfully!');
    } catch (err) {
      toast.error(err.message || 'Failed to publish event');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleCancelEvent = async () => {
    if (!selectedEvent) return;

    setIsProcessing(true);
    try {
      const response = await fetch(`${API_BASE_URL}/events/${selectedEvent.id}`, {
        method: 'DELETE',
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
      });

      if (!response.ok) throw new Error('Failed to cancel event');

      setIsCancelModalOpen(false);
      await fetchEvents();
      toast.success('Event cancelled.');
    } catch (err) {
      toast.error(err.message || 'Failed to cancel event');
    } finally {
      setIsProcessing(false);
    }
  };

  const groupedEvents = {
    [EVENT_STATUS.DRAFT]: events.filter((e) => e.status === EVENT_STATUS.DRAFT),
    [EVENT_STATUS.PUBLISHED]: events.filter((e) => e.status === EVENT_STATUS.PUBLISHED),
    [EVENT_STATUS.DEPOSIT_PAID]: events.filter((e) => e.status === EVENT_STATUS.DEPOSIT_PAID),
    [EVENT_STATUS.IN_PROGRESS]: events.filter((e) => e.status === EVENT_STATUS.IN_PROGRESS),
    [EVENT_STATUS.COMPLETED]: events.filter((e) => e.status === EVENT_STATUS.COMPLETED),
    [EVENT_STATUS.CANCELLED]: events.filter((e) => e.status === EVENT_STATUS.CANCELLED),
  };

  const EventRow = ({ event }) => {
    return (
      <div className="bg-white rounded-2xl p-5 border border-[#807aeb]/10 shadow-sm card-hover">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div className="flex-1 min-w-0">
            <h3 className="text-base font-semibold text-[#111827] truncate cursor-pointer hover:text-[#807aeb] transition"
              onClick={() => navigate(`/events/${event.id}`)}>
              {event.title}
            </h3>
            <div className="flex flex-wrap items-center gap-3 mt-2 text-sm text-[#6B7280]">
              <span>📅 {formatDate(event.date, 'short')}</span>
              <span>📍 {event.location}</span>
              <span className="text-[#10B981] font-medium">{formatCurrency(event.paymentPerVolunteer)}</span>
            </div>
          </div>

          <div className="flex flex-col sm:items-end gap-2">
            <span className="px-3 py-1 bg-[#807aeb]/10 text-[#807aeb] text-xs font-semibold rounded-full border border-[#807aeb]/20 w-fit">
              {formatEventStatus(event.status)}
            </span>
            <p className="text-xs text-[#6B7280]">
              <span className="font-semibold text-[#111827]">{event.confirmedCount || 0}</span>/{event.requiredVolunteers} volunteers
            </p>
            <div className="flex gap-2 flex-wrap">
              {event.status === EVENT_STATUS.DRAFT && (
                <>
                  <button onClick={() => navigate(`/events/${event.id}/edit`)}
                    className="px-3 py-1.5 bg-[#807aeb]/10 text-[#807aeb] text-xs font-medium rounded-lg hover:bg-[#807aeb]/20 transition">
                    Edit
                  </button>
                  <button onClick={() => handlePublishEvent(event.id)} disabled={isProcessing}
                    className="px-3 py-1.5 bg-[#10B981]/10 text-[#10B981] text-xs font-medium rounded-lg hover:bg-[#10B981]/20 transition disabled:opacity-50">
                    Publish
                  </button>
                </>
              )}
              {event.status === EVENT_STATUS.PUBLISHED && (
                <>
                  <button onClick={() => navigate(`/events/${event.id}/edit`)}
                    className="px-3 py-1.5 bg-[#807aeb]/10 text-[#807aeb] text-xs font-medium rounded-lg hover:bg-[#807aeb]/20 transition">
                    Edit
                  </button>
                  <button onClick={() => navigate(`/events/${event.id}/manage-applications`)}
                    className="px-3 py-1.5 bg-[#807aeb]/10 text-[#807aeb] text-xs font-medium rounded-lg hover:bg-[#807aeb]/20 transition">
                    Applications
                  </button>
                </>
              )}
              {[EVENT_STATUS.DRAFT, EVENT_STATUS.PUBLISHED].includes(event.status) && (
                <button onClick={() => { setSelectedEvent(event); setIsCancelModalOpen(true); }}
                  className="px-3 py-1.5 bg-red-50 text-[#EF4444] text-xs font-medium rounded-lg hover:bg-red-100 transition">
                  Cancel
                </button>
              )}
              {event.status === EVENT_STATUS.IN_PROGRESS && (
                <button onClick={() => navigate(`/attendance/${event.id}`)}
                  className="px-3 py-1.5 bg-[#807aeb]/10 text-[#807aeb] text-xs font-medium rounded-lg hover:bg-[#807aeb]/20 transition">
                  Attendance
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
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-3xl font-bold text-[#111827] mb-1">My Events</h1>
            <p className="text-[#6B7280]">Manage your volunteer events</p>
          </div>
          <button onClick={() => navigate('/events/create')}
            className="px-5 py-2.5 bg-[#807aeb] text-white font-semibold rounded-xl hover:bg-[#6b64d4] hover:shadow-lg hover:shadow-[#807aeb]/30 transition flex items-center gap-2">
            <span>+</span> New Event
          </button>
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

        {!loading && events.length > 0 && (
          <div className="space-y-8">
            {Object.entries(groupedEvents).map(([status, eventList]) => (
              eventList.length > 0 && (
                <div key={status}>
                  <h2 className="text-base font-semibold text-[#111827] mb-3 flex items-center gap-2">
                    <span className="w-2.5 h-2.5 rounded-full bg-[#807aeb]" />
                    {formatEventStatus(status)} ({eventList.length})
                  </h2>
                  <div className="space-y-3">
                    {eventList.map((event) => (
                      <EventRow key={event.id} event={event} />
                    ))}
                  </div>
                </div>
              )
            ))}
          </div>
        )}

        {!loading && events.length === 0 && (
          <div className="text-center py-16">
            <div className="text-5xl mb-4">📋</div>
            <h3 className="text-xl font-semibold text-[#111827] mb-2">No events yet</h3>
            <p className="text-[#6B7280] mb-6">Create your first volunteer event</p>
            <button onClick={() => navigate('/events/create')}
              className="px-6 py-2 bg-[#807aeb] text-white font-medium rounded-xl hover:bg-[#6b64d4] transition">
              Create Event
            </button>
          </div>
        )}
      </div>

      {/* Cancel Modal */}
      <Modal isOpen={isCancelModalOpen} onClose={() => setIsCancelModalOpen(false)} title="Cancel Event" size="md">
        <div className="space-y-4">
          <p className="text-[#6B7280]">
            Cancel <span className="font-semibold text-[#111827]">{selectedEvent?.title}</span>?
          </p>
          <p className="text-sm text-[#6B7280]">All volunteers will be notified.</p>
          <div className="flex gap-3 pt-2">
            <button onClick={() => setIsCancelModalOpen(false)}
              className="flex-1 py-2 bg-[#ebf2fa] text-[#111827] font-medium rounded-xl hover:bg-gray-200 transition">
              Keep Event
            </button>
            <button onClick={handleCancelEvent} disabled={isProcessing}
              className="flex-1 py-2 bg-[#EF4444] text-white font-medium rounded-xl hover:bg-red-600 transition disabled:opacity-50">
              {isProcessing ? 'Cancelling...' : 'Cancel Event'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};


export default MyEvents;