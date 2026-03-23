import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { useAuthStore } from '../../store/authStore';
import { RatingsDisplay } from '../../components/ratings/RatingsDisplay';
import api from '../../api/axios';
const compressImage = async (file, maxWidth = 800, maxHeight = 800, quality = 0.85) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = (event) => {
      const img = new Image();
      img.src = event.target.result;
      img.onload = () => {
        const canvas = document.createElement('canvas');
        let width = img.width;
        let height = img.height;
        if (width > height) {
          if (width > maxWidth) { height = Math.round((height * maxWidth) / width); width = maxWidth; }
        } else {
          if (height > maxHeight) { width = Math.round((width * maxHeight) / height); height = maxHeight; }
        }
        canvas.width = width;
        canvas.height = height;
        canvas.getContext('2d').drawImage(img, 0, 0, width, height);
        canvas.toBlob((blob) => resolve(blob), 'image/jpeg', quality);
      };
      img.onerror = () => reject(new Error('Failed to load image'));
    };
    reader.onerror = () => reject(new Error('Failed to read file'));
  });
};

// Phone OTP Modal — uses backend SNS OTP (no external dependencies)
const PhoneOtpModal = ({ phone, onClose, onVerified }) => {
  const [step, setStep] = useState('enter_phone');
  const [phoneInput, setPhoneInput] = useState(phone || '');
  const [otp, setOtp] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [countdown, setCountdown] = useState(0);
  const [devOtp, setDevOtp] = useState('');

  useEffect(() => {
    if (countdown > 0) {
      const t = setTimeout(() => setCountdown(c => c - 1), 1000);
      return () => clearTimeout(t);
    }
  }, [countdown]);

  const handleSendOtp = async () => {
    if (!phoneInput.trim()) { setError('Enter a phone number'); return; }
    setLoading(true); setError('');
    try {
      const res = await api.post('/users/phone/send-otp', { phone: phoneInput.trim() });
      // dev mode: backend returns OTP in response when SNS not configured
      if (res.data.devOtp) setDevOtp(res.data.devOtp);
      setStep('enter_otp');
      setCountdown(60);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to send OTP. Check the phone number and try again.');
    } finally { setLoading(false); }
  };

  const handleVerifyOtp = async () => {
    if (!otp.trim() || otp.length < 6) { setError('Enter the 6-digit OTP'); return; }
    setLoading(true); setError('');
    try {
      const res = await api.post('/users/phone/verify-otp', { otp });
      onVerified(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid OTP. Try again.');
    } finally { setLoading(false); }
  };

  const handleResend = () => {
    setStep('enter_phone');
    setOtp('');
    setError('');
    setDevOtp('');
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl border border-[#807aeb]/20 shadow-xl p-6 w-full max-w-sm">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold text-[#111827]">Verify Phone Number</h2>
          <button onClick={onClose} className="text-[#6B7280] hover:text-[#111827] transition">
            <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
            </svg>
          </button>
        </div>
        {error && <p className="text-[#EF4444] text-sm mb-4 p-3 bg-red-50 rounded-lg border border-red-100">{error}</p>}
        {step === 'enter_phone' ? (
          <>
            <p className="text-[#6B7280] text-sm mb-4">Enter your mobile number. We'll send a 6-digit OTP via SMS.</p>
            <input
              type="tel"
              value={phoneInput}
              onChange={e => setPhoneInput(e.target.value)}
              placeholder="+91 98765 43210"
              className="w-full px-4 py-3 bg-[#ebf2fa] border border-[#807aeb]/20 rounded-xl text-[#111827] placeholder-[#9CA3AF] focus:outline-none focus:border-[#807aeb] mb-4"
            />
            <button onClick={handleSendOtp} disabled={loading}
              className="w-full py-3 bg-[#807aeb] text-white rounded-xl font-semibold hover:bg-[#6b64d4] transition disabled:opacity-50">
              {loading ? 'Sending...' : 'Send OTP'}
            </button>
          </>
        ) : (
          <>
            <p className="text-[#6B7280] text-sm mb-1">OTP sent to <span className="text-[#111827] font-medium">{phoneInput}</span></p>
            <p className="text-[#9CA3AF] text-xs mb-4">Valid for 10 minutes</p>
            {devOtp && (
              <p className="text-xs text-amber-600 bg-amber-50 border border-amber-200 rounded-lg px-3 py-2 mb-3">
                Dev mode OTP: <span className="font-bold">{devOtp}</span>
              </p>
            )}
            <input
              type="text"
              value={otp}
              onChange={e => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
              placeholder="Enter 6-digit OTP"
              maxLength={6}
              className="w-full px-4 py-3 bg-[#ebf2fa] border border-[#807aeb]/20 rounded-xl text-[#111827] text-center text-2xl tracking-widest placeholder-[#9CA3AF] focus:outline-none focus:border-[#807aeb] mb-4"
            />
            <button onClick={handleVerifyOtp} disabled={loading || otp.length < 6}
              className="w-full py-3 bg-[#10B981] text-white rounded-xl font-semibold hover:bg-[#059669] transition disabled:opacity-50 mb-3">
              {loading ? 'Verifying...' : 'Verify OTP'}
            </button>
            <button onClick={handleResend} disabled={countdown > 0}
              className="w-full py-2 text-[#6B7280] text-sm hover:text-[#807aeb] transition disabled:opacity-40">
              {countdown > 0 ? `Resend OTP in ${countdown}s` : 'Resend OTP'}
            </button>
          </>
        )}
      </div>
    </div>
  );
};

const VolunteerProfile = () => {
  const { userId } = useParams();
  const { user: currentUser } = useAuth();
  const { updateUser } = useAuthStore();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [editData, setEditData] = useState({});
  const [showGallery, setShowGallery] = useState(false);
  const [galleryPhotos, setGalleryPhotos] = useState([]);
  const [uploadingPhoto, setUploadingPhoto] = useState(false);
  const [showOtpModal, setShowOtpModal] = useState(false);
  const [showIdentityForm, setShowIdentityForm] = useState(false);
  const [identityData, setIdentityData] = useState({ fullName: '', gender: '', dateOfBirth: '' });
  const [savingIdentity, setSavingIdentity] = useState(false);

  const isOwnProfile = !userId || userId === currentUser?.id?.toString();

  useEffect(() => { fetchProfile(); }, [userId]);

  const fetchProfile = async () => {
    try {
      setLoading(true); setError(null);
      if (isOwnProfile) {
        const response = await api.get('/users/profile');
        const data = response.data;
        setProfile({
          id: data.id,
          name: data.fullName || '',
          email: data.email,
          phone: data.phone || '',
          phoneVerified: data.phoneVerified || false,
          bio: data.bio || '',
          skills: data.skills || '',
          experience: data.experience || '',
          profilePictureUrl: data.profilePictureUrl || null,
          galleryPhotos: data.galleryPhotos || [],
          isVerified: data.verificationBadge || false,
          totalEventsAttended: data.totalEventsAttended || 0,
          noShowCount: data.noShowCount || 0,
          averageRating: data.averageRating || 0,
          totalEarnings: data.totalEarnings || 0,
          attendanceRate: data.attendanceRate || 0,
          createdAt: data.createdAt || new Date().toISOString(),
          gender: data.gender || '',
          dateOfBirth: data.dateOfBirth || '',
        });
        setGalleryPhotos(data.galleryPhotos || []);
        setIdentityData({ fullName: data.fullName || '', gender: data.gender || '', dateOfBirth: data.dateOfBirth || '' });
        setEditData({ name: data.fullName || '', email: data.email, phone: data.phone || '', bio: data.bio || '', skills: data.skills || '', experience: data.experience || '' });
        return;
      }
      if (userId) {
        const response = await api.get(`/users/${userId}`);
        const data = response.data;
        setProfile({ ...data, name: data.fullName || 'User' });
        setGalleryPhotos(data.galleryPhotos || []);
      }
    } catch (err) {
      console.error('Error fetching profile:', err);
      setError('Unable to load profile. Please try again.');
    } finally { setLoading(false); }
  };

  const handleEditToggle = () => { if (isEditing) setEditData(profile); setIsEditing(!isEditing); };
  const handleInputChange = (e) => { const { name, value } = e.target; setEditData(prev => ({ ...prev, [name]: value })); };

  const handleSaveProfile = async () => {
    try {
      setLoading(true);
      const res = await api.put('/users/profile', { fullName: editData.name, bio: editData.bio, skills: editData.skills, experience: editData.experience });
      const data = res.data;
      setProfile(prev => ({ ...prev, name: data.fullName || prev.name, bio: data.bio, skills: data.skills, experience: data.experience }));
      setIsEditing(false);
    } catch (err) { setError(err.message); }
    finally { setLoading(false); }
  };

  const identityComplete = profile && profile.name && profile.gender && profile.dateOfBirth;

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
      setProfile(prev => ({ ...prev, name: data.fullName, gender: data.gender, dateOfBirth: data.dateOfBirth }));
      setShowIdentityForm(false);
    } catch (err) { setError(err.message); }
    finally { setSavingIdentity(false); }
  };

  const handleProfilePictureUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      setLoading(true);
      const blob = await compressImage(file);
      const compressed = new File([blob], file.name, { type: 'image/jpeg' });
      const formData = new FormData();
      formData.append('file', compressed);
      const res = await api.post('/users/profile-picture', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
      const url = res.data.profilePictureUrl;
      setProfile(prev => ({ ...prev, profilePictureUrl: url }));
      updateUser({ profilePictureUrl: url });
      setError(null);
    } catch (err) { setError(err.response?.data?.message || err.message); }
    finally { setLoading(false); }
  };

  const handleGalleryPhotoUpload = async (e) => {
    const files = e.target.files;
    if (!files || galleryPhotos.length >= 3) { setError('Maximum 3 photos allowed'); return; }
    try {
      setUploadingPhoto(true);
      const slots = 3 - galleryPhotos.length;
      for (const file of Array.from(files).slice(0, slots)) {
        const blob = await compressImage(file);
        const compressed = new File([blob], file.name, { type: 'image/jpeg' });
        const formData = new FormData();
        formData.append('file', compressed);
        const res = await api.post('/users/gallery-photo', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
        setGalleryPhotos(res.data.galleryPhotos || []);
      }
      setError(null);
    } catch (err) { setError(err.response?.data?.message || err.message); }
    finally { setUploadingPhoto(false); }
  };

  const handleRemoveGalleryPhoto = async (photoUrl) => {
    try {
      const res = await api.delete('/users/gallery-photo', { data: { photoUrl } });
      setGalleryPhotos(res.data.galleryPhotos || []);
    } catch (err) { setError(err.response?.data?.message || err.message); }
  };

  const handlePhoneVerified = (updatedProfile) => {
    setProfile(prev => ({ ...prev, phone: updatedProfile.phone, phoneVerified: true }));
    updateUser({ phone: updatedProfile.phone, phoneVerified: true });
    setShowOtpModal(false);
  };

  if (loading) return (
    <div className="min-h-screen bg-[#ebf2fa] py-8">
      <div className="max-w-4xl mx-auto px-4"><div className="animate-pulse space-y-4"><div className="h-32 bg-white rounded-2xl" /><div className="h-4 bg-white rounded w-3/4" /></div></div>
    </div>
  );

  if (!profile) return (
    <div className="min-h-screen bg-[#ebf2fa] py-8">
      <div className="max-w-4xl mx-auto px-4"><p className="text-[#EF4444]">{error || 'Profile not found'}</p></div>
    </div>
  );

  return (
    <div className="min-h-screen bg-[#ebf2fa] py-8 animate-fade-in">
      {showOtpModal && (
        <PhoneOtpModal
          phone={editData.phone || profile.phone}
          onClose={() => setShowOtpModal(false)}
          onVerified={handlePhoneVerified}
        />
      )}

      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">

        {/* Phone verification banner */}
        {isOwnProfile && !profile.phoneVerified && (
          <div className="mb-6 p-4 bg-yellow-50 border border-yellow-200 rounded-2xl flex items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <span className="text-yellow-500">⚠️</span>
              <p className="text-yellow-700 text-sm font-medium">Phone number not verified. Verify to access all features.</p>
            </div>
            <button onClick={() => setShowOtpModal(true)}
              className="px-4 py-2 bg-yellow-500 text-white rounded-xl text-sm font-semibold hover:bg-yellow-400 transition flex-shrink-0">
              Verify Now
            </button>
          </div>
        )}

        {error && (
          <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-xl">
            <p className="text-[#EF4444] text-sm">{error}</p>
          </div>
        )}

        {/* Header Card */}
        <div className="bg-white rounded-2xl border border-[#807aeb]/10 p-8 mb-8 shadow-sm">
          <div className="flex flex-col sm:flex-row gap-8 items-start">
            {/* Profile Picture */}
            <div className="relative flex-shrink-0">
              <div className="w-32 h-32 bg-[#807aeb] rounded-2xl overflow-hidden">
                {profile.profilePictureUrl ? (
                  <img
                    src={`${profile.profilePictureUrl}?t=${Date.now()}`}
                    alt={profile.name}
                    className="w-full h-full object-cover"
                    crossOrigin="anonymous"
                    onError={(e) => { e.target.style.display = 'none'; e.target.nextSibling.style.display = 'flex'; }}
                  />
                ) : null}
                <div className={`w-full h-full flex items-center justify-center text-white text-4xl font-bold ${profile.profilePictureUrl ? 'hidden' : ''}`}>
                  {profile.name?.charAt(0).toUpperCase()}
                </div>
              </div>
              {isOwnProfile && isEditing && (
                <label className="absolute bottom-0 right-0 p-2 bg-blue-600 rounded-full cursor-pointer hover:bg-blue-700 transition">
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
                  <h1 className="text-3xl font-bold text-[#111827] mb-1">{profile.name}</h1>
                  <p className="text-[#6B7280] text-sm">{profile.email}</p>
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
                    className="px-4 py-2 bg-[#807aeb] text-white rounded-xl font-medium hover:bg-[#6b64d4] transition">
                    {isEditing ? 'Cancel' : 'Edit Profile'}
                  </button>
                )}
              </div>

              {profile.isVerified && (
                <div className="flex items-center gap-2 mb-4">
                  <svg className="w-5 h-5 text-green-500" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M6.267 3.455a3.066 3.066 0 001.745-.723 3.066 3.066 0 013.976 0 3.066 3.066 0 001.745.723 3.066 3.066 0 012.812 2.812c.051.643.304 1.254.723 1.745a3.066 3.066 0 010 3.976 3.066 3.066 0 00-.723 1.745 3.066 3.066 0 01-2.812 2.812 3.066 3.066 0 00-1.745.723 3.066 3.066 0 01-3.976 0 3.066 3.066 0 00-1.745-.723 3.066 3.066 0 01-2.812-2.812 3.066 3.066 0 00-.723-1.745 3.066 3.066 0 010-3.976 3.066 3.066 0 00.723-1.745 3.066 3.066 0 012.812-2.812zm7.44 5.252a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                  </svg>
                  <span className="text-green-400 font-medium">Verified</span>
                </div>
              )}

              <div className="grid grid-cols-3 gap-4">
                <div className="bg-[#ebf2fa] rounded-xl p-3">
                  <p className="text-[#6B7280] text-xs">Events Attended</p>
                  <p className="text-2xl font-bold text-[#111827]">{profile.totalEventsAttended || 0}</p>
                </div>
                <div className="bg-[#ebf2fa] rounded-xl p-3">
                  <p className="text-[#6B7280] text-xs">No-Shows</p>
                  <p className="text-2xl font-bold text-[#EF4444]">{profile.noShowCount || 0}</p>
                </div>
                <div className="bg-[#ebf2fa] rounded-xl p-3">
                  <p className="text-[#6B7280] text-xs">Avg Rating</p>
                  <p className="text-2xl font-bold text-yellow-500">{profile.averageRating ? profile.averageRating.toFixed(1) : 'N/A'}</p>
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
                  <p className="text-[#111827] font-medium text-sm">{profile.name}</p>
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
              <p className="text-[#6B7280] text-sm">Complete your identity to get matched with events.</p>
            )}
          </div>
        )}

        {/* Edit Form */}
        {isEditing && isOwnProfile && (
          <div className="bg-white rounded-2xl border border-[#807aeb]/10 p-6 mb-8 shadow-sm">
            <h2 className="text-xl font-bold text-[#111827] mb-6">Edit Profile</h2>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-[#111827] mb-2">Full Name</label>
                <input type="text" name="name" value={editData.name || ''} onChange={handleInputChange}
                  className="w-full px-4 py-2 bg-[#ebf2fa] border border-transparent rounded-xl text-[#111827] focus:outline-none focus:border-[#807aeb] transition" />
              </div>

              {/* Phone with verify button */}
              <div>
                <label className="block text-sm font-medium text-[#111827] mb-2">
                  Phone Number <span className="text-[#EF4444]">*</span>
                  {profile.phoneVerified && <span className="ml-2 text-xs text-[#10B981]">✓ Verified</span>}
                </label>
                <div className="flex gap-2">
                  <input type="tel" name="phone" value={editData.phone || ''} onChange={handleInputChange}
                    placeholder="+91 98765 43210"
                    className="flex-1 px-4 py-2 bg-[#ebf2fa] border border-transparent rounded-xl text-[#111827] focus:outline-none focus:border-[#807aeb] transition" />
                  <button type="button" onClick={() => setShowOtpModal(true)}
                    className={`px-4 py-2 rounded-xl text-sm font-medium transition ${profile.phoneVerified ? 'bg-[#10B981]/10 text-[#10B981] border border-[#10B981]/20' : 'bg-yellow-500 text-white hover:bg-yellow-400'}`}>
                    {profile.phoneVerified ? '✓ Verified' : 'Verify'}
                  </button>
                </div>
                {!profile.phoneVerified && (
                  <p className="text-yellow-600 text-xs mt-1">Phone verification is required to use the platform.</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-[#111827] mb-2">Bio</label>
                <textarea name="bio" value={editData.bio || ''} onChange={handleInputChange} rows="4"
                  className="w-full px-4 py-2 bg-[#ebf2fa] border border-transparent rounded-xl text-[#111827] focus:outline-none focus:border-[#807aeb] transition"
                  placeholder="Tell us about yourself..." />
              </div>
              <div>
                <label className="block text-sm font-medium text-[#111827] mb-2">Skills</label>
                <input type="text" name="skills" value={editData.skills || ''} onChange={handleInputChange}
                  className="w-full px-4 py-2 bg-[#ebf2fa] border border-transparent rounded-xl text-[#111827] focus:outline-none focus:border-[#807aeb] transition"
                  placeholder="e.g., Event Planning, Customer Service" />
              </div>
              <div>
                <label className="block text-sm font-medium text-[#111827] mb-2">Experience</label>
                <textarea name="experience" value={editData.experience || ''} onChange={handleInputChange} rows="3"
                  className="w-full px-4 py-2 bg-[#ebf2fa] border border-transparent rounded-xl text-[#111827] focus:outline-none focus:border-[#807aeb] transition"
                  placeholder="Describe your relevant experience..." />
              </div>
              <div className="flex gap-3 pt-4">
                <button onClick={handleSaveProfile} disabled={loading}
                  className="px-6 py-2 bg-[#807aeb] text-white rounded-xl font-medium hover:bg-[#6b64d4] transition disabled:opacity-50">
                  {loading ? 'Saving...' : 'Save Changes'}
                </button>
                <button onClick={handleEditToggle}
                  className="px-6 py-2 bg-[#ebf2fa] text-[#111827] rounded-xl font-medium hover:bg-gray-200 transition">
                  Cancel
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Profile Details */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
          <div className="lg:col-span-2 space-y-6">
            {/* Gallery */}
            {isOwnProfile && (
              <div className="bg-white rounded-2xl border border-[#807aeb]/10 p-6 shadow-sm">
                <div className="flex justify-between items-center mb-4">
                  <h2 className="text-base font-bold text-[#111827]">Photo Gallery</h2>
                  <button onClick={() => setShowGallery(!showGallery)}
                    className="text-sm px-3 py-1 bg-[#807aeb] text-white rounded-lg hover:bg-[#6b64d4] transition">
                    {showGallery ? 'Hide' : 'Manage'}
                  </button>
                </div>
                {showGallery && (
                  <div className="mb-4 p-4 bg-[#ebf2fa] rounded-xl border border-[#807aeb]/10">
                    <label className="block mb-3">
                      <div className="border-2 border-dashed border-[#807aeb]/30 rounded-xl p-6 text-center cursor-pointer hover:border-[#807aeb] transition">
                        <p className="text-[#6B7280] text-sm">Click to upload photos ({galleryPhotos.length}/3)</p>
                        <p className="text-[#6B7280]/60 text-xs mt-1">Images compressed automatically</p>
                      </div>
                      <input type="file" multiple accept="image/*" onChange={handleGalleryPhotoUpload}
                        disabled={uploadingPhoto || galleryPhotos.length >= 3} className="hidden" />
                    </label>
                  </div>
                )}
                {galleryPhotos.length > 0 ? (
                  <div className="grid grid-cols-3 gap-3">
                    {galleryPhotos.map((url, idx) => (
                      <div key={idx} className="relative group">
                        <img src={`${url}?t=${Date.now()}`} alt={`Gallery ${idx + 1}`}
                          className="w-full h-32 object-cover rounded-xl" crossOrigin="anonymous" />
                        {isEditing && (
                          <button onClick={() => handleRemoveGalleryPhoto(url)}
                            className="absolute top-1 right-1 p-1 bg-[#EF4444] text-white rounded-lg opacity-0 group-hover:opacity-100 transition text-xs">
                            ✕
                          </button>
                        )}
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-[#6B7280] text-sm">No photos yet. Add up to 3 photos!</p>
                )}
              </div>
            )}

            {profile.bio && (
              <div className="bg-white rounded-2xl border border-[#807aeb]/10 p-6 shadow-sm">
                <h2 className="text-base font-bold text-[#111827] mb-3">About</h2>
                <p className="text-[#6B7280]">{profile.bio}</p>
              </div>
            )}
            {profile.skills && (
              <div className="bg-white rounded-2xl border border-[#807aeb]/10 p-6 shadow-sm">
                <h2 className="text-base font-bold text-[#111827] mb-3">Skills</h2>
                <div className="flex flex-wrap gap-2">
                  {(Array.isArray(profile.skills) ? profile.skills : profile.skills.split(',')).map((skill, idx) => (
                    <span key={idx} className="px-3 py-1 bg-[#807aeb]/10 text-[#807aeb] rounded-full text-sm font-medium border border-[#807aeb]/20">{skill.trim()}</span>
                  ))}
                </div>
              </div>
            )}
            {profile.experience && (
              <div className="bg-white rounded-2xl border border-[#807aeb]/10 p-6 shadow-sm">
                <h2 className="text-base font-bold text-[#111827] mb-3">Experience</h2>
                <p className="text-[#6B7280]">{profile.experience}</p>
              </div>
            )}
            <RatingsDisplay userId={profile.id} />
          </div>

          <div className="space-y-6">
            <div className="bg-white rounded-2xl border border-[#807aeb]/10 p-6 shadow-sm">
              <h3 className="text-base font-bold text-[#111827] mb-4">Quick Stats</h3>
              <div className="space-y-3">
                <div className="flex justify-between items-center">
                  <span className="text-[#6B7280] text-sm">Member Since</span>
                  <span className="text-[#111827] font-medium text-sm">{new Date(profile.createdAt).toLocaleDateString()}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-[#6B7280] text-sm">Total Earnings</span>
                  <span className="text-[#111827] font-medium text-sm">₹{profile.totalEarnings?.toFixed(2) || '0.00'}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-[#6B7280] text-sm">Attendance Rate</span>
                  <span className="text-[#111827] font-medium text-sm">{profile.attendanceRate ? `${(profile.attendanceRate * 100).toFixed(0)}%` : 'N/A'}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default VolunteerProfile;
