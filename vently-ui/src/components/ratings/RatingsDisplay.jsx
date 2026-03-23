import React, { useState, useEffect } from 'react';
import { RatingStars } from '../shared/RatingStars';
import { Pagination } from '../shared/Pagination';
import { ratingApi } from '../../api/ratingApi';
import { formatDate } from '../../utils/formatters';

export const RatingsDisplay = ({ userId, averageRating, ratingCount }) => {
  const [ratings, setRatings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (userId) {
      fetchRatings(0);
    }
  }, [userId]);

  const fetchRatings = async (page) => {
    if (!userId) return;
    
    setLoading(true);
    setError(null);

    try {
      const response = await ratingApi.getUserRatings(userId, page, 5);
      setRatings(response.content || []);
      setCurrentPage(response.number || 0);
      setTotalPages(response.totalPages || 0);
    } catch (err) {
      setError('Failed to load ratings');
      console.error('Error fetching ratings:', err);
    } finally {
      setLoading(false);
    }
  };

  const handlePageChange = (page) => {
    fetchRatings(page);
  };

  return (
    <div className="space-y-6">
      {/* Average Rating Summary */}
      <div className="bg-[[#807aeb]] rounded-lg p-6 border border-slate-600">
        <div className="flex items-center gap-6">
          <div className="flex flex-col items-center">
            <div className="text-4xl font-bold text-[#c9a46d] mb-2">
              {averageRating?.toFixed(1) || '0.0'}
            </div>
            <RatingStars rating={averageRating || 0} size="md" />
            <p className="text-sm text-slate-400 mt-2">
              {ratingCount || 0} {ratingCount === 1 ? 'review' : 'reviews'}
            </p>
          </div>

          {/* Rating Breakdown */}
          <div className="flex-1 space-y-2">
            {[5, 4, 3, 2, 1].map((star) => (
              <div key={star} className="flex items-center gap-2">
                <span className="text-xs text-slate-400 w-8">{star} star</span>
                <div className="flex-1 h-2 bg-slate-600 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-gradient-to-r from-yellow-400 to-yellow-500"
                    style={{ width: '0%' }}
                  ></div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Reviews List */}
      <div>
        <h3 className="text-lg font-bold text-black mb-4">Recent Reviews</h3>

        {loading && (
          <div className="flex justify-center py-8">
            <svg className="animate-spin h-8 w-8 text-blue-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
          </div>
        )}

        {error && (
          <div className="p-4 bg-red-500/10 border border-red-500/30 rounded-lg">
            <p className="text-sm text-red-400">{error}</p>
          </div>
        )}

        {!loading && ratings.length === 0 && (
          <div className="text-center py-8">
            <p className="text-slate-400">No reviews yet</p>
          </div>
        )}

        {!loading && ratings.length > 0 && (
          <div className="space-y-4">
            {ratings.map((rating) => (
              <div key={rating.id} className="bg-slate-700/50 rounded-lg p-4 border border-slate-600">
                <div className="flex items-start justify-between mb-3">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-full flex items-center justify-center text-white font-bold text-sm">
                      {rating.rater?.name?.charAt(0).toUpperCase()}
                    </div>
                    <div>
                      <p className="text-white font-semibold">{rating.rater?.name}</p>
                      <p className="text-xs text-slate-400">{formatDate(rating.createdAt, 'short')}</p>
                    </div>
                  </div>
                  <RatingStars rating={rating.rating} size="sm" />
                </div>

                {rating.review && (
                  <p className="text-slate-300 text-sm leading-relaxed">{rating.review}</p>
                )}
              </div>
            ))}
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="mt-6">
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
