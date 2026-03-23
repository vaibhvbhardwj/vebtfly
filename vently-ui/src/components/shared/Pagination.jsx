import React from 'react';

export const Pagination = ({ currentPage, totalPages, onPageChange, loading = false }) => {
  const getPageNumbers = () => {
    const pages = [];
    const maxVisible = 5;
    let startPage = Math.max(1, currentPage - Math.floor(maxVisible / 2));
    let endPage = Math.min(totalPages, startPage + maxVisible - 1);

    if (endPage - startPage < maxVisible - 1) {
      startPage = Math.max(1, endPage - maxVisible + 1);
    }

    if (startPage > 1) {
      pages.push(1);
      if (startPage > 2) pages.push('...');
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }

    if (endPage < totalPages) {
      if (endPage < totalPages - 1) pages.push('...');
      pages.push(totalPages);
    }

    return pages;
  };

  const handlePageChange = (page) => {
    if (page !== '...' && page !== currentPage && !loading) {
      onPageChange(page);
    }
  };

  if (totalPages <= 1) return null;

  const pages = getPageNumbers();

  return (
    <div className="flex items-center justify-center gap-2 mt-8">
      {/* Previous Button */}
      <button
        onClick={() => handlePageChange(currentPage - 1)}
        disabled={currentPage === 1 || loading}
        className="p-2 rounded-lg border border-slate-600 text-slate-400 hover:text-white hover:border-slate-500 disabled:opacity-50 disabled:cursor-not-allowed transition"
      >
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
        </svg>
      </button>

      {/* Page Numbers */}
      <div className="flex items-center gap-1">
        {pages.map((page, index) => (
          <button
            key={index}
            onClick={() => handlePageChange(page)}
            disabled={page === '...' || loading}
            className={`px-3 py-2 rounded-lg font-medium transition ${
              page === currentPage
                ? 'bg-gradient-to-r from-blue-500 to-indigo-600 text-white'
                : page === '...'
                ? 'text-slate-400 cursor-default'
                : 'border border-slate-600 text-slate-400 hover:text-white hover:border-slate-500 disabled:opacity-50 disabled:cursor-not-allowed'
            }`}
          >
            {page}
          </button>
        ))}
      </div>

      {/* Next Button */}
      <button
        onClick={() => handlePageChange(currentPage + 1)}
        disabled={currentPage === totalPages || loading}
        className="p-2 rounded-lg border border-slate-600 text-slate-400 hover:text-white hover:border-slate-500 disabled:opacity-50 disabled:cursor-not-allowed transition"
      >
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
        </svg>
      </button>

      {/* Info */}
      <span className="ml-4 text-sm text-slate-400">
        Page {currentPage} of {totalPages}
      </span>
    </div>
  );
};
