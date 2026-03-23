import React, { useState } from 'react';

export const FilterPanel = ({ filters, onFilterChange, onReset }) => {
  const [isOpen, setIsOpen] = useState(false);

  const handleFilterChange = (key, value) => {
    onFilterChange({ ...filters, [key]: value });
  };

  const handleReset = () => {
    onReset();
    setIsOpen(false);
  };

  const inputClass = 'w-full px-3 py-2 bg-[#ebf2fa] border border-[#807aeb]/20 rounded-lg text-[#111827] text-sm placeholder-[#6B7280] focus:outline-none focus:border-[#807aeb] focus:ring-1 focus:ring-[#807aeb]/20 transition';

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-2 px-4 py-3 bg-white border border-[#807aeb]/30 rounded-xl text-[#111827] hover:border-[#807aeb] hover:shadow-sm transition shadow-sm font-medium"
      >
        <svg className="w-4 h-4 text-[#807aeb]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
        </svg>
        Filters
      </button>

      {isOpen && (
        <div className="absolute top-full right-0 mt-2 w-64 bg-white border border-[#807aeb]/20 rounded-2xl shadow-xl p-4 z-10">
          <div className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-[#111827] mb-2 uppercase tracking-wide">Date Range</label>
              <div className="space-y-2">
                <input type="date" value={filters.dateStart || ''} onChange={(e) => handleFilterChange('dateStart', e.target.value)} className={inputClass} />
                <input type="date" value={filters.dateEnd || ''} onChange={(e) => handleFilterChange('dateEnd', e.target.value)} className={inputClass} />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-[#111827] mb-2 uppercase tracking-wide">Location</label>
              <input type="text" value={filters.location || ''} onChange={(e) => handleFilterChange('location', e.target.value)} placeholder="Enter location" className={inputClass} />
            </div>

            <div>
              <label className="block text-xs font-semibold text-[#111827] mb-2 uppercase tracking-wide">Payment Range (₹)</label>
              <div className="space-y-2">
                <input type="number" value={filters.minPayment || ''} onChange={(e) => handleFilterChange('minPayment', e.target.value)} placeholder="Min" className={inputClass} />
                <input type="number" value={filters.maxPayment || ''} onChange={(e) => handleFilterChange('maxPayment', e.target.value)} placeholder="Max" className={inputClass} />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-[#111827] mb-2 uppercase tracking-wide">Category</label>
              <select value={filters.category || ''} onChange={(e) => handleFilterChange('category', e.target.value)} className={inputClass}>
                <option value="">All Categories</option>
                <option value="community">Community</option>
                <option value="environment">Environment</option>
                <option value="education">Education</option>
                <option value="health">Health</option>
                <option value="other">Other</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-[#111827] mb-2 uppercase tracking-wide">Sort By</label>
              <select value={filters.sort || 'relevance'} onChange={(e) => handleFilterChange('sort', e.target.value)} className={inputClass}>
                <option value="relevance">Relevance</option>
                <option value="date">Date (Newest)</option>
                <option value="payment-high">Payment (High to Low)</option>
                <option value="payment-low">Payment (Low to High)</option>
              </select>
            </div>

            <div className="flex gap-2 pt-3 border-t border-[#ebf2fa]">
              <button onClick={handleReset} className="flex-1 px-3 py-2 bg-[#ebf2fa] hover:bg-gray-200 text-[#111827] text-sm font-medium rounded-lg transition">
                Reset
              </button>
              <button onClick={() => setIsOpen(false)} className="flex-1 px-3 py-2 bg-[#807aeb] hover:bg-[#6b64d4] text-white text-sm font-medium rounded-lg transition">
                Apply
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
