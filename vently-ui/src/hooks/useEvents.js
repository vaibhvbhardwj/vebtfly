import { useEventStore } from '../store/eventStore';
import { useCallback } from 'react';

export const useEvents = () => {
  const {
    events,
    filteredEvents,
    selectedEvent,
    loading,
    error,
    filters,
    pagination,
    fetchEvents,
    fetchEventById,
    setFilters,
    resetFilters,
    setPagination,
    clearSelectedEvent,
    clearError,
  } = useEventStore();

  const handleFetchEvents = useCallback(
    async (newFilters = {}, page = 1) => {
      try {
        await fetchEvents(newFilters, page);
      } catch (err) {
        console.error('Error fetching events:', err);
      }
    },
    [fetchEvents]
  );

  const handleFetchEventById = useCallback(
    async (eventId) => {
      try {
        const event = await fetchEventById(eventId);
        return event;
      } catch (err) {
        console.error('Error fetching event:', err);
        throw err;
      }
    },
    [fetchEventById]
  );

  const handleSetFilters = useCallback(
    (newFilters) => {
      setFilters(newFilters);
    },
    [setFilters]
  );

  const handleResetFilters = useCallback(() => {
    resetFilters();
  }, [resetFilters]);

  const handleSetPagination = useCallback(
    (page) => {
      setPagination(page);
    },
    [setPagination]
  );

  const handleClearSelectedEvent = useCallback(() => {
    clearSelectedEvent();
  }, [clearSelectedEvent]);

  const handleClearError = useCallback(() => {
    clearError();
  }, [clearError]);

  return {
    events,
    filteredEvents,
    selectedEvent,
    loading,
    error,
    filters,
    pagination,
    fetchEvents: handleFetchEvents,
    fetchEventById: handleFetchEventById,
    setFilters: handleSetFilters,
    resetFilters: handleResetFilters,
    setPagination: handleSetPagination,
    clearSelectedEvent: handleClearSelectedEvent,
    clearError: handleClearError,
  };
};
