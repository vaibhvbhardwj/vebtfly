import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FileUpload } from '../../components/shared/FileUpload';
import { disputeApi } from '../../api/disputeApi';
import { API_BASE_URL } from '../../utils/constants';

const DisputeSubmission = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    eventId: '',
    description: '',
  });
  const [uploadedFiles, setUploadedFiles] = useState([]);
  const [events, setEvents] = useState([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [loadingEvents, setLoadingEvents] = useState(true);

  useEffect(() => {
    fetchUserEvents();
  }, []);

  const fetchUserEvents = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/events/my-events`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
      });

      if (!response.ok) throw new Error('Failed to fetch events');

      const data = await response.json();
      setEvents(data.content || data || []);
    } catch (err) {
      console.error('Error fetching events:', err);
      setError('Failed to load your events');
    } finally {
      setLoadingEvents(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleFilesSelected = (files) => {
    // Validate file sizes (max 10MB per file)
    const validFiles = files.filter((file) => {
      if (file.size > 10 * 1024 * 1024) {
        setError(`File ${file.name} exceeds 10MB limit`);
        return false;
      }
      return true;
    });

    setUploadedFiles((prev) => [...prev, ...validFiles]);
    setError(null);
  };

  const handleRemoveFile = (index) => {
    setUploadedFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    // Validation
    if (!formData.eventId) {
      setError('Please select an event');
      return;
    }

    if (!formData.description.trim()) {
      setError('Please provide a description');
      return;
    }

    if (formData.description.trim().length < 20) {
      setError('Description must be at least 20 characters');
      return;
    }

    setIsSubmitting(true);

    try {
      // Create dispute
      const disputeResponse = await disputeApi.createDispute({
        eventId: parseInt(formData.eventId),
        description: formData.description.trim(),
      });

      // Upload evidence if files are selected
      if (uploadedFiles.length > 0) {
        await disputeApi.uploadEvidence(disputeResponse.id, uploadedFiles);
      }

      // Navigate to dispute details
      navigate(`/disputes/${disputeResponse.id}`, {
        state: { message: 'Dispute submitted successfully' },
      });
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit dispute');
      console.error('Error submitting dispute:', err);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
      {/* Background decorative elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-20 right-0 w-96 h-96 bg-red-500 rounded-full mix-blend-multiply filter blur-3xl opacity-10 animate-pulse"></div>
        <div className="absolute bottom-0 left-0 w-96 h-96 bg-orange-500 rounded-full mix-blend-multiply filter blur-3xl opacity-10 animate-pulse"></div>
      </div>

      <div className="relative max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <button
            onClick={() => navigate('/disputes')}
            className="flex items-center gap-2 text-blue-400 hover:text-blue-300 transition mb-6"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
            Back to Disputes
          </button>

          <h1 className="text-4xl font-bold text-white mb-2">Submit a Dispute</h1>
          <p className="text-slate-400">
            Report an issue related to an event. Please provide detailed information and supporting evidence.
          </p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="bg-slate-800 border border-slate-700 rounded-2xl p-8 space-y-6">
          {/* Error Message */}
          {error && (
            <div className="p-4 bg-red-500/10 border border-red-500/30 rounded-lg">
              <p className="text-sm text-red-400">{error}</p>
            </div>
          )}

          {/* Event Selection */}
          <div>
            <label htmlFor="eventId" className="block text-sm font-medium text-white mb-2">
              Select Event <span className="text-red-400">*</span>
            </label>
            {loadingEvents ? (
              <div className="flex items-center justify-center py-4">
                <svg className="animate-spin h-5 w-5 text-blue-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              </div>
            ) : (
              <select
                id="eventId"
                name="eventId"
                value={formData.eventId}
                onChange={handleInputChange}
                className="w-full px-4 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">Choose an event...</option>
                {events.map((event) => (
                  <option key={event.id} value={event.id}>
                    {event.title}
                  </option>
                ))}
              </select>
            )}
            {events.length === 0 && !loadingEvents && (
              <p className="text-sm text-slate-400 mt-2">No events found</p>
            )}
          </div>

          {/* Description */}
          <div>
            <label htmlFor="description" className="block text-sm font-medium text-white mb-2">
              Description <span className="text-red-400">*</span>
            </label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleInputChange}
              placeholder="Describe the issue in detail. What happened? When did it occur? Who was involved?"
              rows={6}
              maxLength={2000}
              className="w-full px-4 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
            />
            <div className="flex justify-between items-center mt-2">
              <p className="text-xs text-slate-400">Minimum 20 characters required</p>
              <p className="text-xs text-slate-400">
                {formData.description.length}/2000
              </p>
            </div>
          </div>

          {/* File Upload */}
          <div>
            <label className="block text-sm font-medium text-white mb-2">
              Evidence (Optional)
            </label>
            <p className="text-xs text-slate-400 mb-3">
              Upload supporting files (images, documents). Max 10MB per file.
            </p>
            <FileUpload
              onFilesSelected={handleFilesSelected}
              maxSize={10 * 1024 * 1024}
              acceptedTypes={['image/*', '.pdf', '.doc', '.docx', '.xls', '.xlsx']}
            />

            {/* Uploaded Files List */}
            {uploadedFiles.length > 0 && (
              <div className="mt-4 space-y-2">
                <p className="text-sm font-medium text-white">Uploaded Files:</p>
                {uploadedFiles.map((file, index) => (
                  <div
                    key={index}
                    className="flex items-center justify-between p-3 bg-slate-700/50 rounded-lg border border-slate-600"
                  >
                    <div className="flex items-center gap-2 flex-1 min-w-0">
                      <svg className="w-5 h-5 text-blue-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                      </svg>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm text-white truncate">{file.name}</p>
                        <p className="text-xs text-slate-400">
                          {(file.size / 1024 / 1024).toFixed(2)} MB
                        </p>
                      </div>
                    </div>
                    <button
                      type="button"
                      onClick={() => handleRemoveFile(index)}
                      className="ml-2 p-1 text-slate-400 hover:text-red-400 transition"
                    >
                      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Submit Button */}
          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={() => navigate('/disputes')}
              className="flex-1 py-2 px-4 bg-slate-700 hover:bg-slate-600 text-white font-medium rounded-lg transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex-1 py-2 px-4 bg-gradient-to-r from-red-500 to-orange-600 hover:from-red-600 hover:to-orange-700 text-white font-medium rounded-lg transition disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isSubmitting ? 'Submitting...' : 'Submit Dispute'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};


export default DisputeSubmission;