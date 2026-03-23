import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import api from '../api/axios';
import AuthLayout from '../components/AuthLayout';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const { setUser, setToken, isAuthenticated } = useAuthStore();

  useEffect(() => {
    // Clear stale auth-store from old broken sessions (user was stored as null/undefined)
    const stored = localStorage.getItem('auth-store');
    if (stored) {
      try {
        const parsed = JSON.parse(stored);
        if (parsed?.state?.user && !parsed?.state?.user?.role) {
          localStorage.removeItem('auth-store');
          localStorage.removeItem('user');
        }
      } catch { localStorage.removeItem('auth-store'); }
    }

    if (isAuthenticated) {
      const user = useAuthStore.getState().user;
      if (user?.role === 'VOLUNTEER') navigate('/volunteer/dashboard', { replace: true });
      else if (user?.role === 'ORGANIZER') navigate('/organizer/dashboard', { replace: true });
      else if (user?.role === 'ADMIN') navigate('/admin/dashboard', { replace: true });
    }
  }, []);

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const res = await api.post('/auth/authenticate', { email, password });
      localStorage.setItem('token', res.data.token);
      setToken(res.data.token);
      setUser(res.data);
      setTimeout(() => {
        const role = res.data.role;
        if (role === 'VOLUNTEER') navigate('/volunteer/dashboard', { replace: true });
        else if (role === 'ORGANIZER') navigate('/organizer/dashboard', { replace: true });
        else if (role === 'ADMIN') navigate('/admin/dashboard', { replace: true });
        else navigate('/', { replace: true });
      }, 0);
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Please try again.');
      setLoading(false);
    }
  };

  return (
    <AuthLayout title="Welcome back to" subtitle="Log in to manage your events or find your next event.">
      <h2 className="text-3xl font-bold mb-8 text-[#111827]">Login</h2>
      {error && (
        <div className="mb-4 p-4 bg-red-50 border border-red-200 text-[#EF4444] rounded-xl text-sm">
          {error}
        </div>
      )}
      <form onSubmit={handleLogin} className="space-y-4">
        <input
          type="email"
          placeholder="Email"
          value={email}
          autoComplete="email"
          className="w-full p-4 rounded-xl bg-[#ebf2fa] border border-transparent focus:border-[#807aeb] outline-none transition text-[#111827] placeholder-[#6B7280]"
          onChange={e => setEmail(e.target.value)}
          required
          disabled={loading}
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          autoComplete="current-password"
          className="w-full p-4 rounded-xl bg-[#ebf2fa] border border-transparent focus:border-[#807aeb] outline-none transition text-[#111827] placeholder-[#6B7280]"
          onChange={e => setPassword(e.target.value)}
          required
          disabled={loading}
        />
        <button
          type="submit"
          disabled={loading}
          className="w-full bg-[#807aeb] text-white py-4 rounded-xl font-bold text-base hover:bg-[#6b64d4] hover:shadow-lg hover:shadow-[#807aeb]/30 transition disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {loading ? 'Signing In...' : 'Sign In'}
        </button>
      </form>
      <p className="mt-6 text-center text-[#6B7280] text-sm">
        New here?{' '}
        <Link to="/register" className="text-[#807aeb] font-semibold hover:underline">
          Create account
        </Link>
      </p>
    </AuthLayout>
  );
};

export default Login;
