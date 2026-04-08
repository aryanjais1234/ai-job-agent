import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  BrainCircuit,
  Mail,
  Lock,
  Eye,
  EyeOff,
  Loader2,
  AlertCircle,
  Zap,
  Shield,
  Sparkles,
} from 'lucide-react';

const features = [
  { icon: Zap, text: 'AI-Powered Resume Tailoring' },
  { icon: Shield, text: 'Smart Job Matching' },
  { icon: Sparkles, text: 'ATS-Optimized Documents' },
];

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '' });
  const [fieldErrors, setFieldErrors] = useState({});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setFieldErrors((prev) => ({ ...prev, [e.target.name]: '' }));
  };

  const validate = () => {
    const errors = {};
    if (!form.email.trim()) errors.email = 'Email is required';
    else if (!/\S+@\S+\.\S+/.test(form.email)) errors.email = 'Enter a valid email address';
    if (!form.password) errors.password = 'Password is required';
    return errors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    const errors = validate();
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    setFieldErrors({});
    setLoading(true);
    try {
      await login(form.email, form.password);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex">
      {/* Left decorative panel */}
      <div className="hidden lg:flex lg:w-1/2 relative bg-gradient-to-br from-brand-900 via-brand-800 to-brand-700 text-white flex-col justify-center px-12 xl:px-20 overflow-hidden">
        {/* Abstract dot decoration */}
        <div className="absolute inset-0 opacity-10">
          <div
            className="absolute inset-0"
            style={{
              backgroundImage: 'radial-gradient(circle, rgba(255,255,255,0.3) 1px, transparent 1px)',
              backgroundSize: '24px 24px',
            }}
          />
        </div>

        <div className="relative z-10 max-w-md">
          <div className="flex items-center gap-3 mb-8">
            <div className="h-14 w-14 rounded-2xl bg-white/10 backdrop-blur-sm flex items-center justify-center">
              <BrainCircuit className="h-8 w-8" />
            </div>
            <span className="text-2xl font-bold tracking-tight">AI Job Agent</span>
          </div>

          <h2 className="text-3xl xl:text-4xl font-bold leading-tight mb-6">
            Your AI-Powered Career Companion
          </h2>
          <p className="text-brand-200 text-lg mb-10 leading-relaxed">
            Let artificial intelligence supercharge your job search with tailored resumes, smart
            matching, and optimized applications.
          </p>

          <div className="space-y-5">
            {features.map(({ icon: Icon, text }) => (
              <div key={text} className="flex items-center gap-4">
                <div className="flex-shrink-0 h-10 w-10 rounded-xl bg-white/10 backdrop-blur-sm flex items-center justify-center">
                  <Icon className="h-5 w-5" />
                </div>
                <span className="text-base font-medium">{text}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Right form panel */}
      <div className="flex-1 flex items-center justify-center px-6 py-12 bg-gray-50">
        <div className="w-full max-w-md animate-fade-in">
          {/* Mobile logo */}
          <div className="flex items-center gap-2 mb-8 lg:hidden">
            <BrainCircuit className="h-7 w-7 text-brand-600" />
            <span className="text-lg font-bold text-brand-600">AI Job Agent</span>
          </div>

          <div className="mb-8">
            <h1 className="text-2xl font-bold text-gray-900">Welcome back</h1>
            <p className="mt-1.5 text-sm text-gray-500">Sign in to your account</p>
          </div>

          {error && (
            <div className="mb-6 flex items-start gap-3 rounded-lg border border-red-200 bg-red-50 p-3.5 text-sm text-red-700 animate-slide-down">
              <AlertCircle className="h-5 w-5 flex-shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Email */}
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1.5">
                Email address
              </label>
              <div className="relative">
                <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5">
                  <Mail className="h-4 w-4 text-gray-400" />
                </div>
                <input
                  id="email"
                  name="email"
                  type="email"
                  autoComplete="email"
                  value={form.email}
                  onChange={handleChange}
                  className={`input-field pl-10 ${fieldErrors.email ? 'border-red-300 focus:border-red-500 focus:ring-red-500/20' : ''}`}
                  placeholder="you@example.com"
                />
              </div>
              {fieldErrors.email && (
                <p className="mt-1.5 text-xs text-red-600">{fieldErrors.email}</p>
              )}
            </div>

            {/* Password */}
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1.5">
                Password
              </label>
              <div className="relative">
                <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5">
                  <Lock className="h-4 w-4 text-gray-400" />
                </div>
                <input
                  id="password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  value={form.password}
                  onChange={handleChange}
                  className={`input-field pl-10 pr-10 ${fieldErrors.password ? 'border-red-300 focus:border-red-500 focus:ring-red-500/20' : ''}`}
                  placeholder="••••••••"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((v) => !v)}
                  className="absolute inset-y-0 right-0 flex items-center pr-3.5 text-gray-400 hover:text-gray-600 transition-colors"
                  tabIndex={-1}
                >
                  {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
              {fieldErrors.password && (
                <p className="mt-1.5 text-xs text-red-600">{fieldErrors.password}</p>
              )}
            </div>

            {/* Remember me & Forgot password */}
            <div className="flex items-center justify-between">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  className="h-4 w-4 rounded border-gray-300 text-brand-600 focus:ring-brand-500"
                />
                <span className="text-sm text-gray-600">Remember me</span>
              </label>
              <Link
                to="/forgot-password"
                className="text-sm font-medium text-brand-600 hover:text-brand-500 transition-colors"
              >
                Forgot password?
              </Link>
            </div>

            {/* Submit */}
            <button type="submit" disabled={loading} className="btn-primary w-full">
              {loading ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  Signing in…
                </>
              ) : (
                'Sign In'
              )}
            </button>
          </form>

          <p className="mt-8 text-center text-sm text-gray-500">
            Don&apos;t have an account?{' '}
            <Link
              to="/register"
              className="font-semibold text-brand-600 hover:text-brand-500 transition-colors"
            >
              Sign up
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
