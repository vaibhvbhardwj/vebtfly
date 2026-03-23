import React, { useState } from 'react';
import { useDebounce } from '../../hooks/useDebounce';

export const SearchBar = ({ onSearch, placeholder = 'Search...', debounceDelay = 500 }) => {
  const [value, setValue] = useState('');
  const debouncedValue = useDebounce(value, debounceDelay);

  React.useEffect(() => {
    onSearch(debouncedValue);
  }, [debouncedValue]);

  const handleClear = () => setValue('');

  return (
    <div className="relative">
      <div className="absolute left-4 top-1/2 -translate-y-1/2 text-[#807aeb]">
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
      </div>

      <input
        type="text"
        value={value}
        onChange={(e) => setValue(e.target.value)}
        placeholder={placeholder}
        className="w-full pl-12 pr-10 py-3 bg-white border border-[#807aeb]/30 rounded-xl text-[#111827] placeholder-[#6B7280] focus:outline-none focus:border-[#807aeb] focus:ring-2 focus:ring-[#807aeb]/20 transition shadow-sm"
      />

      {value && (
        <button
          onClick={handleClear}
          className="absolute right-4 top-1/2 -translate-y-1/2 text-[#6B7280] hover:text-[#111827] transition"
        >
          <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
          </svg>
        </button>
      )}
    </div>
  );
};
