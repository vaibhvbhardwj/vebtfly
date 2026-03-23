import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import AuthLayout from '../components/AuthLayout';

const Register = () => {
  const [formData, setFormData] = useState({
    fullName: '', email: '', password: '', role: 'VOLUNTEER', gender: '',
  });
  const [acceptedTerms, setAcceptedTerms] = useState(false);
  const [termsError, setTermsError] = useState(false);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    
    if (!acceptedTerms) {
      setTermsError(true);
      return;
    }
    
    setTermsError(false);
    setLoading(true);
    try {
      const res = await api.post('/auth/register', formData);
      localStorage.setItem('token', res.data.token);
      navigate('/');
    } catch (err) {
      alert(err.response?.data || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout title="Start your journey with" subtitle="Join thousands of students and organizers building the future of events.">
      <h2 className="text-3xl font-bold mb-6 text-[#111827]">Create Account</h2>

      <form onSubmit={handleRegister} className="space-y-4">
        {/* Role Selector */}
        <div className="flex bg-[#ebf2fa] p-1 rounded-xl mb-2">
          {['VOLUNTEER', 'ORGANIZER'].map((role) => (
            <button
              key={role}
              type="button"
              onClick={() => setFormData({ ...formData, role })}
              className={`flex-1 py-2.5 rounded-lg font-semibold text-sm transition ${
                formData.role === role
                  ? 'bg-white shadow text-[#807aeb]'
                  : 'text-[#6B7280] hover:text-[#111827]'
              }`}
            >
              {role.charAt(0) + role.slice(1).toLowerCase()}
            </button>
          ))}
        </div>

        <input
          type="text"
          placeholder="Full Name"
          autoComplete="name"
          className="w-full p-4 rounded-xl bg-[#ebf2fa] border border-transparent focus:border-[#807aeb] outline-none transition text-[#111827] placeholder-[#6B7280]"
          onChange={e => setFormData({ ...formData, fullName: e.target.value })}
          required
        />
        <input
          type="email"
          placeholder="Email Address"
          autoComplete="email"
          className="w-full p-4 rounded-xl bg-[#ebf2fa] border border-transparent focus:border-[#807aeb] outline-none transition text-[#111827] placeholder-[#6B7280]"
          onChange={e => setFormData({ ...formData, email: e.target.value })}
          required
        />
        <input
          type="password"
          placeholder="Password"
          autoComplete="new-password"
          className="w-full p-4 rounded-xl bg-[#ebf2fa] border border-transparent focus:border-[#807aeb] outline-none transition text-[#111827] placeholder-[#6B7280]"
          onChange={e => setFormData({ ...formData, password: e.target.value })}
          required
        />

        {/* Gender — required, important for event matching */}
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
          {!formData.gender && (
            <p className="text-xs text-[#6B7280] mt-1.5 ml-1">Please select your gender to continue</p>
          )}
        </div>

        {/* Terms & Conditions Checkbox */}
        <div className="py-2">
          <div className="flex items-start gap-3">
            <div className="flex items-center h-5">
              <input
                id="accept-terms"
                type="checkbox"
                checked={acceptedTerms}
                onChange={(e) => {
                  setAcceptedTerms(e.target.checked);
                  if (e.target.checked) setTermsError(false);
                }}
                className={`w-4 h-4 text-[#807aeb] bg-[#ebf2fa] rounded focus:ring-[#807aeb] focus:ring-2 transition ${
                  termsError ? 'border-[#EF4444] border-2' : 'border-[#807aeb]/30'
                }`}
                required
              />
            </div>
            <label htmlFor="accept-terms" className="text-sm text-[#6B7280] leading-5">
              I agree to the{' '}
              <Link to="/terms" target="_blank" className="text-[#807aeb] hover:underline font-medium">
                Terms & Conditions
              </Link>
              ,{' '}
              <Link to="/privacy" target="_blank" className="text-[#807aeb] hover:underline font-medium">
                Privacy Policy
              </Link>
              , and{' '}
              <Link to="/refunds" target="_blank" className="text-[#807aeb] hover:underline font-medium">
                Refund Policy
              </Link>
            </label>
          </div>
          {termsError && (
            <p className="text-[#EF4444] text-xs mt-2 ml-7">
              <i className="bx bx-error-circle mr-1" />
              Please accept the Terms & Conditions and policies to continue
            </p>
          )}
        </div>

        <button
          type="submit"
          disabled={loading || !acceptedTerms || !formData.gender}
          className="w-full bg-[#807aeb] text-white py-4 rounded-xl font-bold text-base hover:bg-[#6b64d4] hover:shadow-lg hover:shadow-[#807aeb]/30 transition disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {loading ? 'Creating Account...' : 'Sign Up'}
        </button>
      </form>

      <p className="mt-6 text-center text-[#6B7280] text-sm">
        Already have an account?{' '}
        <Link to="/login" className="text-[#807aeb] font-semibold hover:underline">
          Login
        </Link>
      </p>
    </AuthLayout>
  );
};

export default Register;
