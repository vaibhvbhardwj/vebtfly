import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import api from '../../api/axios';
import SubscriptionLimitModal from '../../components/SubscriptionLimitModal';
import { toast } from '../../components/Toast';

const CreateEvent = () => {
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAuthStore();
  const [showLoginModal, setShowLoginModal] = useState(false);
  const [showLimitModal, setShowLimitModal] = useState(false);
  const [subscription, setSubscription] = useState(null);
  const [eventCount, setEventCount] = useState(0);
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [formData, setFormData] = useState({
    title: '', description: '', date: '', time: '', location: '',
    requiredVolunteers: '', requiredMaleVolunteers: '', requiredFemaleVolunteers: '',
    paymentPerVolunteer: '', paymentPerMaleVolunteer: '', paymentPerFemaleVolunteer: '',
    category: 'community', applicationDeadline: '',
  });
  const [formErrors, setFormErrors] = useState({});

  useEffect(() => {
    if (!isAuthenticated || user?.role !== 'ORGANIZER') setShowLoginModal(true);
    else fetchSubscriptionAndEventCount();
  }, [isAuthenticated, user]);

  const fetchSubscriptionAndEventCount = async () => {
    try {
      const subResponse = await api.get('/subscriptions/current');
      setSubscription(subResponse.data);
      const eventsResponse = await api.get('/events/my-events');
      const events = Array.isArray(eventsResponse.data) ? eventsResponse.data : eventsResponse.data.content || [];
      setEventCount(events.filter(e => e.status === 'DRAFT' || e.status === 'PUBLISHED').length);
    } catch (err) { console.error(err); }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (formErrors[name]) setFormErrors(prev => ({ ...prev, [name]: '' }));
  };

  const validateStep1 = () => {
    const errors = {};
    if (!formData.title.trim()) errors.title = 'Event title is required';
    else if (formData.title.trim().length < 5) errors.title = 'Title must be at least 5 characters';
    if (!formData.description.trim()) errors.description = 'Description is required';
    else if (formData.description.trim().length < 20) errors.description = 'Description must be at least 20 characters';
    return errors;
  };

  const validateStep2 = () => {
    const errors = {};
    if (!formData.date) errors.date = 'Event date is required';
    if (!formData.time) errors.time = 'Event time is required';
    if (!formData.location.trim()) errors.location = 'Location is required';
    if (!formData.applicationDeadline) errors.applicationDeadline = 'Application deadline is required';
    else if (new Date(formData.applicationDeadline) >= new Date(formData.date)) errors.applicationDeadline = 'Deadline must be before event date';
    return errors;
  };

  const validateStep3 = () => {
    const errors = {};
    if (!formData.requiredVolunteers) errors.requiredVolunteers = 'Number of volunteers is required';
    else if (parseInt(formData.requiredVolunteers) < 1) errors.requiredVolunteers = 'Must require at least 1 volunteer';
    const male = parseInt(formData.requiredMaleVolunteers || 0);
    const female = parseInt(formData.requiredFemaleVolunteers || 0);
    const total = parseInt(formData.requiredVolunteers || 0);
    if (male < 0) errors.requiredMaleVolunteers = 'Cannot be negative';
    if (female < 0) errors.requiredFemaleVolunteers = 'Cannot be negative';
    if ((male + female) > total) errors.requiredMaleVolunteers = 'Male + Female count cannot exceed total volunteers';
    if (!formData.paymentPerVolunteer) errors.paymentPerVolunteer = 'Payment amount is required';
    else if (parseFloat(formData.paymentPerVolunteer) < 0) errors.paymentPerVolunteer = 'Payment cannot be negative';
    if (male > 0 && !formData.paymentPerMaleVolunteer) errors.paymentPerMaleVolunteer = 'Payment for male volunteers is required';
    if (female > 0 && !formData.paymentPerFemaleVolunteer) errors.paymentPerFemaleVolunteer = 'Payment for female volunteers is required';
    return errors;
  };

  const handleNextStep = () => {
    setError('');
    const errors = step === 1 ? validateStep1() : step === 2 ? validateStep2() : {};
    if (Object.keys(errors).length > 0) { setFormErrors(errors); return; }
    setStep(step + 1);
  };

  const handleSubmit = async (e) => {
    e.preventDefault(); setError('');
    if (subscription?.tier === 'FREE' && eventCount >= 3) { setShowLimitModal(true); return; }
    const errors = validateStep3();
    if (Object.keys(errors).length > 0) { setFormErrors(errors); return; }
    setLoading(true);
    try {
      await api.post('/events', {
        title: formData.title, description: formData.description, date: formData.date,
        time: formData.time, location: formData.location,
        requiredVolunteers: parseInt(formData.requiredVolunteers),
        requiredMaleVolunteers: parseInt(formData.requiredMaleVolunteers || 0),
        requiredFemaleVolunteers: parseInt(formData.requiredFemaleVolunteers || 0),
        paymentPerVolunteer: parseFloat(formData.paymentPerVolunteer),
        paymentPerMaleVolunteer: formData.paymentPerMaleVolunteer ? parseFloat(formData.paymentPerMaleVolunteer) : null,
        paymentPerFemaleVolunteer: formData.paymentPerFemaleVolunteer ? parseFloat(formData.paymentPerFemaleVolunteer) : null,
        category: formData.category, applicationDeadline: formData.applicationDeadline,
      });
      toast.success('Event created successfully!');
      navigate('/my-events');
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to create event.');
    } finally { setLoading(false); }
  };

  const inputClass = (field) =>
    `w-full px-4 py-3 bg-[#ebf2fa] border rounded-xl text-[#111827] placeholder-[#6B7280] focus:outline-none focus:border-[#807aeb] transition ${formErrors[field] ? 'border-[#EF4444]' : 'border-[#807aeb]/30'}`;

  return (
    <div className="min-h-screen bg-[#ebf2fa] animate-fade-in">
      {showLoginModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl border border-[#807aeb]/10 shadow-xl p-8 max-w-md w-full animate-slide-up">
            <h2 className="text-2xl font-bold text-[#111827] mb-3">Login Required</h2>
            <p className="text-[#6B7280] mb-6">
              You need to be logged in as an <span className="font-semibold text-[#807aeb]">Organizer</span> to post events.
            </p>
            <div className="flex gap-3">
              <button onClick={() => navigate('/login')}
                className="flex-1 py-3 bg-[#807aeb] text-white font-semibold rounded-xl hover:bg-[#6c66d4] transition">
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
        <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-[#111827] mb-1">Create Event</h1>
            <p className="text-[#6B7280]">Post a new volunteer opportunity</p>
          </div>

          {/* Subscription CTA — shown on FREE tier */}
          {subscription?.tier === 'FREE' && (
            <div className="mb-6 flex items-center justify-between gap-4 p-4 bg-[#807aeb]/5 border border-[#807aeb]/20 rounded-2xl">
              <div className="flex items-center gap-3">
                <i className="bx bxs-crown text-2xl text-[#807aeb]" />
                <div>
                  <p className="text-sm font-semibold text-[#111827]">
                    {eventCount}/3 free events used
                  </p>
                  <p className="text-xs text-[#6B7280]">Upgrade to post unlimited events</p>
                </div>
              </div>
              <a href="/subscription"
                className="px-4 py-2 bg-[#807aeb] text-white text-sm font-semibold rounded-xl hover:bg-[#6c66d4] transition flex-shrink-0">
                Upgrade
              </a>
            </div>
          )}

          {/* Step indicator */}
          <div className="mb-8">
            <div className="flex items-center justify-between mb-3">
              {[1, 2, 3].map((s) => (
                <React.Fragment key={s}>
                  <div className={`flex items-center justify-center w-10 h-10 rounded-full font-semibold text-sm transition ${step >= s ? 'bg-[#807aeb] text-white' : 'bg-white text-[#6B7280] border border-[#807aeb]/20'}`}>
                    {s}
                  </div>
                  {s < 3 && <div className={`flex-1 h-1 mx-2 rounded transition ${step > s ? 'bg-[#807aeb]' : 'bg-[#807aeb]/20'}`} />}
                </React.Fragment>
              ))}
            </div>
            <p className="text-sm text-[#6B7280] text-center">
              {step === 1 ? 'Basic Information' : step === 2 ? 'Event Details' : 'Volunteer & Payment'}
            </p>
          </div>

          {/* Form card */}
          <div className="bg-white rounded-2xl border border-[#807aeb]/10 shadow-sm p-8 animate-slide-up">
            {error && (
              <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-xl">
                <p className="text-[#EF4444] text-sm">{error}</p>
              </div>
            )}

            <form onSubmit={step === 3 ? handleSubmit : (e) => { e.preventDefault(); handleNextStep(); }} className="space-y-5">
              {step === 1 && (
                <>
                  <div>
                    <label className="block text-sm font-medium text-[#111827] mb-2">Event Title</label>
                    <input type="text" name="title" value={formData.title} onChange={handleChange}
                      placeholder="e.g., Community Park Cleanup" className={inputClass('title')} />
                    {formErrors.title && <p className="mt-1 text-xs text-[#EF4444]">{formErrors.title}</p>}
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-[#111827] mb-2">Description</label>
                    <textarea name="description" value={formData.description} onChange={handleChange} rows="5"
                      placeholder="Describe the event, what volunteers will do, and any requirements..."
                      className={`${inputClass('description')} resize-none`} />
                    {formErrors.description && <p className="mt-1 text-xs text-[#EF4444]">{formErrors.description}</p>}
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-[#111827] mb-2">Category</label>
                    <select name="category" value={formData.category} onChange={handleChange}
                      className="w-full px-4 py-3 bg-[#ebf2fa] border border-[#807aeb]/30 rounded-xl text-[#111827] focus:outline-none focus:border-[#807aeb] transition">
                      <option value="community">Community</option>
                      <option value="environment">Environment</option>
                      <option value="education">Education</option>
                      <option value="health">Health</option>
                      <option value="other">Other</option>
                    </select>
                  </div>
                </>
              )}

              {step === 2 && (
                <>
                  <div>
                    <label className="block text-sm font-medium text-[#111827] mb-2">Event Date</label>
                    <input type="date" name="date" value={formData.date} onChange={handleChange} className={inputClass('date')} />
                    {formErrors.date && <p className="mt-1 text-xs text-[#EF4444]">{formErrors.date}</p>}
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-[#111827] mb-2">Event Time</label>
                    <input type="time" name="time" value={formData.time} onChange={handleChange} className={inputClass('time')} />
                    {formErrors.time && <p className="mt-1 text-xs text-[#EF4444]">{formErrors.time}</p>}
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-[#111827] mb-2">Location</label>
                    <input type="text" name="location" value={formData.location} onChange={handleChange}
                      placeholder="e.g., Central Park, New York" className={inputClass('location')} />
                    {formErrors.location && <p className="mt-1 text-xs text-[#EF4444]">{formErrors.location}</p>}
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-[#111827] mb-2">Application Deadline</label>
                    <input type="date" name="applicationDeadline" value={formData.applicationDeadline} onChange={handleChange} className={inputClass('applicationDeadline')} />
                    {formErrors.applicationDeadline && <p className="mt-1 text-xs text-[#EF4444]">{formErrors.applicationDeadline}</p>}
                  </div>
                </>
              )}

              {step === 3 && (
                <>
                  <div>
                    <label className="block text-sm font-medium text-[#111827] mb-2">Number of Volunteers Needed</label>
                    <input type="number" name="requiredVolunteers" value={formData.requiredVolunteers} onChange={handleChange}
                      placeholder="e.g., 10" min="1" className={inputClass('requiredVolunteers')} />
                    {formErrors.requiredVolunteers && <p className="mt-1 text-xs text-[#EF4444]">{formErrors.requiredVolunteers}</p>}
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-[#111827] mb-2">
                        <span className="inline-flex items-center gap-1.5">
                          <span className="w-2.5 h-2.5 rounded-full bg-blue-500 inline-block"></span>
                          Male Volunteers
                        </span>
                      </label>
                      <input type="number" name="requiredMaleVolunteers" value={formData.requiredMaleVolunteers} onChange={handleChange}
                        placeholder="0" min="0" className={inputClass('requiredMaleVolunteers')} />
                      {formErrors.requiredMaleVolunteers && <p className="mt-1 text-xs text-[#EF4444]">{formErrors.requiredMaleVolunteers}</p>}
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-[#111827] mb-2">
                        <span className="inline-flex items-center gap-1.5">
                          <span className="w-2.5 h-2.5 rounded-full bg-pink-500 inline-block"></span>
                          Female Volunteers
                        </span>
                      </label>
                      <input type="number" name="requiredFemaleVolunteers" value={formData.requiredFemaleVolunteers} onChange={handleChange}
                        placeholder="0" min="0" className={inputClass('requiredFemaleVolunteers')} />
                      {formErrors.requiredFemaleVolunteers && <p className="mt-1 text-xs text-[#EF4444]">{formErrors.requiredFemaleVolunteers}</p>}
                    </div>
                  </div>
                  {(formData.requiredMaleVolunteers || formData.requiredFemaleVolunteers) && (
                    <p className="text-xs text-[#6B7280] -mt-2">
                      Remaining (any gender): {Math.max(0, parseInt(formData.requiredVolunteers || 0) - parseInt(formData.requiredMaleVolunteers || 0) - parseInt(formData.requiredFemaleVolunteers || 0))} slots
                    </p>
                  )}
                  {/* Payment inputs — all at same level in a grid */}
                  <div className={`grid gap-4 ${
                    parseInt(formData.requiredMaleVolunteers || 0) > 0 && parseInt(formData.requiredFemaleVolunteers || 0) > 0
                      ? 'grid-cols-3'
                      : parseInt(formData.requiredMaleVolunteers || 0) > 0 || parseInt(formData.requiredFemaleVolunteers || 0) > 0
                        ? 'grid-cols-2'
                        : 'grid-cols-1'
                  }`}>
                    <div>
                      <label className="block text-sm font-medium text-[#111827] mb-2">Payment per Volunteer (₹)</label>
                      <input type="number" name="paymentPerVolunteer" value={formData.paymentPerVolunteer} onChange={handleChange}
                        placeholder="e.g., 500" min="0" step="0.01" className={inputClass('paymentPerVolunteer')} />
                      {formErrors.paymentPerVolunteer && <p className="mt-1 text-xs text-[#EF4444]">{formErrors.paymentPerVolunteer}</p>}
                      <p className="mt-1 text-xs text-[#6B7280]">Default / any-gender rate</p>
                    </div>
                    {parseInt(formData.requiredMaleVolunteers || 0) > 0 && (
                      <div>
                        <label className="block text-sm font-medium text-[#111827] mb-2">
                          <span className="inline-flex items-center gap-1.5">
                            <span className="w-2.5 h-2.5 rounded-full bg-blue-500 inline-block"></span>
                            Male Payment (₹)
                          </span>
                        </label>
                        <input type="number" name="paymentPerMaleVolunteer" value={formData.paymentPerMaleVolunteer} onChange={handleChange}
                          placeholder="e.g., 600" min="0" step="0.01" className={inputClass('paymentPerMaleVolunteer')} />
                        {formErrors.paymentPerMaleVolunteer && <p className="mt-1 text-xs text-[#EF4444]">{formErrors.paymentPerMaleVolunteer}</p>}
                      </div>
                    )}
                    {parseInt(formData.requiredFemaleVolunteers || 0) > 0 && (
                      <div>
                        <label className="block text-sm font-medium text-[#111827] mb-2">
                          <span className="inline-flex items-center gap-1.5">
                            <span className="w-2.5 h-2.5 rounded-full bg-pink-500 inline-block"></span>
                            Female Payment (₹)
                          </span>
                        </label>
                        <input type="number" name="paymentPerFemaleVolunteer" value={formData.paymentPerFemaleVolunteer} onChange={handleChange}
                          placeholder="e.g., 550" min="0" step="0.01" className={inputClass('paymentPerFemaleVolunteer')} />
                        {formErrors.paymentPerFemaleVolunteer && <p className="mt-1 text-xs text-[#EF4444]">{formErrors.paymentPerFemaleVolunteer}</p>}
                      </div>
                    )}
                  </div>
                  <div className="p-4 bg-[#807aeb]/5 border border-[#807aeb]/20 rounded-xl">
                    <p className="text-sm text-[#6B7280] mb-1">Total Budget</p>
                    <p className="text-2xl font-bold text-[#807aeb]">
                      ₹{(() => {
                        const total = parseInt(formData.requiredVolunteers || 0);
                        const male = parseInt(formData.requiredMaleVolunteers || 0);
                        const female = parseInt(formData.requiredFemaleVolunteers || 0);
                        const other = Math.max(0, total - male - female);
                        const maleAmt = male * parseFloat(formData.paymentPerMaleVolunteer || formData.paymentPerVolunteer || 0);
                        const femaleAmt = female * parseFloat(formData.paymentPerFemaleVolunteer || formData.paymentPerVolunteer || 0);
                        const otherAmt = other * parseFloat(formData.paymentPerVolunteer || 0);
                        return (maleAmt + femaleAmt + otherAmt).toFixed(2);
                      })()}
                    </p>
                  </div>
                </>
              )}

              <div className="flex gap-3 pt-4">
                {step > 1 && (
                  <button type="button" onClick={() => setStep(step - 1)}
                    className="flex-1 py-3 bg-[#ebf2fa] text-[#111827] font-semibold rounded-xl hover:bg-gray-200 transition">
                    ← Back
                  </button>
                )}
                <button type="submit" disabled={loading}
                  className="flex-1 py-3 bg-[#807aeb] text-white font-semibold rounded-xl hover:bg-[#6c66d4] transition disabled:opacity-50">
                  {loading ? 'Creating...' : step === 3 ? 'Create Event →' : 'Next →'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <SubscriptionLimitModal isOpen={showLimitModal} onClose={() => setShowLimitModal(false)}
        type="events" currentCount={eventCount} limit={3} />
    </div>
  );
};

export default CreateEvent;
