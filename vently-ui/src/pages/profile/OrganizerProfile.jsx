import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { useAuthStore } from '../../store/authStore';
import { RatingsDisplay } from '../../components/ratings/RatingsDisplay';
import api from '../../api/axios';

const EmailOtpModal = ({ email, onClose, onVerified }) => {
  const [otp, setOtp] = React.useState('');
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState('');
  const [sent, setSent] = React.useState(false);
  const [countdown, setCountdown] = React.useState(0);

  const startCountdown = () => {
    setCountdown(60);
    const interval = setInterval(() => {
      setCountdown(c => { if (c <= 1) { clearInterval(interval); return 0; } return c - 1; });
    }, 1000);
  };

  const handleSend = async () => {
    setLoading(true); setError('');
    try {
      await api.post('/auth/send-email-otp', { email });
      setSent(true); startCountdown();
    } catch (err) { setError(err.response?.data?.message || 'Failed to send code.'); }
    finally { setLoading(false); }
  };

  const handleVerify = async () => {
    if (otp.length < 6) { setError('Enter the 6-digit code'); return; }
    setLoading(true); setError('');
    try {
      await api.post('/auth/verify-email-otp', { email, otp });
      onVerified();
    } catch (err) { setError(err.response?.data?.message || 'Invalid code. Try again.'); }
    finally { setLoading(false); }
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl border border-[#807aeb]/20 p-6 w-full max-w-sm shadow-xl">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold text-[#111827]">Verify Email</h2>
          <button onClick={onClose} className="text-[#6B7280] hover:text-[#111827] transition text-xl">✕</button>
        </div>
        {error && <p className="text-[#EF4444] text-sm mb-4 p-3 bg-red-50 rounded-xl">{error}</p>}
        {!sent ? (
          <>
            <p className="text-[#6B7280] text-sm mb-4">We'll send a 6-digit code to <span className="font-semibold text-[#111827]">{email}</span></p>
            <button onClick={handleSend} disabled={loading}
              className="w-full py-3 bg-[#807aeb] text-white rounded-xl font-semibold hover:bg-[#6b64d4] transition disabled:opacity-50">
              {loading ? 'Sending...' : 'Send Code'}
            </button>
          </>
        ) : (
          <>
            <p className="text-[#6B7280] text-sm mb-4">Code sent to <span className="font-semibold text-[#111827]">{email}</span></p>
            <input type="text" inputMode="numeric" maxLength={6} value={otp}
              onChange={e => { setOtp(e.target.value.replace(/\D/g, '').slice(0, 6)); setError(''); }}
              placeholder="000000"
              className="w-full px-4 py-3 bg-[#ebf2fa] border border-[#807aeb]/20 rounded-xl text-[#111827] text-center text-2xl tracking-widest focus:outline-none focus:border-[#807aeb] mb-4" />
            <button onClick={handleVerify} disabled={loading || otp.length < 6}
              className="w-full py-3 bg-[#10B981] text-white rounded-xl font-semibold hover:bg-[#059669] transition disabled:opacity-50 mb-3">
              {loading ? 'Verifying...' : 'Verify'}
            </button>
            <button onClick={handleSend} disabled={countdown > 0}
              className="w-full py-2 text-[#6B7280] text-sm hover:text-[#807aeb] transition disabled:opacity-40">
              {countdown > 0 ? `Resend in ${countdown}s` : 'Resend code'}
            </button>
          </>
        )}
      </div>
    </div>
  );
};

const OrganizerProfile = () => {
  const { userId } = useParams();
  const { user: currentUser } = useAuth();
  const { updateUser } = useAuthStore();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [editData, setEditData] = useState({});
  const [showEmailOtpModal, setShowEmailOtpModal] = useState(false);
  const [showIdentityForm, setShowIdentityForm] = useState(false);
  const [identityData, setIdentityData] = useState({ fullName: '', gender: '', dateOfBirth: '' });
  const [savingIdentity, setSavingIdentity] = useState(false);

  const isOwnProfile = !userId || userId === currentUser?.id?.toString();

  useEffect(() => { fetchProfile(); }, [userId]);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const endpoint = userId ? `/users/${userId}` : '/users/profile';
      const response = await api.get(endpoint);
      const data = response.data;
      setProfile({ ...data, name: data.fullName || 'Organizer' });
      setEditData({ ...data, name: data.fullName || 'Organizer' });
      setIdentityData({ fullName: data.fullName || '', gender: data.gender || '', dateOfBirth: data.dateOfBirth || '' });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleEditToggle = () => {
    if (isEditing) setEditData(profile);
    setIsEditing(!isEditing);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setEditData(prev => ({ ...prev, [name]: value }));
  };

  const handleSaveProfile = async () => {
    try {
      setLoading(true);
      const res = await api.put('/users/profile', {
        organizationName: editData.organizationName,
        organizationDescription: editData.organizationDescription,
        phone: editData.phone,
      });
      setProfile(prev => ({ ...prev, ...res.data, name: res.data.fullName || prev.name }));
      setIsEditing(false);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const identityComplete = profile && profile.fullName && profile.gender && profile.dateOfBirth;

  const handleSaveIdentity = async () => {
    if (!identityData.fullName.trim() || !identityData.gender || !identityData.dateOfBirth) {
      setError('Please fill in all identity fields.');
      return;
    }
    setSavingIdentity(true);
    try {
      const res = await api.put('/users/profile', {
        fullName: identityData.fullName,
        gender: identityData.gender,
        dateOfBirth: identityData.dateOfBirth,
      });
      const data = res.data;
      setProfile(prev => ({ ...prev, fullName: data.fullName, name: data.fullName, gender: data.gender, dateOfBirth: data.dateOfBirth }));
      setShowIdentityForm(false);
    } catch (err) { setError(err.message); }
    finally { setSavingIdentity(false); }
  };

  const handleProfilePictureUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      const formData = new FormData();
      formData.append('file', file);
      const res = await api.post('/users/profile-picture', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
      const url = res.data.profilePictureUrl;
      setProfile(prev => ({ ...prev, profilePictureUrl: url }));
      setEditData(prev => ({ ...prev, profilePictureUrl: url }));
      updateUser({ profilePictureUrl: url });
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  const handleEmailVerified = () => {
    setProfile(prev => ({ ...prev, emailVerified: true }));
    updateUser({ emailVerified: true });
    setShowEmailOtpModal(false);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-[#ebf2fa] py-8">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="animate-pulse space-y-4">
            <div className="h-32 bg-white rounded-2xl" />
            <div className="h-4 bg-white rounded w-3/4" />
            <div className="h-4 bg-white rounded w-1/2" />
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-[#ebf2fa] py-8">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="p-4 bg-red-50 border border-red-200 rounded-2xl">
            <p className="text-[#EF4444]">{error}</p>
          </div>
        </div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="min-h-screen bg-[#ebf2fa] py-8">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <p className="text-[#6B7280]">Profile not found</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#ebf2fa] py-8 animate-fade-in">
      {showEmailOtpModal && (
        <EmailOtpModal
          email={profile?.email}
          onClose={() => setShowEmailOtpModal(false)}
          onVerified={handleEmailVerified}
        />
      )}
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">



        {/* No phone number banner */}
        {isOwnProfile && profile && !profile.phone && (
          <div className="mb-4 p-4 bg-blue-50 border border-blue-200 rounded-2xl flex items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <span className="text-blue-500">📱</span>
              <p className="text-blue-700 text-sm font-medium">Add your mobile number so volunteers can reach you directly.</p>
            </div>
            <button onClick={() => setIsEditing(true)}
              className="px-4 py-2 bg-blue-500 text-white rounded-xl text-sm font-semibold hover:bg-blue-400 transition flex-shrink-0">
              Add Now
            </button>
          </div>
        )}

        {error && (
          <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-xl">
            <p className="text-[#EF4444] text-sm">{error}</p>
          </div>
        )}

        {/* Header Card */}
        <div className="bg-white rounded-2xl border border-[#807aeb]/10 p-8 mb-8 shadow-sm card-hover">
          <div className="flex flex-col sm:flex-row gap-8 items-start">
            {/* Profile Picture */}
            <div className="relative flex-shrink-0">
              <div className="w-32 h-32 bg-[#807aeb]/20 rounded-2xl overflow-hidden">
                {profile.profilePictureUrl ? (
                  <img
                    src={`${profile.profilePictureUrl}?t=${Date.now()}`}
                    alt={profile.organizationName}
                    className="w-full h-full object-cover"
                    crossOrigin="anonymous"
                    onError={(e) => { e.target.style.display = 'none'; e.target.nextSibling.style.display = 'flex'; }}
                  />
                ) : null}
                <div className={`w-full h-full flex items-center justify-center text-[#807aeb] text-4xl font-bold ${profile.profilePictureUrl ? 'hidden' : ''}`}>
                  {(profile.organizationName || profile.fullName || 'O')?.charAt(0).toUpperCase()}
                </div>
              </div>
              {isOwnProfile && isEditing && (
                <label className="absolute bottom-0 right-0 p-2 bg-[#807aeb] rounded-full cursor-pointer hover:bg-[#6c66d4] transition">
                  <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
                  </svg>
                  <input type="file" accept="image/*" onChange={handleProfilePictureUpload} className="hidden" />
                </label>
              )}
            </div>

            {/* Profile Info */}
            <div className="flex-1">
              <div className="flex items-start justify-between mb-4">
                <div>
                  <h1 className="text-3xl font-bold text-[#111827] mb-2">{profile.organizationName}</h1>
                  <p className="text-[#6B7280]">{profile.email}</p>
                  {profile.phone && (
                    <div className="flex items-center gap-2 mt-1">
                      <p className="text-[#6B7280] text-sm">{profile.phone}</p>
                      {profile.phoneVerified
                        ? <span className="text-xs text-[#10B981] bg-[#10B981]/10 px-2 py-0.5 rounded-full">✓ Verified</span>
                        : <span className="text-xs text-yellow-600 bg-yellow-100 px-2 py-0.5 rounded-full">Unverified</span>}
                    </div>
                  )}
                </div>
                {isOwnProfile && (
                  <button onClick={handleEditToggle}
                    className="px-4 py-2 bg-[#807aeb] text-white rounded-xl font-medium hover:bg-[#6c66d4] transition duration-200">
                    {isEditing ? 'Cancel' : 'Edit Profile'}
                  </button>
                )}
              </div>

              {/* Verification Badge */}
              {profile.isVerified && (
                <div className="flex items-center gap-2 mb-4">
                  <svg className="w-5 h-5 text-[#10B981]" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M6.267 3.455a3.066 3.066 0 001.745-.723 3.066 3.066 0 013.976 0 3.066 3.066 0 001.745.723 3.066 3.066 0 012.812 2.812c.051.643.304 1.254.723 1.745a3.066 3.066 0 010 3.976 3.066 3.066 0 00-.723 1.745 3.066 3.066 0 01-2.812 2.812 3.066 3.066 0 00-1.745.723 3.066 3.066 0 01-3.976 0 3.066 3.066 0 00-1.745-.723 3.066 3.066 0 01-2.812-2.812 3.066 3.066 0 00-.723-1.745 3.066 3.066 0 010-3.976 3.066 3.066 0 00.723-1.745 3.066 3.066 0 012.812-2.812zm7.44 5.252a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                  </svg>
                  <span className="text-[#10B981] font-medium">Verified</span>
                </div>
              )}

              {/* Stats */}
              <div className="grid grid-cols-3 gap-4">
                <div className="bg-[#ebf2fa] rounded-xl p-3 border border-[#807aeb]/10">
                  <p className="text-[#6B7280] text-sm">Events Created</p>
                  <p className="text-2xl font-bold text-[#111827]">{profile.totalEventsCreated || 0}</p>
                </div>
                <div className="bg-[#ebf2fa] rounded-xl p-3 border border-[#807aeb]/10">
                  <p className="text-[#6B7280] text-sm">Completed</p>
                  <p className="text-2xl font-bold text-[#10B981]">{profile.totalEventsCompleted || 0}</p>
                </div>
                <div className="bg-[#ebf2fa] rounded-xl p-3 border border-[#807aeb]/10">
                  <p className="text-[#6B7280] text-sm">Avg Rating</p>
                  <p className="text-2xl font-bold text-yellow-500">
                    {profile.averageRating ? profile.averageRating.toFixed(1) : 'N/A'}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Identity Card */}
        {isOwnProfile && (
          <div className="bg-white rounded-2xl border border-[#807aeb]/10 p-6 mb-8 shadow-sm">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-base font-bold text-[#111827]">Personal Identity</h2>
              {!identityComplete && !showIdentityForm && (
                <button onClick={() => setShowIdentityForm(true)}
                  className="px-3 py-1.5 bg-[#807aeb] text-white text-xs font-medium rounded-lg hover:bg-[#6b64d4] transition">
                  Complete Profile
                </button>
              )}
            </div>

            {identityComplete ? (
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div className="bg-[#ebf2fa] rounded-xl p-3">
                  <p className="text-[#6B7280] text-xs mb-1">Full Name</p>
                  <p className="text-[#111827] font-medium text-sm">{profile.fullName}</p>
                </div>
                <div className="bg-[#ebf2fa] rounded-xl p-3">
                  <p className="text-[#6B7280] text-xs mb-1">Gender</p>
                  <p className="text-[#111827] font-medium text-sm capitalize">{profile.gender}</p>
                </div>
                <div className="bg-[#ebf2fa] rounded-xl p-3">
                  <p className="text-[#6B7280] text-xs mb-1">Date of Birth</p>
                  <p className="text-[#111827] font-medium text-sm">{new Date(profile.dateOfBirth).toLocaleDateString('en-IN')}</p>
                </div>
              </div>
            ) : showIdentityForm ? (
              <div className="space-y-4">
                <p className="text-[#6B7280] text-sm">These fields can only be set once and cannot be changed later.</p>
                <div>
                  <label className="block text-sm font-medium text-[#111827] mb-1">Full Name</label>
                  <input type="text" value={identityData.fullName} onChange={e => setIdentityData(p => ({ ...p, fullName: e.target.value }))}
                    className="w-full px-4 py-2 bg-[#ebf2fa] border border-transparent rounded-xl text-[#111827] focus:outline-none focus:border-[#807aeb] transition"
                    placeholder="Your full name" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-[#111827] mb-1">Gender</label>
                  <select value={identityData.gender} onChange={e => setIdentityData(p => ({ ...p, gender: e.target.value }))}
                    className="w-full px-4 py-2 bg-[#ebf2fa] border border-transparent rounded-xl text-[#111827] focus:outline-none focus:border-[#807aeb] transition">
                    <option value="">Select gender</option>
                    <option value="male">Male</option>
                    <option value="female">Female</option>
                    <option value="other">Other</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-[#111827] mb-1">Date of Birth</label>
                  <input type="date" value={identityData.dateOfBirth} onChange={e => setIdentityData(p => ({ ...p, dateOfBirth: e.target.value }))}
                    max={new Date().toISOString().split('T')[0]}
                    className="w-full px-4 py-2 bg-[#ebf2fa] border border-transparent rounded-xl text-[#111827] focus:outline-none focus:border-[#807aeb] transition" />
                </div>
                <div className="flex gap-3">
                  <button onClick={handleSaveIdentity} disabled={savingIdentity}
                    className="px-5 py-2 bg-[#807aeb] text-white rounded-xl text-sm font-medium hover:bg-[#6b64d4] transition disabled:opacity-50">
                    {savingIdentity ? 'Saving...' : 'Save Identity'}
                  </button>
                  <button onClick={() => setShowIdentityForm(false)}
                    className="px-5 py-2 bg-[#ebf2fa] text-[#111827] rounded-xl text-sm font-medium hover:bg-gray-200 transition">
                    Cancel
                  </button>
                </div>
              </div>
            ) : (
              <p className="text-[#6B7280] text-sm">Complete your identity to build trust with volunteers.</p>
            )}
          </div>
        )}

        {/* Edit Form */}
        {isEditing && isOwnProfile && (
          <div className="bg-white rounded-2xl border border-[#807aeb]/10 p-6 mb-8 shadow-sm animate-slide-up">
            <h2 className="text-xl font-bold text-[#111827] mb-6">Edit Profile</h2>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-[#111827] mb-2">Organization Name</label>
                <input type="text" name="organizationName" value={editData.organizationName || ''} onChange={handleInputChange}
                  className="w-full px-4 py-2 bg-[#ebf2fa] border border-[#807aeb]/30 rounded-xl text-[#111827] focus:outline-none focus:border-[#807aeb]" />
              </div>

              <div>
                <label className="block text-sm font-medium text-[#111827] mb-2">
                  Mobile Number
                  {profile?.phone && <span className="ml-2 text-xs text-[#10B981]">✓ Saved</span>}
                </label>
                <input type="tel" name="phone" value={editData.phone || ''} onChange={handleInputChange}
                  placeholder="e.g. 9876543210"
                  className="w-full px-4 py-2 bg-[#ebf2fa] border border-[#807aeb]/30 rounded-xl text-[#111827] focus:outline-none focus:border-[#807aeb]" />
                <p className="text-xs text-[#6B7280] mt-1">Volunteers may use this to contact you directly</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-[#111827] mb-2">Organization Description</label>
                <textarea name="organizationDescription" value={editData.organizationDescription || ''} onChange={handleInputChange} rows="4"
                  className="w-full px-4 py-2 bg-[#ebf2fa] border border-[#807aeb]/30 rounded-xl text-[#111827] focus:outline-none focus:border-[#807aeb]"
                  placeholder="Tell us about your organization..." />
              </div>

              <div className="flex gap-3 pt-4">
                <button onClick={handleSaveProfile} disabled={loading}
                  className="px-6 py-2 bg-[#807aeb] text-white rounded-xl font-medium hover:bg-[#6c66d4] transition duration-200 disabled:opacity-50">
                  {loading ? 'Saving...' : 'Save Changes'}
                </button>
                <button onClick={handleEditToggle}
                  className="px-6 py-2 bg-[#ebf2fa] text-[#6B7280] rounded-xl font-medium hover:bg-[#807aeb]/10 transition duration-200">
                  Cancel
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Profile Details */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
          <div className="lg:col-span-2 space-y-6">
            {profile.organizationDescription && (
              <div className="bg-white rounded-2xl border border-[#807aeb]/10 p-6 shadow-sm card-hover">
                <h2 className="text-lg font-bold text-[#111827] mb-4">About Organization</h2>
                <p className="text-[#6B7280]">{profile.organizationDescription}</p>
              </div>
            )}
            <RatingsDisplay userId={profile.id} />
          </div>

          {/* Quick Stats */}
          <div className="space-y-6">
            <div className="bg-white rounded-2xl border border-[#807aeb]/10 p-6 shadow-sm card-hover">
              <h3 className="text-lg font-bold text-[#111827] mb-4">Quick Stats</h3>
              <div className="space-y-3">
                <div className="flex justify-between items-center">
                  <span className="text-[#6B7280]">Member Since</span>
                  <span className="text-[#111827] font-medium">{new Date(profile.createdAt).toLocaleDateString()}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-[#6B7280]">Total Spent</span>
                  <span className="text-[#111827] font-medium">₹{profile.totalSpent?.toFixed(2) || '0.00'}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-[#6B7280]">Completion Rate</span>
                  <span className="text-[#111827] font-medium">
                    {profile.completionRate ? `${(profile.completionRate * 100).toFixed(0)}%` : 'N/A'}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default OrganizerProfile;
