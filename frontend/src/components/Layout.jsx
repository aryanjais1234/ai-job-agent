import { useState, useEffect, useRef } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard,
  Target,
  ClipboardList,
  Bell,
  Settings,
  User,
  LogOut,
  Menu,
  X,
  BrainCircuit,
  ChevronDown,
} from 'lucide-react';

const navLinks = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/matches', label: 'Job Matches', icon: Target },
  { to: '/applications', label: 'Applications', icon: ClipboardList },
  { to: '/notifications', label: 'Notifications', icon: Bell },
];

const userMenuItems = [
  { to: '/onboarding', label: 'Profile', icon: User },
  { to: '/settings', label: 'Settings', icon: Settings },
];

export default function Layout({ children }) {
  const { user, isAuthenticated, logout } = useAuth();
  const [menuOpen, setMenuOpen] = useState(false);
  const [mobileNavOpen, setMobileNavOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const dropdownRef = useRef(null);

  // Close dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(event) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setMenuOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Close mobile nav on route change
  useEffect(() => {
    setMobileNavOpen(false);
    setMenuOpen(false);
  }, [location.pathname]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path) => location.pathname === path;

  const unreadCount = 3; // TODO: wire to real notification count

  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      <nav className="sticky top-0 z-40 bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            {/* Logo */}
            <Link
              to="/dashboard"
              className="flex items-center gap-2 text-brand-600 hover:text-brand-700 transition-colors"
            >
              <BrainCircuit className="h-7 w-7" />
              <span className="text-lg font-bold tracking-tight">AI Job Agent</span>
            </Link>

            {/* Desktop nav links */}
            {isAuthenticated && (
              <div className="hidden md:flex items-center gap-1">
                {navLinks.map((link) => {
                  const Icon = link.icon;
                  const active = isActive(link.to);
                  return (
                    <Link
                      key={link.to}
                      to={link.to}
                      className={`relative flex items-center gap-1.5 px-3 py-2 text-sm font-medium rounded-lg transition-colors ${
                        active
                          ? 'text-brand-600 bg-brand-50'
                          : 'text-gray-600 hover:text-brand-600 hover:bg-gray-50'
                      }`}
                    >
                      <Icon className="h-4 w-4" />
                      {link.label}
                      {link.to === '/notifications' && unreadCount > 0 && (
                        <span className="absolute -top-0.5 -right-0.5 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-[10px] font-bold text-white">
                          {unreadCount}
                        </span>
                      )}
                      {active && (
                        <span className="absolute bottom-0 left-3 right-3 h-0.5 rounded-full bg-brand-600" />
                      )}
                    </Link>
                  );
                })}
              </div>
            )}

            <div className="flex items-center gap-3">
              {/* Desktop notification bell */}
              {isAuthenticated && (
                <Link
                  to="/notifications"
                  className="relative hidden md:flex items-center justify-center h-9 w-9 rounded-lg text-gray-500 hover:text-brand-600 hover:bg-gray-100 transition-colors"
                >
                  <Bell className="h-5 w-5" />
                  {unreadCount > 0 && (
                    <span className="absolute top-1 right-1 h-2 w-2 rounded-full bg-red-500 ring-2 ring-white" />
                  )}
                </Link>
              )}

              {/* Desktop user menu */}
              {isAuthenticated && (
                <div className="hidden md:block relative" ref={dropdownRef}>
                  <button
                    onClick={() => setMenuOpen((prev) => !prev)}
                    className="flex items-center gap-2 rounded-lg px-2 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-100 transition-colors"
                  >
                    <div className="h-8 w-8 rounded-full bg-brand-100 flex items-center justify-center">
                      <span className="text-brand-600 font-semibold text-sm">
                        {user?.fullName?.charAt(0)?.toUpperCase() || 'U'}
                      </span>
                    </div>
                    <span className="max-w-[120px] truncate">{user?.fullName || 'User'}</span>
                    <ChevronDown
                      className={`h-4 w-4 text-gray-400 transition-transform duration-200 ${
                        menuOpen ? 'rotate-180' : ''
                      }`}
                    />
                  </button>

                  {menuOpen && (
                    <div className="absolute right-0 mt-2 w-52 rounded-xl bg-white shadow-lg ring-1 ring-black/5 z-50 animate-slide-down">
                      <div className="px-4 py-3 border-b border-gray-100">
                        <p className="text-sm font-semibold text-gray-900 truncate">
                          {user?.fullName || 'User'}
                        </p>
                        <p className="text-xs text-gray-500 truncate">{user?.email || ''}</p>
                      </div>
                      <div className="py-1">
                        {userMenuItems.map((item) => {
                          const Icon = item.icon;
                          return (
                            <Link
                              key={item.to}
                              to={item.to}
                              onClick={() => setMenuOpen(false)}
                              className="flex items-center gap-2.5 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                            >
                              <Icon className="h-4 w-4 text-gray-400" />
                              {item.label}
                            </Link>
                          );
                        })}
                      </div>
                      <div className="border-t border-gray-100 py-1">
                        <button
                          onClick={handleLogout}
                          className="flex w-full items-center gap-2.5 px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors"
                        >
                          <LogOut className="h-4 w-4" />
                          Logout
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              )}

              {/* Mobile menu button */}
              {isAuthenticated && (
                <button
                  onClick={() => setMobileNavOpen((prev) => !prev)}
                  className="md:hidden flex items-center justify-center h-9 w-9 rounded-lg text-gray-600 hover:text-brand-600 hover:bg-gray-100 transition-colors"
                  aria-label="Toggle navigation menu"
                >
                  {mobileNavOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
                </button>
              )}
            </div>
          </div>
        </div>

        {/* Mobile nav panel */}
        {isAuthenticated && mobileNavOpen && (
          <div className="md:hidden border-t border-gray-200 bg-white animate-slide-down">
            <div className="px-4 py-3 space-y-1">
              {navLinks.map((link) => {
                const Icon = link.icon;
                const active = isActive(link.to);
                return (
                  <Link
                    key={link.to}
                    to={link.to}
                    className={`flex items-center gap-3 px-3 py-2.5 text-sm font-medium rounded-lg transition-colors ${
                      active
                        ? 'bg-brand-50 text-brand-600'
                        : 'text-gray-700 hover:text-brand-600 hover:bg-gray-50'
                    }`}
                  >
                    <Icon className="h-5 w-5" />
                    {link.label}
                    {link.to === '/notifications' && unreadCount > 0 && (
                      <span className="ml-auto badge bg-red-100 text-red-700">{unreadCount}</span>
                    )}
                  </Link>
                );
              })}

              <div className="my-2 border-t border-gray-100" />

              {userMenuItems.map((item) => {
                const Icon = item.icon;
                return (
                  <Link
                    key={item.to}
                    to={item.to}
                    className="flex items-center gap-3 px-3 py-2.5 text-sm font-medium text-gray-700 hover:text-brand-600 hover:bg-gray-50 rounded-lg transition-colors"
                  >
                    <Icon className="h-5 w-5" />
                    {item.label}
                  </Link>
                );
              })}

              <button
                onClick={handleLogout}
                className="flex w-full items-center gap-3 px-3 py-2.5 text-sm font-medium text-red-600 hover:bg-red-50 rounded-lg transition-colors"
              >
                <LogOut className="h-5 w-5" />
                Logout
              </button>
            </div>
          </div>
        )}
      </nav>

      {/* Main content */}
      <main className="flex-1 w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {children}
      </main>

      {/* Footer */}
      <footer className="border-t border-gray-200 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 flex flex-col sm:flex-row items-center justify-between gap-2 text-sm text-gray-500">
          <p>&copy; {new Date().getFullYear()} AI Job Agent. All rights reserved.</p>
          <div className="flex items-center gap-1 text-gray-400">
            <BrainCircuit className="h-4 w-4" />
            <span>Powered by AI</span>
          </div>
        </div>
      </footer>
    </div>
  );
}
