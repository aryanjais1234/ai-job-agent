import { useState, useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  BrainCircuit,
  User,
  Mail,
  Lock,
  Eye,
  EyeOff,
  Loader2,
  AlertCircle,
  Zap,
  Shield,
  Sparkles,
  Check,
} from 'lucide-react';

const features = [
  { icon: Zap, text: 'AI-Powered Resume Tailoring' },
  { icon: Shield, text: 'Smart Job Matching' },
  { icon: Sparkles, text: 'ATS-Optimized Documents' },
];

const passwordRules = [
  { key: 'length', label: 'At least 8 characters', test: (pw) => pw.length >= 8 },
  { key: 'upper', label: '1 uppercase letter', test: (pw) => /[A-Z]/.test(pw) },
  { key: 'number', label: '1 number', test: (pw) => /\d/.test(pw) },
];

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [fieldErrors, setFieldErrors] = useState({});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [agreedToTerms, setAgreedToTerms] = useState(false);

  const passwordChecks = useMemo(
    () => passwordRules.map((rule) => ({ ...rule, met: rule.test(form.password) })),
    [form.password],
  );

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setFieldErrors((prev) => ({ ...prev, [e.target.name]: '' }));
  };

  const validate = () => {
    const errors = {};
    if (!form.fullName.trim()) errors.fullName = 'Full name is required';
    if (!form.email.trim()) errors.email = 'Email is required';
    else if (!/\S+@\S+\.\S+/.test(form.email)) errors.email = 'Enter a valid email address';
    if (!form.password) errors.password = 'Password is required';
    else if (!passwordChecks.every((r) => r.met))
      errors.password = 'Password does not meet all requirements';
    if (form.password !== form.confirmPassword)
      errors.confirmPassword = 'Passwords do not match';
    if (!agreedToTerms) errors.terms = 'You must agree to the terms';
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
      await register(form.fullName, form.email, form.password);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Please try again.');
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
            Start Your AI-Powered Job Search
          </h2>
          <p className="text-brand-200 text-lg mb-10 leading-relaxed">
            Join thousands of job seekers using AI to land their dream role faster and smarter.
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
            <h1 className="text-2xl font-bold text-gray-900">Create your account</h1>
            <p className="mt-1.5 text-sm text-gray-500">Get started in 2 minutes</p>
          </div>

          {error && (
            <div className="mb-6 flex items-start gap-3 rounded-lg border border-red-200 bg-red-50 p-3.5 text-sm text-red-700 animate-slide-down">
              <AlertCircle className="h-5 w-5 flex-shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Full Name */}
            <div>
              <label htmlFor="fullName" className="block text-sm font-medium text-gray-700 mb-1.5">
                Full Name
              </label>
              <div className="relative">
                <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5">
                  <User className="h-4 w-4 text-gray-400" />
                </div>
                <input
                  id="fullName"
                  name="fullName"
                  type="text"
                  autoComplete="name"
                  value={form.fullName}
                  onChange={handleChange}
                  className={`input-field pl-10 ${fieldErrors.fullName ? 'border-red-300 focus:border-red-500 focus:ring-red-500/20' : ''}`}
                  placeholder="John Doe"
                />
              </div>
              {fieldErrors.fullName && (
                <p className="mt-1.5 text-xs text-red-600">{fieldErrors.fullName}</p>
              )}
            </div>

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
                  autoComplete="new-password"
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
              {/* Password requirements */}
              {form.password.length > 0 && (
                <ul className="mt-2 space-y-1">
                  {passwordChecks.map((rule) => (
                    <li
                      key={rule.key}
                      className={`flex items-center gap-1.5 text-xs transition-colors ${
                        rule.met ? 'text-green-600' : 'text-gray-400'
                      }`}
                    >
                      <Check className={`h-3.5 w-3.5 ${rule.met ? 'opacity-100' : 'opacity-40'}`} />
                      {rule.label}
                    </li>
                  ))}
                </ul>
              )}
            </div>

            {/* Confirm Password */}
            <div>
              <label
                htmlFor="confirmPassword"
                className="block text-sm font-medium text-gray-700 mb-1.5"
              >
                Confirm Password
              </label>
              <div className="relative">
                <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5">
                  <Lock className="h-4 w-4 text-gray-400" />
                </div>
                <input
                  id="confirmPassword"
                  name="confirmPassword"
                  type={showConfirm ? 'text' : 'password'}
                  autoComplete="new-password"
                  value={form.confirmPassword}
                  onChange={handleChange}
                  className={`input-field pl-10 pr-10 ${fieldErrors.confirmPassword ? 'border-red-300 focus:border-red-500 focus:ring-red-500/20' : ''}`}
                  placeholder="••••••••"
                />
                <button
                  type="button"
                  onClick={() => setShowConfirm((v) => !v)}
                  className="absolute inset-y-0 right-0 flex items-center pr-3.5 text-gray-400 hover:text-gray-600 transition-colors"
                  tabIndex={-1}
                >
                  {showConfirm ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
              {fieldErrors.confirmPassword && (
                <p className="mt-1.5 text-xs text-red-600">{fieldErrors.confirmPassword}</p>
              )}
            </div>

            {/* Terms checkbox */}
            <div>
              <label className="flex items-start gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={agreedToTerms}
                  onChange={(e) => {
                    setAgreedToTerms(e.target.checked);
                    setFieldErrors((prev) => ({ ...prev, terms: '' }));
                  }}
                  className="mt-0.5 h-4 w-4 rounded border-gray-300 text-brand-600 focus:ring-brand-500"
                />
                <span className="text-sm text-gray-600">
                  I agree to the{' '}
                  <Link to="/terms" className="font-medium text-brand-600 hover:text-brand-500">
                    Terms of Service
                  </Link>{' '}
                  and{' '}
                  <Link to="/privacy" className="font-medium text-brand-600 hover:text-brand-500">
                    Privacy Policy
                  </Link>
                </span>
              </label>
              {fieldErrors.terms && (
                <p className="mt-1.5 text-xs text-red-600">{fieldErrors.terms}</p>
              )}
            </div>

            {/* Submit */}
            <button type="submit" disabled={loading} className="btn-primary w-full">
              {loading ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  Creating account…
                </>
              ) : (
                'Create Account'
              )}
            </button>
          </form>

          <p className="mt-8 text-center text-sm text-gray-500">
            Already have an account?{' '}
            <Link
              to="/login"
              className="font-semibold text-brand-600 hover:text-brand-500 transition-colors"
            >
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
