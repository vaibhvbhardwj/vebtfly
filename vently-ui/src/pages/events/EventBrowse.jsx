import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useEvents } from '../../hooks/useEvents';
import { useAuthStore } from '../../store/authStore';
import { SearchBar } from '../../components/shared/SearchBar';
import { FilterPanel } from '../../components/shared/FilterPanel';
import { Pagination } from '../../components/shared/Pagination';
import { EventCard } from '../../components/events/EventCard';
import SubscriptionLimitModal from '../../components/SubscriptionLimitModal';
import api from '../../api/axios';

const EventBrowse = () => {
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAuthStore();
  const [showLoginModal, setShowLoginModal] = useState(false);
  const [initialized, setInitialized] = useState(false);
  const [showLimitModal, setShowLimitModal] = useState(false);
  const [subscription, setSubscription] = useState(null);
  const [applicationCount, setApplicationCount] = useState(0);
  const {
    filteredEvents, loading, error, filters, pagination,
    fetchEvents, setFilters, resetFilters, setPagination, clearError,
  } = useEvents();

  useEffect(() => {
    if (!initialized) {
      if (!isAuthenticated) {
        setShowLoginModal(true);
      } else {
        fetchEvents(filters, pagination.page);
        fetchSubscriptionAndApplicationCount();
      }
      setInitialized(true);
    }
  }, []);

  const fetchSubscriptionAndApplicationCount = async () => {
    try {
      const subResponse = await api.get('/subscriptions/current');
      setSubscription(subResponse.data);
      if (user?.role === 'VOLUNTEER') {
        const appsResponse = await api.get('/applications/my-applications');
        const apps = Array.isArray(appsResponse.data) ? appsResponse.data : appsResponse.data.content || [];
        setApplicationCount(apps.filter(a => a.status === 'PENDING' || a.status === 'ACCEPTED').length);
      }
    } catch (err) {
      console.error('Error fetching subscription/applications:', err);
    }
  };

  const handleSearch = useCallback((query) => {
    setFilters({ ...filters, search: query });
    setPagination(1);
    fetchEvents({ ...filters, search: query }, 1);
  }, [filters, setFilters, setPagination, fetchEvents]);

  const handleFilterChange = useCallback((newFilters) => {
    setFilters(newFilters);
    setPagination(1);
    fetchEvents(newFilters, 1);
  }, [setFilters, setPagination, fetchEvents]);

  const handleResetFilters = useCallback(() => {
    resetFilters();
    fetchEvents({}, 1);
  }, [resetFilters, fetchEvents]);

  const handlePageChange = useCallback((page) => {
    setPagination(page);
    fetchEvents(filters, page);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, [filters, setPagination, fetchEvents]);

  return (
    <div className="min-h-screen bg-[#ebf2fa]">
      {/* Login Required Modal */}
      {showLoginModal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-xl p-8 max-w-md w-full border border-[#807aeb]/10 animate-scale-in">
            <h2 className="text-2xl font-bold text-[#111827] mb-3">Login Required</h2>
            <p className="text-[#6B7280] mb-6">
              You need to be <span className="font-semibold text-[#807aeb]">logged in</span> to browse events.
            </p>
            <div className="flex gap-3">
              <button onClick={() => navigate('/login')}
                className="flex-1 py-3 bg-[#807aeb] text-white font-semibold rounded-xl hover:bg-[#6b64d4] transition">
                Go to Login
              </button>
              <button onClick={() => navigate('/')}
                className="flex-1 py-3 bg-[#ebf2fa] text-[#111827] font-semibold rounded-xl hover:bg-gray-200 transition">
                Go Home
              </button>
            </div>
          </div>
        </div>
      )}

      {!showLoginModal && (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in">
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-[#111827] mb-1">Browse Events</h1>
            <p className="text-[#6B7280]">Find volunteer opportunities that match your interests</p>
          </div>

          {/* Subscription CTA — shown to volunteers on FREE tier */}
          {user?.role === 'VOLUNTEER' && (!subscription || subscription?.tier === 'FREE') && (
            <div className="mb-6 flex items-center justify-between gap-4 p-4 bg-[#807aeb]/5 border border-[#807aeb]/20 rounded-2xl">
              <div className="flex items-center gap-3">
                <i className="bx bxs-crown text-2xl text-[#807aeb]" />
                <div>
                  <p className="text-sm font-semibold text-[#111827]">
                    {applicationCount}/5 free applications used
                  </p>
                  <p className="text-xs text-[#6B7280]">Go Premium for unlimited applications & priority access</p>
                </div>
              </div>
              <a href="/subscription"
                className="px-4 py-2 bg-[#807aeb] text-white text-sm font-semibold rounded-xl hover:bg-[#6b64d4] transition flex-shrink-0">
                Upgrade
              </a>
            </div>
          )}

          <div className="flex flex-col sm:flex-row gap-4 mb-8">
            <div className="flex-1">
              <SearchBar onSearch={handleSearch} placeholder="Search events..." debounceDelay={500} />
            </div>
            <FilterPanel filters={filters} onFilterChange={handleFilterChange} onReset={handleResetFilters} />
          </div>

          {error && (
            <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-xl flex items-center justify-between">
              <p className="text-[#EF4444] text-sm">{error}</p>
              <button onClick={clearError} className="text-[#EF4444] hover:opacity-70 transition ml-4">✕</button>
            </div>
          )}

          {loading && (
            <div className="flex items-center justify-center py-16">
              <div className="text-center">
                <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-[#807aeb] mx-auto mb-4" />
                <p className="text-[#6B7280]">Loading events...</p>
              </div>
            </div>
          )}

          {!loading && filteredEvents.length > 0 && (
            <>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
                {filteredEvents.map((event) => (
                  <EventCard
                    key={event.id}
                    event={event}
                    subscription={subscription}
                    applicationCount={applicationCount}
                    onLimitReached={() => setShowLimitModal(true)}
                  />
                ))}
              </div>
              <Pagination
                currentPage={pagination.page}
                totalPages={Math.ceil(pagination.total / pagination.pageSize)}
                onPageChange={handlePageChange}
                loading={loading}
              />
            </>
          )}

          {!loading && filteredEvents.length === 0 && (
            <div className="text-center py-16">
              <i className="bx bx-search text-5xl text-[#807aeb]/40 mb-4 block" />
              <h3 className="text-xl font-semibold text-[#111827] mb-2">No events found</h3>
              <p className="text-[#6B7280] mb-6">Try adjusting your filters or search query</p>
              <button onClick={handleResetFilters}
                className="px-6 py-2 bg-[#807aeb] text-white font-medium rounded-xl hover:bg-[#6b64d4] transition">
                Clear Filters
              </button>
            </div>
          )}
        </div>
      )}

      <SubscriptionLimitModal
        isOpen={showLimitModal}
        onClose={() => setShowLimitModal(false)}
        type="applications"
        currentCount={applicationCount}
        limit={5}
      />
    </div>
  );
};

export default EventBrowse;
