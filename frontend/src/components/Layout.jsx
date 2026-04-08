import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const navLinks = [
  { to: '/dashboard', label: 'Dashboard', icon: '📊' },
  { to: '/matches', label: 'Matches', icon: '🎯' },
  { to: '/applications', label: 'Applications', icon: '📋' },
  { to: '/notifications', label: 'Notifications', icon: '🔔' },
];

export default function Layout({ children }) {
  const { user, isAuthenticated, logout } = useAuth();
  const [menuOpen, setMenuOpen] = useState(false);
  const [mobileNavOpen, setMobileNavOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path) => location.pathname === path;

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            {/* Logo */}
            <div className="flex items-center">
              <Link to="/dashboard" className="text-xl font-bold text-indigo-600">
                🧠 AI Job Agent
              </Link>
            </div>

            {/* Desktop nav links */}
            {isAuthenticated && (
              <div className="hidden sm:flex sm:items-center sm:space-x-1">
                {navLinks.map((link) => (
                  <Link key={link.to} to={link.to}
                    className={`px-3 py-2 text-sm font-medium rounded-lg transition-colors ${
                      isActive(link.to)
                        ? 'bg-indigo-50 text-indigo-700'
                        : 'text-gray-700 hover:text-indigo-600 hover:bg-gray-50'
                    }`}>
                    <span className="mr-1">{link.icon}</span> {link.label}
                  </Link>
                ))}
              </div>
            )}

            {/* Desktop user menu */}
            {isAuthenticated && (
              <div className="hidden sm:flex sm:items-center">
                <div className="relative">
                  <button
                    onClick={() => setMenuOpen(!menuOpen)}
                    className="flex items-center space-x-2 text-gray-700 hover:text-indigo-600 text-sm font-medium transition-colors"
                  >
                    <div className="h-8 w-8 rounded-full bg-indigo-100 flex items-center justify-center">
                      <span className="text-indigo-600 font-semibold text-sm">
                        {user?.fullName?.charAt(0)?.toUpperCase() || 'U'}
                      </span>
                    </div>
                    <span>{user?.fullName || 'User'}</span>
                    <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                    </svg>
                  </button>

                  {menuOpen && (
                    <div className="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg ring-1 ring-black ring-opacity-5 z-50">
                      <div className="py-1">
                        <Link to="/onboarding" onClick={() => setMenuOpen(false)}
                          className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                          👤 Profile
                        </Link>
                        <Link to="/settings" onClick={() => setMenuOpen(false)}
                          className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                          ⚙️ Settings
                        </Link>
                        <button onClick={handleLogout}
                          className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                          🚪 Logout
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Mobile menu button */}
            {isAuthenticated && (
              <div className="flex items-center sm:hidden">
                <button
                  onClick={() => setMobileNavOpen(!mobileNavOpen)}
                  className="text-gray-700 hover:text-indigo-600 p-2"
                >
                  <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    {mobileNavOpen ? (
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    ) : (
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                    )}
                  </svg>
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Mobile nav */}
        {isAuthenticated && mobileNavOpen && (
          <div className="sm:hidden border-t border-gray-200">
            <div className="px-4 py-3 space-y-1">
              {navLinks.map((link) => (
                <Link key={link.to} to={link.to} onClick={() => setMobileNavOpen(false)}
                  className={`block px-3 py-2 text-base font-medium rounded-md ${
                    isActive(link.to)
                      ? 'bg-indigo-50 text-indigo-700'
                      : 'text-gray-700 hover:text-indigo-600 hover:bg-gray-50'
                  }`}>
                  {link.icon} {link.label}
                </Link>
              ))}
              <Link to="/onboarding" onClick={() => setMobileNavOpen(false)}
                className="block px-3 py-2 text-base font-medium text-gray-700 hover:text-indigo-600 hover:bg-gray-50 rounded-md">
                👤 Profile
              </Link>
              <Link to="/settings" onClick={() => setMobileNavOpen(false)}
                className="block px-3 py-2 text-base font-medium text-gray-700 hover:text-indigo-600 hover:bg-gray-50 rounded-md">
                ⚙️ Settings
              </Link>
              <button onClick={handleLogout}
                className="block w-full text-left px-3 py-2 text-base font-medium text-gray-700 hover:text-indigo-600 hover:bg-gray-50 rounded-md">
                🚪 Logout
              </button>
            </div>
          </div>
        )}
      </nav>

      {/* Main content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {children}
      </main>
    </div>
  );
}
