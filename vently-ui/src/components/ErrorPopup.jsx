import React, { useState } from 'react';
import { useError } from '../context/ErrorContext';

export default function ErrorPopup() {
  const { error, clearError } = useError();
  const [copied, setCopied] = useState(false);

  if (!error) return null;

  const handleCopy = () => {
    navigator.clipboard.writeText(error.traceId || '');
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
      <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full mx-4 overflow-hidden">
        {/* Header */}
        <div className="flex items-center gap-3 px-6 py-4 border-b border-gray-100">
          <div className="w-9 h-9 rounded-full flex items-center justify-center" style={{ background: '#FEE2E2' }}>
            <i className="bx bx-error-circle text-xl" style={{ color: '#EF4444' }}></i>
          </div>
          <div>
            <p className="font-semibold text-sm" style={{ color: '#111827' }}>Something went wrong</p>
            <p className="text-xs" style={{ color: '#6B7280' }}>An error occurred while processing your request</p>
          </div>
          <button
            onClick={clearError}
            className="ml-auto p-1 rounded-lg hover:bg-gray-100 transition"
            aria-label="Close"
          >
            <i className="bx bx-x text-xl" style={{ color: '#6B7280' }}></i>
          </button>
        </div>

        {/* Body */}
        <div className="px-6 py-4">
          <p className="text-sm mb-4" style={{ color: '#374151' }}>
            {error.message || 'An unexpected error occurred. Please try again.'}
          </p>

          {error.traceId && (
            <div className="rounded-xl p-3" style={{ background: '#F3F4F6' }}>
              <p className="text-xs mb-1 font-medium" style={{ color: '#6B7280' }}>Trace ID (share with support)</p>
              <div className="flex items-center gap-2">
                <code className="text-xs flex-1 break-all font-mono" style={{ color: '#111827' }}>
                  {error.traceId}
                </code>
                <button
                  onClick={handleCopy}
                  className="flex-shrink-0 flex items-center gap-1 px-2 py-1 rounded-lg text-xs font-medium transition"
                  style={{
                    background: copied ? '#D1FAE5' : '#EDE9FE',
                    color: copied ? '#10B981' : '#807aeb',
                  }}
                >
                  <i className={`bx ${copied ? 'bx-check' : 'bx-copy'} text-sm`}></i>
                  {copied ? 'Copied' : 'Copy'}
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-gray-100 flex justify-end">
          <button
            onClick={clearError}
            className="px-4 py-2 rounded-xl text-sm font-medium text-white transition hover:opacity-90"
            style={{ background: '#807aeb' }}
          >
            Dismiss
          </button>
        </div>
      </div>
    </div>
  );
}
