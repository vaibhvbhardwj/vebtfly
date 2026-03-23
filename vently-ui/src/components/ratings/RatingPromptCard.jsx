import React, { useState } from 'react';
import { RatingStars } from '../shared/RatingStars';
import { RatingSubmissionModal } from './RatingSubmissionModal';

export const RatingPromptCard = ({ event, ratedUser, onRatingSubmitted }) => {
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Calculate days remaining for rating (7 days from event end)
  const eventEndDate = new Date(event.date);
  const ratingDeadline = new Date(eventEndDate.getTime() + 7 * 24 * 60 * 60 * 1000);
  const now = new Date();
  const daysLeft = Math.ceil((ratingDeadline - now) / (1000 * 60 * 60 * 24));
  const daysRemaining = Math.max(0, daysLeft);

  const isExpired = daysRemaining === 0;

  return (
    <>
      <div className="bg-gradient-to-br from-slate-700 to-slate-800 rounded-lg p-6 border border-slate-600 hover:border-blue-500/50 transition">
        <div className="flex items-start justify-between mb-4">
          <div>
            <h3 className="text-lg font-bold text-white mb-1">Rate this Event</h3>
            <p className="text-sm text-slate-400">{event.title}</p>
          </div>
          <div className="text-right">
            <p className="text-xs text-slate-400">Rating deadline</p>
            <p className={`text-sm font-semibold ${isExpired ? 'text-red-400' : 'text-yellow-400'}`}>
              {isExpired ? 'Expired' : `${daysRemaining} day${daysRemaining !== 1 ? 's' : ''} left`}
            </p>
          </div>
        </div>

        {/* User Being Rated */}
        <div className="flex items-center gap-3 mb-4 p-3 bg-slate-600/50 rounded-lg">
          <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-full flex items-center justify-center text-white font-bold text-sm">
            {ratedUser?.name?.charAt(0).toUpperCase()}
          </div>
          <div className="flex-1">
            <p className="text-white font-semibold text-sm">{ratedUser?.name}</p>
            <p className="text-xs text-slate-400">{ratedUser?.role}</p>
          </div>
        </div>

        {/* Action Button */}
        <button
          onClick={() => setIsModalOpen(true)}
          disabled={isExpired}
          className="w-full py-2 px-4 bg-gradient-to-r from-blue-500 to-indigo-600 hover:from-blue-600 hover:to-indigo-700 text-white font-medium rounded-lg transition disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:from-blue-500 disabled:hover:to-indigo-600"
        >
          {isExpired ? 'Rating Period Expired' : 'Rate Now'}
        </button>
      </div>

      {/* Rating Modal */}
      <RatingSubmissionModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        event={event}
        ratedUser={ratedUser}
        onSuccess={() => {
          onRatingSubmitted?.();
        }}
      />
    </>
  );
};
