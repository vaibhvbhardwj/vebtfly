import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import api from '../../api/axios';

const CATEGORIES = ['community', 'education', 'environment', 'health', 'sports', 'arts', 'technology', 'other'];

const EditEvent = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuthStore();

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [success, setSuccess] = useState('');
  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState('');
  const [uploadingImage, setUploadingImage] = useState(false);
  const [paymentPerVolunteer, setPaymentPerVolunteer] = useState('');

  const [formData, setFormData] = useState({
    title: '',
    description: '',
    location: '',
    date: '',
    time: '',
    requiredVolunteers: '',
    category: 'community',
    applicationDeadline: '',
  });

  useEffect(() => {
    fetchEvent();
  }, [id]);

  const fetchEvent = async () => {
    try {
      const res = await api.get(`/events/${id}`);
      const e = res.data;
      setFormData({
        title: e.title || '',
        description: e.description || '',
        location: e.location || '',
        date: e.date || '',
        time: e.time || '',
        requiredVolunteers: e.requiredVolunteers || '',
        category: e.category || 'community',
        applicationDeadline: e.applicationDeadline || '',
      });
      setPaymentPerVolunteer(e.paymentPerVolunteer || '');
      setImagePreview(e.imageUrl || '');
    } catch (err) {
      setError('Failed to load event details.');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (fieldErrors[name]) setFieldErrors(prev => ({ ...prev, [name]: '' }));
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setImageFile(file);
    setImagePreview(URL.createObjectURL(file));
  };

  const uploadImage = async () => {
    if (!imageFile) return null;
    setUploadingImage(true);
    try {
      const fd = new FormData();
      fd.append('file', imageFile);
      const res = await api.post(`/events/${id}/image`, fd, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      const s3Url = res.data.imageUrl;
      setImagePreview(s3Url); // update preview to actual S3 URL
      setImageFile(null);
      return s3Url;
    } catch (err) {
      throw new Error('Image upload failed: ' + (err.response?.data?.error || err.message));
    } finally {
      setUploadingImage(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setFieldErrors({});
    setSuccess('');
    setSaving(true);

    try {
      // Upload image first if a new one was selected
      if (imageFile) {
        await uploadImage();
      }

      // Build payload — no paymentPerVolunteer (read-only)
      const payload = {
        title: formData.title,
        description: formData.description,
        location: formData.location,
        date: formData.date,
        time: formData.time,
        requiredVolunteers: parseInt(formData.requiredVolunteers, 10),
        category: formData.category,
        applicationDeadline: formData.applicationDeadline,
      };

      await api.put(`/events/${id}`, payload);
      setSuccess('Event updated successfully!');
      setTimeout(() => navigate('/organizer/dashboard'), 1500);
    } catch (err) {
      const data = err.response?.data;
      if (data?.fieldErrors) {
        setFieldErrors(data.fieldErrors);
      } else {
        setError(data?.message || 'Failed to update event. Please try again.');
      }
    } finally {
      setSaving(false);
    }
  };

  const inputClass = (field) =>
    `w-full px-4 py-3 rounded-xl border text-sm focus:outline-none focus:ring-2 transition-all ${
      fieldErrors[field]
        ? 'border-red-400 focus:ring-red-200 bg-red-50'
        : 'border-gray-200 focus:ring-[#807aeb]/30 focus:border-[#807aeb] bg-white'
    }`;

  if (loading) {
    return (
      <div className="min-h-screen bg-[#ebf2fa] flex items-center justify-center">
        <div className="animate-spin rounded-full h-10 w-10 border-4 border-[#807aeb] border-t-transparent" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#ebf2fa] py-10 px-4">
      <div className="max-w-2xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <button
            onClick={() => navigate(-1)}
            className="flex items-center gap-2 text-sm text-[#6B7280] hover:text-[#807aeb] transition-colors mb-4"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
            Back
          </button>
          <h1 className="text-2xl font-bold text-[#111827]">Edit Event</h1>
          <p className="text-[#6B7280] text-sm mt-1">Update your event details below</p>
        </div>

        {/* Alerts */}
        {error && (
          <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-xl text-red-700 text-sm">
            {error}
          </div>
        )}
        {success && (
          <div className="mb-4 p-4 bg-green-50 border border-green-200 rounded-xl text-green-700 text-sm">
            {success}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Basic Info Card */}
          <div className="bg-white rounded-2xl border border-[#807aeb]/10 shadow-sm p-6 space-y-4">
            <h2 className="text-base font-semibold text-[#111827]">Basic Information</h2>

            <div>
              <label className="block text-sm font-medium text-[#111827] mb-1">Event Title</label>
              <input
                type="text"
                name="title"
                value={formData.title}
                onChange={handleChange}
                className={inputClass('title')}
                placeholder="Enter event title"
              />
              {fieldErrors.title && <p className="mt-1 text-xs text-red-500">{fieldErrors.title}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-[#111827] mb-1">Description</label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleChange}
                rows={4}
                className={inputClass('description')}
                placeholder="Describe your event..."
              />
              {fieldErrors.description && <p className="mt-1 text-xs text-red-500">{fieldErrors.description}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-[#111827] mb-1">Category</label>
              <select
                name="category"
                value={formData.category}
                onChange={handleChange}
                className={inputClass('category')}
              >
                {CATEGORIES.map(c => (
                  <option key={c} value={c}>{c.charAt(0).toUpperCase() + c.slice(1)}</option>
                ))}
              </select>
            </div>
          </div>

          {/* Date & Location Card */}
          <div className="bg-white rounded-2xl border border-[#807aeb]/10 shadow-sm p-6 space-y-4">
            <h2 className="text-base font-semibold text-[#111827]">Date, Time & Location</h2>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-[#111827] mb-1">Event Date</label>
                <input
                  type="date"
                  name="date"
                  value={formData.date}
                  onChange={handleChange}
                  className={inputClass('date')}
                />
                {fieldErrors.date && <p className="mt-1 text-xs text-red-500">{fieldErrors.date}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-[#111827] mb-1">Event Time</label>
                <input
                  type="time"
                  name="time"
                  value={formData.time}
                  onChange={handleChange}
                  className={inputClass('time')}
                />
                {fieldErrors.time && <p className="mt-1 text-xs text-red-500">{fieldErrors.time}</p>}
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-[#111827] mb-1">Location</label>
              <input
                type="text"
                name="location"
                value={formData.location}
                onChange={handleChange}
                className={inputClass('location')}
                placeholder="Event location or address"
              />
              {fieldErrors.location && <p className="mt-1 text-xs text-red-500">{fieldErrors.location}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-[#111827] mb-1">Application Deadline</label>
              <input
                type="date"
                name="applicationDeadline"
                value={formData.applicationDeadline}
                onChange={handleChange}
                className={inputClass('applicationDeadline')}
              />
              {fieldErrors.applicationDeadline && (
                <p className="mt-1 text-xs text-red-500">{fieldErrors.applicationDeadline}</p>
              )}
            </div>
          </div>

          {/* Volunteers & Payment Card */}
          <div className="bg-white rounded-2xl border border-[#807aeb]/10 shadow-sm p-6 space-y-4">
            <h2 className="text-base font-semibold text-[#111827]">Volunteers & Payment</h2>

            <div>
              <label className="block text-sm font-medium text-[#111827] mb-1">Required Volunteers</label>
              <input
                type="number"
                name="requiredVolunteers"
                value={formData.requiredVolunteers}
                onChange={handleChange}
                min="1"
                className={inputClass('requiredVolunteers')}
                placeholder="Number of volunteers needed"
              />
              {fieldErrors.requiredVolunteers && (
                <p className="mt-1 text-xs text-red-500">{fieldErrors.requiredVolunteers}</p>
              )}
            </div>

            {/* Payment is read-only */}
            <div>
              <label className="block text-sm font-medium text-[#111827] mb-1">
                Payment per Volunteer
                <span className="ml-2 text-xs text-[#6B7280] font-normal">(cannot be changed after creation)</span>
              </label>
              <div className="w-full px-4 py-3 rounded-xl border border-gray-200 bg-gray-50 text-sm text-[#6B7280]">
                ₹{paymentPerVolunteer}
              </div>
            </div>
          </div>

          {/* Image Upload Card */}
          <div className="bg-white rounded-2xl border border-[#807aeb]/10 shadow-sm p-6 space-y-4">
            <h2 className="text-base font-semibold text-[#111827]">Event Image</h2>

            {imagePreview && (
              <img
                src={imagePreview}
                alt="Event preview"
                className="w-full h-48 object-cover rounded-xl border border-gray-200"
              />
            )}

            <label className="flex flex-col items-center justify-center w-full h-32 border-2 border-dashed border-[#807aeb]/30 rounded-xl cursor-pointer hover:border-[#807aeb]/60 hover:bg-[#807aeb]/5 transition-all">
              <svg className="w-8 h-8 text-[#807aeb]/50 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              <span className="text-sm text-[#6B7280]">
                {imageFile ? imageFile.name : 'Click to upload a new image'}
              </span>
              <input type="file" accept="image/*" onChange={handleImageChange} className="hidden" />
            </label>
          </div>

          {/* Submit */}
          <div className="flex gap-3">
            <button
              type="button"
              onClick={() => navigate(-1)}
              className="flex-1 py-3 rounded-xl border border-gray-200 text-[#6B7280] text-sm font-medium hover:bg-gray-50 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving || uploadingImage}
              className="flex-1 py-3 rounded-xl bg-[#807aeb] text-white text-sm font-semibold hover:bg-[#6c66d4] transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {saving || uploadingImage ? (
                <span className="flex items-center justify-center gap-2">
                  <svg className="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
                  </svg>
                  {uploadingImage ? 'Uploading...' : 'Saving...'}
                </span>
              ) : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EditEvent;
