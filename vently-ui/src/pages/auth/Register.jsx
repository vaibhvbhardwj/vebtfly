import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { validateEmail, validatePassword, getPasswordStrength } from '../../utils/validators';
import { USER_ROLES } from '../../utils/constants';

const Register = () => {
  const navigate = useNavigate();
  const { register, loading, error, clearError } = useAuth();
  const [step, setStep] = useState(1);
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: USER_ROLES.VOLUNTEER,
    agreeToTerms: false,
  });
  const [formErrors, setFormErrors] = useState({});

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
    if (formErrors[name]) {
      setFormErrors((prev) => ({
        ...prev,
        [name]: '',
      }));
    }
  };

  const validateStep1 = () => {
    const errors = {};

    if (!formData.name.trim()) {
      errors.name = 'Full name is required';
    } else if (formData.name.trim().length < 2) {
      errors.name = 'Name must be at least 2 characters';
    }

    if (!formData.email.trim()) {
      errors.email = 'Email is required';
    } else if (!validateEmail(formData.email)) {
      errors.email = 'Please enter a valid email';
    }

    return errors;
  };

  const validateStep2 = () => {
    const errors = {};

    if (!formData.password) {
      errors.password = 'Password is required';
    } else if (!validatePassword(formData.password)) {
      errors.password = 'Password must be at least 8 characters with uppercase, lowercase, number, and special character';
    }

    if (!formData.confirmPassword) {
      errors.confirmPassword = 'Please confirm your password';
    } else if (formData.password !== formData.confirmPassword) {
      errors.confirmPassword = 'Passwords do not match';
    }

    if (!formData.agreeToTerms) {
      errors.agreeToTerms = 'You must agree to the terms and conditions';
    }

    return errors;
  };

  const handleNextStep = () => {
    clearError();
    const errors = validateStep1();
    if (Object.keys(errors).length > 0) {
      setFormErrors(errors);
      return;
    }
    setStep(2);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    clearError();

    const errors = validateStep2();
    if (Object.keys(errors).length > 0) {
      setFormErrors(errors);
      return;
    }

    try {
      await register(formData.email, formData.password, formData.role, formData.name);
      navigate('/verify-email');
    } catch (err) {
      setFormErrors({ submit: err.message || 'Registration failed' });
    }
  };

  const passwordStrength = getPasswordStrength(formData.password);
  const strengthColors = {
    weak: 'bg-red-500',
    fair: 'bg-orange-500',
    good: 'bg-yellow-500',
    strong: 'bg-green-500',
    'very-strong': 'bg-emerald-500',
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 flex items-center justify-center px-4 py-12">
      {/* Background decorative elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-0 right-0 w-96 h-96 bg-indigo-500 rounded-full mix-blend-multiply filter blur-3xl opacity-10 animate-pulse"></div>
        <div className="absolute bottom-0 left-0 w-96 h-96 bg-blue-500 rounded-full mix-blend-multiply filter blur-3xl opacity-10 animate-pulse"></div>
      </div>

      <div className="relative w-full max-w-md">
        {/* Logo/Header */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-2xl mb-4 shadow-lg">
            <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
            </svg>
          </div>
          <h1 className="text-3xl font-bold text-white mb-2">Vently</h1>
          <p className="text-slate-400">Join our community</p>
        </div>

        {/* Register Card */}
        <div className="bg-slate-800 rounded-2xl shadow-2xl p-8 border border-slate-700">
          {/* Progress Indicator */}
          <div className="mb-8">
            <div className="flex items-center justify-between mb-4">
              <div className={`flex items-center justify-center w-10 h-10 rounded-full font-semibold transition ${
                step >= 1 ? 'bg-blue-500 text-white' : 'bg-slate-700 text-slate-400'
              }`}>
                1
              </div>
              <div className={`flex-1 h-1 mx-2 rounded transition ${
                step >= 2 ? 'bg-blue-500' : 'bg-slate-700'
              }`}></div>
              <div className={`flex items-center justify-center w-10 h-10 rounded-full font-semibold transition ${
                step >= 2 ? 'bg-blue-500 text-white' : 'bg-slate-700 text-slate-400'
              }`}>
                2
              </div>
            </div>
            <p className="text-sm text-slate-400 text-center">
              {step === 1 ? 'Account Information' : 'Security & Preferences'}
            </p>
          </div>

          <h2 className="text-2xl font-bold text-white mb-6">
            {step === 1 ? 'Create your account' : 'Set your password'}
          </h2>

          {/* Error Alert */}
          {(error || formErrors.submit) && (
            <div className="mb-6 p-4 bg-red-500/10 border border-red-500/30 rounded-lg">
              <p className="text-red-400 text-sm font-medium">{error || formErrors.submit}</p>
            </div>
          )}

          <form onSubmit={step === 1 ? (e) => { e.preventDefault(); handleNextStep(); } : handleSubmit} className="space-y-5">
            {step === 1 ? (
              <>
                {/* Role Selection */}
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-3">
                    I want to join as a
                  </label>
                  <div className="grid grid-cols-2 gap-3">
                    {[
                      { value: USER_ROLES.VOLUNTEER, label: 'Volunteer', icon: '👤' },
                      { value: USER_ROLES.ORGANIZER, label: 'Organizer', icon: '🏢' },
                    ].map((option) => (
                      <button
                        key={option.value}
                        type="button"
                        onClick={() => setFormData((prev) => ({ ...prev, role: option.value }))}
                        className={`p-4 rounded-lg border-2 transition text-center ${
                          formData.role === option.value
                            ? 'border-blue-500 bg-blue-500/10'
                            : 'border-slate-600 bg-slate-700 hover:border-slate-500'
                        }`}
                      >
                        <div className="text-2xl mb-2">{option.icon}</div>
                        <p className="text-white font-medium">{option.label}</p>
                      </button>
                    ))}
                  </div>
                </div>

                {/* Name Field */}
                <div>
                  <label htmlFor="name" className="block text-sm font-medium text-slate-300 mb-2">
                    Full Name
                  </label>
                  <input
                    id="name"
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    placeholder="John Doe"
                    className={`w-full px-4 py-3 bg-slate-700 border rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition ${
                      formErrors.name ? 'border-red-500' : 'border-slate-600'
                    }`}
                  />
                  {formErrors.name && (
                    <p className="mt-1 text-sm text-red-400">{formErrors.name}</p>
                  )}
                </div>

                {/* Email Field */}
                <div>
                  <label htmlFor="email" className="block text-sm font-medium text-slate-300 mb-2">
                    Email Address
                  </label>
                  <input
                    id="email"
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    placeholder="you@example.com"
                    className={`w-full px-4 py-3 bg-slate-700 border rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition ${
                      formErrors.email ? 'border-red-500' : 'border-slate-600'
                    }`}
                  />
                  {formErrors.email && (
                    <p className="mt-1 text-sm text-red-400">{formErrors.email}</p>
                  )}
                </div>

                {/* Next Button */}
                <button
                  type="submit"
                  className="w-full py-3 px-4 bg-gradient-to-r from-blue-500 to-indigo-600 text-white font-semibold rounded-lg hover:from-blue-600 hover:to-indigo-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:ring-offset-slate-800 transition"
                >
                  Continue
                </button>
              </>
            ) : (
              <>
                {/* Password Field */}
                <div>
                  <label htmlFor="password" className="block text-sm font-medium text-slate-300 mb-2">
                    Password
                  </label>
                  <input
                    id="password"
                    type="password"
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    placeholder="••••••••"
                    className={`w-full px-4 py-3 bg-slate-700 border rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition ${
                      formErrors.password ? 'border-red-500' : 'border-slate-600'
                    }`}
                  />
                  {formData.password && (
                    <div className="mt-2">
                      <div className="flex items-center gap-2 mb-1">
                        <div className="flex-1 h-1 bg-slate-700 rounded overflow-hidden">
                          <div className={`h-full w-1/5 ${strengthColors[passwordStrength]} transition`}></div>
                        </div>
                        <span className="text-xs text-slate-400 capitalize">{passwordStrength}</span>
                      </div>
                      <p className="text-xs text-slate-400">
                        Must contain uppercase, lowercase, number, and special character
                      </p>
                    </div>
                  )}
                  {formErrors.password && (
                    <p className="mt-1 text-sm text-red-400">{formErrors.password}</p>
                  )}
                </div>

                {/* Confirm Password Field */}
                <div>
                  <label htmlFor="confirmPassword" className="block text-sm font-medium text-slate-300 mb-2">
                    Confirm Password
                  </label>
                  <input
                    id="confirmPassword"
                    type="password"
                    name="confirmPassword"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    placeholder="••••••••"
                    className={`w-full px-4 py-3 bg-slate-700 border rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition ${
                      formErrors.confirmPassword ? 'border-red-500' : 'border-slate-600'
                    }`}
                  />
                  {formErrors.confirmPassword && (
                    <p className="mt-1 text-sm text-red-400">{formErrors.confirmPassword}</p>
                  )}
                </div>

                {/* Terms Checkbox */}
                <div className="flex items-start">
                  <input
                    id="agreeToTerms"
                    type="checkbox"
                    name="agreeToTerms"
                    checked={formData.agreeToTerms}
                    onChange={handleChange}
                    className="w-4 h-4 mt-1 bg-slate-700 border-slate-600 rounded focus:ring-2 focus:ring-blue-500 cursor-pointer"
                  />
                  <label htmlFor="agreeToTerms" className="ml-2 text-sm text-slate-400 cursor-pointer">
                    I agree to the{' '}
                    <a href="#" className="text-blue-400 hover:text-blue-300 transition">
                      Terms of Service
                    </a>{' '}
                    and{' '}
                    <a href="#" className="text-blue-400 hover:text-blue-300 transition">
                      Privacy Policy
                    </a>
                  </label>
                </div>
                {formErrors.agreeToTerms && (
                  <p className="text-sm text-red-400">{formErrors.agreeToTerms}</p>
                )}

                {/* Button Group */}
                <div className="flex gap-3 pt-2">
                  <button
                    type="button"
                    onClick={() => setStep(1)}
                    className="flex-1 py-3 px-4 bg-slate-700 hover:bg-slate-600 text-white font-semibold rounded-lg border border-slate-600 transition"
                  >
                    Back
                  </button>
                  <button
                    type="submit"
                    disabled={loading}
                    className="flex-1 py-3 px-4 bg-gradient-to-r from-blue-500 to-indigo-600 text-white font-semibold rounded-lg hover:from-blue-600 hover:to-indigo-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:ring-offset-slate-800 transition disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {loading ? (
                      <span className="flex items-center justify-center">
                        <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        Creating...
                      </span>
                    ) : (
                      'Create Account'
                    )}
                  </button>
                </div>
              </>
            )}
          </form>

          {/* Sign In Link */}
          <p className="mt-6 text-center text-slate-400">
            Already have an account?{' '}
            <Link to="/login" className="text-blue-400 hover:text-blue-300 font-medium transition">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Register;
