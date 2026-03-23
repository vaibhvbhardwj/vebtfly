import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { formatDate, formatCurrency, formatApplicationStatus } from '../../utils/formatters';
import { RatingStars } from '../shared/RatingStars';
import { useAuthStore } from '../../store/authStore';
import api from '../../api/axios';

export const EventCard = ({ event, applicationStatus = null, subscription = null, applicationCount = 0, onLimitReached = null }) => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const isOrganizer = user?.role === 'ORGANIZER';
  const isOwnEvent = isOrganizer && event.organizer?.id === user?.id;
  const [applying, setApplying] = useState(false);
  const remainingSlots = event.requiredVolunteers - (event.confirmedCount || 0);
  const capacityPercentage = ((event.confirmedCount || 0) / event.requiredVolunteers) * 100;

  const handleApply = async (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (subscription?.tier === 'FREE' && applicationCount >= 5) {
      if (onLimitReached) onLimitReached();
      return;
    }
    setApplying(true);
    try {
      await api.post('/applications', { eventId: event.id });
      window.location.reload();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to apply to event');
    } finally {
      setApplying(false);
    }
  };

  return (
    <Link to={`/events/${event.id}`}>
      <div className="bg-white border border-[#807aeb]/10 rounded-2xl overflow-hidden card-hover h-full flex flex-col shadow-sm">
        {/* Image */}
        <div className="relative h-44 bg-gradient-to-br from-[#807aeb]/20 to-[#807aeb]/40 overflow-hidden">
          {event.imageUrl ? (
            <img src={event.imageUrl} alt={event.title}
              className="w-full h-full object-cover hover:scale-105 transition-transform duration-300" />
          ) : (
            <div className="w-full h-full flex items-center justify-center">
              <svg className="w-14 h-14 text-[#807aeb]/30" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
          )}

          {/* Status badge */}
          <div className="absolute top-3 right-3">
            <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-white/90 text-[#807aeb] backdrop-blur-sm">
              {event.status}
            </span>
          </div>

          {/* Slots badge */}
          {remainingSlots > 0 && (
            <div className="absolute top-3 left-3 bg-[#10B981]/90 text-white px-2.5 py-1 rounded-full text-xs font-semibold backdrop-blur-sm">
              {remainingSlots} slots left
            </div>
          )}
        </div>

        {/* Content */}
        <div className="p-4 flex-1 flex flex-col">
          <h3 className="text-base font-bold text-[#111827] mb-1.5 line-clamp-2 hover:text-[#807aeb] transition">
            {event.title}
          </h3>

          <p className="text-sm text-[#6B7280] mb-3 line-clamp-2">{event.description}</p>

          {/* Organizer */}
          <div className="flex items-center gap-2 mb-3 pb-3 border-b border-[#ebf2fa]">
            <div className="w-8 h-8 bg-[#807aeb] rounded-full flex items-center justify-center text-white text-xs font-bold flex-shrink-0">
              {event.organizer?.name?.charAt(0).toUpperCase()}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm text-[#111827] truncate font-medium">{event.organizer?.name}</p>
              <div className="flex items-center gap-1">
                <RatingStars rating={event.organizer?.averageRating || 0} size="sm" />
                <span className="text-xs text-[#6B7280]">({event.organizer?.ratingCount || 0})</span>
              </div>
            </div>
          </div>

          {/* Details */}
          <div className="space-y-1.5 mb-3 text-sm">
            <div className="flex items-center gap-2 text-[#6B7280]">
              <svg className="w-4 h-4 text-[#807aeb] flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              <span>{formatDate(event.date, 'short')}</span>
            </div>
            <div className="flex items-center gap-2 text-[#6B7280]">
              <svg className="w-4 h-4 text-[#807aeb] flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <span className="truncate">{event.location}</span>
            </div>
            <div className="flex items-center gap-2">
              <svg className="w-4 h-4 text-[#10B981] flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span className="font-semibold text-[#10B981]">{formatCurrency(event.paymentPerVolunteer)}</span>
            </div>
          </div>

          {/* Capacity bar */}
          <div className="mb-4">
            <div className="flex items-center justify-between mb-1.5">
              <span className="text-xs text-[#6B7280]">Capacity</span>
              <span className="text-xs text-[#6B7280]">{event.confirmedCount || 0}/{event.requiredVolunteers}</span>
            </div>
            <div className="w-full h-1.5 bg-[#ebf2fa] rounded-full overflow-hidden">
              <div className="h-full bg-[#807aeb] rounded-full transition-all duration-300"
                style={{ width: `${Math.min(capacityPercentage, 100)}%` }} />
            </div>
          </div>

          {/* Application status pill */}
          {applicationStatus && (
            <div className="mb-3 px-3 py-2 rounded-xl text-center text-sm font-medium bg-[#807aeb]/10 text-[#807aeb] border border-[#807aeb]/20">
              {formatApplicationStatus(applicationStatus)}
            </div>
          )}

          {/* Action button */}
          <div className="mt-auto">
            {isOrganizer ? (
              isOwnEvent ? (
                <button onClick={(e) => { e.preventDefault(); navigate(`/events/${event.id}/applications`); }}
                  className="w-full py-2 px-4 bg-[#807aeb]/10 text-[#807aeb] rounded-xl font-medium hover:bg-[#807aeb]/20 transition text-sm border border-[#807aeb]/20">
                  Manage Applications
                </button>
              ) : null
            ) : (
              !applicationStatus && (
                <button onClick={handleApply} disabled={applying || remainingSlots === 0}
                  className="w-full py-2 px-4 bg-[#807aeb] text-white rounded-xl font-medium hover:bg-[#6c66d4] transition disabled:opacity-50 disabled:cursor-not-allowed text-sm">
                  {applying ? 'Applying...' : remainingSlots === 0 ? 'No Slots Available' : 'Apply Now'}
                </button>
              )
            )}
          </div>
        </div>
      </div>
    </Link>
  );
};
