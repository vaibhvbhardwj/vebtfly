import React, { useState } from 'react';
import { Modal } from '../shared/Modal';
import { RatingStars } from '../shared/RatingStars';
import { ratingApi } from '../../api/ratingApi';

export const RatingSubmissionModal = ({ isOpen, onClose, event, ratedUser, onSuccess }) => {
  const [rating, setRating] = useState(0);
  const [review, setReview] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async () => {
    if (rating === 0) {
      setError('Please select a rating');
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      await ratingApi.submitRating({
        eventId: event.id,
        ratedId: ratedUser.id,
        rating,
        review: review.trim() || null,
      });

      setRating(0);
      setReview('');
      onSuccess?.();
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit rating');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleClose = () => {
    setRating(0);
    setReview('');
    setError(null);
    onClose();
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      title="Rate this Event"
      size="md"
    >
      <div className="space-y-6">
        {/* User Being Rated */}
        <div className="flex items-center gap-4 p-4 bg-slate-700/50 rounded-lg">
          <div className="w-12 h-12 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-full flex items-center justify-center text-white font-bold text-lg">
            {ratedUser?.name?.charAt(0).toUpperCase()}
          </div>
          <div>
            <p className="text-sm text-slate-400">Rating</p>
            <p className="text-white font-semibold">{ratedUser?.name}</p>
            <p className="text-xs text-slate-400">{ratedUser?.role}</p>
          </div>
        </div>

        {/* Event Info */}
        <div className="p-4 bg-slate-700/50 rounded-lg">
          <p className="text-sm text-slate-400 mb-1">Event</p>
          <p className="text-white font-semibold">{event?.title}</p>
        </div>

        {/* Star Rating */}
        <div>
          <label className="block text-sm font-medium text-white mb-3">
            Your Rating <span className="text-red-400">*</span>
          </label>
          <div className="flex justify-center">
            <RatingStars
              rating={rating}
              onRatingChange={setRating}
              interactive={true}
              size="xl"
            />
          </div>
          {rating > 0 && (
            <p className="text-center text-sm text-slate-400 mt-2">
              {rating === 1 && 'Poor'}
              {rating === 2 && 'Fair'}
              {rating === 3 && 'Good'}
              {rating === 4 && 'Very Good'}
              {rating === 5 && 'Excellent'}
            </p>
          )}
        </div>

        {/* Review Text */}
        <div>
          <label htmlFor="review" className="block text-sm font-medium text-white mb-2">
            Review (Optional)
          </label>
          <textarea
            id="review"
            value={review}
            onChange={(e) => setReview(e.target.value)}
            placeholder="Share your experience..."
            maxLength={500}
            rows={4}
            className="w-full px-4 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
          />
          <p className="text-xs text-slate-400 mt-1">
            {review.length}/500 characters
          </p>
        </div>

        {/* Error Message */}
        {error && (
          <div className="p-3 bg-red-500/10 border border-red-500/30 rounded-lg">
            <p className="text-sm text-red-400">{error}</p>
          </div>
        )}

        {/* Action Buttons */}
        <div className="flex gap-3 pt-4">
          <button
            onClick={handleClose}
            disabled={isSubmitting}
            className="flex-1 py-2 px-4 bg-slate-700 hover:bg-slate-600 text-white font-medium rounded-lg transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Cancel
          </button>
          <button
            onClick={handleSubmit}
            disabled={isSubmitting || rating === 0}
            className="flex-1 py-2 px-4 bg-gradient-to-r from-blue-500 to-indigo-600 hover:from-blue-600 hover:to-indigo-700 text-white font-medium rounded-lg transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isSubmitting ? 'Submitting...' : 'Submit Rating'}
          </button>
        </div>
      </div>
    </Modal>
  );
};
