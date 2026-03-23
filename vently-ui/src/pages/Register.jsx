import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import AuthLayout from '../components/AuthLayout';

const Register = () => {
  const [step, setStep] = useState('form'); // 'form' | 'otp'
  const [formData, setFormData] = useState({
    fullName: '', email: '', password: '', role: 'VOLUNTEER', gender: '', phone: '',
  });
  const [otp, setOtp] = useState('');
  const [otpError, setOtpError] = useState('');
  const [acceptedTerms, setAcceptedTerms] = useState(false);
  const [termsError, setTermsError] = useState(false);
  const [loading, setLoading] = useState(false);
  const [resendCountdown, setResendCountdown] = useState(0);
  const navigate = useNavigate();

  const startCountdown = () => {
    setResendCountdown(60);
    const interval = setInterval(() => {
      setResendCountdown(c => {
        if (c <= 1) { clearInterval(interval); return 0; }
        return c - 1;
      });
    }, 1000);
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    if (!acceptedTerms) { setTermsError(true); return; }
    setTermsError(false);
    setLoading(true);
    try {
      const res = await api.post('/auth/register', formData);
      localStorage.setItem('token', res.data.token);
      // Move to OTP step
      setStep('otp');
      startCountdown();
    } catch (err) {
      alert(err.response?.data?.message || err.response?.data || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    if (otp.length < 6) { setOtpError('Enter the 6-digit code'); return; }
    setLoading(true); setOtpError('');
    try {
      await api.post('/auth/verify-email-otp', { email: formData.email, otp });
      navigate('/');
    } catch (err) {
      setOtpError(err.response?.data?.message || 'Invalid code. Try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleResendOtp = async () => {
    if (resendCountdown > 0) return;
    try {
      await api.post('/auth/send-email-otp', { email: formData.email });
      startCountdown();
    } catch (err) {
      setOtpError('Failed to resend. Try again.');
    }
  };

  const handleSkipVerification = () => {
    navigate('/');
  };

  if (step === 'otp') {
    return (
      <AuthLayout title="One last step" subtitle="Check your email for the verification code.">
        <h2 className="text-3xl font-bold mb-2 text-[#111827]">Verify your email</h2>
        <p className="text-[#6B7280] text-sm mb-6">
          We sent a 6-digit code to <span className="font-semibold text-[#111827]">{formData.email}</span>
        </p>

        <form onSubmit={handleVerifyOtp} className="space-y-4">
          <input
            type="text"
            inputMode="numeric"
            maxLength={6}
            value={otp}
            onChange={e => { setOtp(e.target.value.replace(/\D/g, '').slice(0, 6)); setOtpError(''); }}
            placeholder="000000"
            className="w-full p-4 rounded-xl bg-[#ebf2fa] border border-transparent focus:border-[#807aeb] outline-none transition text-[#111827] text-center text-3xl tracking-[0.5em] font-bold placeholder-[#9CA3AF]"
          />
          {otpError && <p className="text-[#EF4444] text-sm">{otpError}</p>}

          <button
            type="submit"
            disabled={loading || otp.length < 6}
            className="w-full bg-[#807aeb] text-white py-4 rounded-xl font-bold text-base hover:bg-[#6b64d4] hover:shadow-lg hover:shadow-[#807aeb]/30 transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? 'Verifying...' : 'Verify Email'}
          </button>

          <button
            type="button"
            onClick={handleResendOtp}
            disabled={resendCountdown > 0}
            className="w-full py-2 text-[#6B7280] text-sm hover:text-[#807aeb] transition disabled:opacity-40"
          >
            {resendCountdown > 0 ? `Resend code in ${resendCountdown}s` : 'Resend code'}
          </button>

          <button
            type="button"
            onClick={handleSkipVerification}
            className="w-full py-2 text-[#9CA3AF] text-xs hover:text-[#6B7280] transition"
          >
            Skip for now (verify later from profile)
          </button>
        </form>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout title="Start your journey with" subtitle="Join thousands of students and organizers building the future of events.">
      <h2 className="text-3xl font-bold mb-6 text-[#111827]">Create Account</h2>

      <form onSubmit={handleRegister} className="space-y-4">
        {/* Role Selector */}
        <div className="flex bg-[#ebf2fa] p-1 rounded-xl mb-2">
          {['VOLUNTEER', 'ORGANIZER'].map((role) => (
            <button key={role} type="button"
              onClick={() => setFormData({ ...formData, role })}
              className={`flex-1 py-2.5 rounded-lg font-semibold text-sm transition ${
                formData.role === role ? 'bg-white shadow text-[#807aeb]' : 'text-[#6B7280] hover:text-[#111827]'
              }`}>
              {role.charAt(0) + role.slice(1).toLowerCase()}
            </button>
          ))}
        </div>

        <input type="text" placeholder="Full Name" autoComplete="name" required
          className="w-full p-4 rounded-xl bg-[#ebf2fa] border border-transparent focus:border-[#807aeb] outline-none transition text-[#111827] placeholder-[#6B7280]"
          onChange={e => setFormData({ ...formData, fullName: e.target.value })} />

        <input type="email" placeholder="Email Address" autoComplete="email" required
          className="w-full p-4 rounded-xl bg-[#ebf2fa] border border-transparent focus:border-[#807aeb] outline-none transition text-[#111827] placeholder-[#6B7280]"
          onChange={e => setFormData({ ...formData, email: e.target.value })} />

        <input type="password" placeholder="Password (min 8 characters)" autoComplete="new-password" required
          className="w-full p-4 rounded-xl bg-[#ebf2fa] border border-transparent focus:border-[#807aeb] outline-none transition text-[#111827] placeholder-[#6B7280]"
          onChange={e => setFormData({ ...formData, password: e.target.value })} />

        {/* Phone — optional but encouraged */}
        <div>
          <input type="tel" placeholder="Mobile Number (e.g. 9876543210)"
            autoComplete="tel"
            className="w-full p-4 rounded-xl bg-[#ebf2fa] border border-transparent focus:border-[#807aeb] outline-none transition text-[#111827] placeholder-[#6B7280]"
            onChange={e => setFormData({ ...formData, phone: e.target.value })} />
          <p className="text-xs text-[#6B7280] mt-1 ml-1">Recommended — organizers may contact you directly</p>
        </div>

        {/* Gender */}
        <div>
          <label className="block text-sm font-semibold text-[#111827] mb-2">
            Gender <span className="text-[#EF4444]">*</span>
            <span className="ml-2 text-xs font-normal text-[#6B7280]">(required — some events are gender-specific)</span>
          </label>
          <div className="flex gap-3">
            {[
              { value: 'MALE',   label: 'Male',   icon: 'bx bx-male',   active: 'bg-blue-500 text-white border-blue-500' },
              { value: 'FEMALE', label: 'Female', icon: 'bx bx-female', active: 'bg-pink-500 text-white border-pink-500' },
              { value: 'OTHER',  label: 'Other',  icon: 'bx bx-user',   active: 'bg-[#807aeb] text-white border-[#807aeb]' },
            ].map(({ value, label, icon, active }) => (
              <button key={value} type="button"
                onClick={() => setFormData({ ...formData, gender: value })}
                className={`flex-1 flex flex-col items-center gap-1 py-3 rounded-xl border-2 font-medium text-sm transition ${
                  formData.gender === value ? active : 'bg-[#ebf2fa] border-transparent text-[#6B7280] hover:border-[#807aeb]/40'
                }`}>
                <i className={`${icon} text-xl`} />
                {label}
              </button>
            ))}
          </div>
        </div>

        {/* Terms */}
        <div className="py-2">
          <div className="flex items-start gap-3">
            <input id="accept-terms" type="checkbox" checked={acceptedTerms}
              onChange={e => { setAcceptedTerms(e.target.checked); if (e.target.checked) setTermsError(false); }}
              className={`w-4 h-4 mt-0.5 text-[#807aeb] bg-[#ebf2fa] rounded focus:ring-[#807aeb] ${termsError ? 'border-[#EF4444] border-2' : ''}`} />
            <label htmlFor="accept-terms" className="text-sm text-[#6B7280] leading-5">
              I agree to the{' '}
              <Link to="/terms" target="_blank" className="text-[#807aeb] hover:underline font-medium">Terms & Conditions</Link>,{' '}
              <Link to="/privacy" target="_blank" className="text-[#807aeb] hover:underline font-medium">Privacy Policy</Link>, and{' '}
              <Link to="/refunds" target="_blank" className="text-[#807aeb] hover:underline font-medium">Refund Policy</Link>
            </label>
          </div>
          {termsError && <p className="text-[#EF4444] text-xs mt-2 ml-7">Please accept the policies to continue</p>}
        </div>

        <button type="submit" disabled={loading || !acceptedTerms || !formData.gender}
          className="w-full bg-[#807aeb] text-white py-4 rounded-xl font-bold text-base hover:bg-[#6b64d4] hover:shadow-lg hover:shadow-[#807aeb]/30 transition disabled:opacity-50 disabled:cursor-not-allowed">
          {loading ? 'Creating Account...' : 'Sign Up'}
        </button>
      </form>

      <p className="mt-6 text-center text-[#6B7280] text-sm">
        Already have an account?{' '}
        <Link to="/login" className="text-[#807aeb] font-semibold hover:underline">Login</Link>
      </p>
    </AuthLayout>
  );
};

export default Register;
