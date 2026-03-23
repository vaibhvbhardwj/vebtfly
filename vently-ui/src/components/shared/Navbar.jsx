import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { USER_ROLES } from '../../utils/constants';
import { NotificationBell } from '../notifications/NotificationBell';

export const Navbar = () => {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getDashboardLink = () => {
    if (user?.role === USER_ROLES.ADMIN) return '/admin/dashboard';
    if (user?.role === USER_ROLES.ORGANIZER) return '/organizer/dashboard';
    return '/volunteer/dashboard';
  };

  return (
    <>
      <nav className="bg-slate-800 border-b border-slate-700 sticky top-0 z-40" aria-label="Main navigation">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            {/* Logo */}
            <Link to={user ? getDashboardLink() : '/'} className="flex items-center gap-2" aria-label="Vently home">
              <div className="inline-flex items-center justify-center w-10 h-10 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-lg">
                <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
                </svg>
              </div>
              <span className="text-xl font-bold text-white hidden sm:inline">Vently</span>
            </Link>

            {/* Desktop Navigation */}
            <div className="hidden md:flex items-center gap-8">
              <Link to={getDashboardLink()} className="text-slate-300 hover:text-white transition">
                Dashboard
              </Link>
              {user?.role === USER_ROLES.VOLUNTEER && (
                <Link to="/events" className="text-slate-300 hover:text-white transition">
                  Browse Events
                </Link>
              )}
              {user?.role === USER_ROLES.ORGANIZER && (
                <Link to="/my-events" className="text-slate-300 hover:text-white transition">
                  My Events
                </Link>
              )}
            </div>

          {/* Right Section */}
          <div className="flex items-center gap-4">
            {/* Notification Bell */}
            <NotificationBell />

            {/* User Menu */}
            <div className="relative hidden sm:block">
              <button
                onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
                className="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-slate-700 transition focus:outline-none focus:ring-2 focus:ring-blue-500"
                aria-label="User menu"
                aria-expanded={isUserMenuOpen}
                aria-haspopup="true"
              >
                <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-full flex items-center justify-center text-white text-sm font-bold">
                  {user?.name?.charAt(0).toUpperCase()}
                </div>
                <svg className="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 14l-7 7m0 0l-7-7m7 7V3" />
                </svg>
              </button>

              {/* User Dropdown */}
              {isUserMenuOpen && (
                <div className="absolute right-0 mt-2 w-48 bg-slate-800 border border-slate-700 rounded-lg shadow-lg overflow-hidden" role="menu">
                  <div className="p-4 border-b border-slate-700">
                    <p className="text-white font-semibold text-sm">{user?.name}</p>
                    <p className="text-slate-400 text-xs">{user?.email}</p>
                  </div>
                  <Link
                    to="/profile"
                    className="block px-4 py-2 text-slate-300 hover:text-white hover:bg-slate-700 transition text-sm focus:outline-none focus:bg-slate-700"
                    role="menuitem"
                  >
                    Profile
                  </Link>
                  <Link
                    to="/subscription"
                    className="block px-4 py-2 text-slate-300 hover:text-white hover:bg-slate-700 transition text-sm focus:outline-none focus:bg-slate-700"
                    role="menuitem"
                  >
                    Subscription
                  </Link>
                  <button
                    onClick={handleLogout}
                    className="w-full text-left px-4 py-2 text-red-400 hover:text-red-300 hover:bg-slate-700 transition text-sm border-t border-slate-700 focus:outline-none focus:bg-slate-700"
                    role="menuitem"
                  >
                    Logout
                  </button>
                </div>
              )}
            </div>

            {/* Mobile Menu Button */}
            <button
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              className="md:hidden p-2 text-slate-400 hover:text-white transition focus:outline-none focus:ring-2 focus:ring-blue-500"
              aria-label="Toggle mobile menu"
              aria-expanded={isMobileMenuOpen}
            >
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>
          </div>
        </div>

        {/* Mobile Menu */}
        {isMobileMenuOpen && (
          <div className="md:hidden pb-4 space-y-2" role="navigation" aria-label="Mobile navigation">
            <Link
              to={getDashboardLink()}
              className="block px-4 py-2 text-slate-300 hover:text-white hover:bg-slate-700 rounded transition focus:outline-none focus:bg-slate-700"
            >
              Dashboard
            </Link>
            {user?.role === USER_ROLES.VOLUNTEER && (
              <Link
                to="/events"
                className="block px-4 py-2 text-slate-300 hover:text-white hover:bg-slate-700 rounded transition focus:outline-none focus:bg-slate-700"
              >
                Browse Events
              </Link>
            )}
            {user?.role === USER_ROLES.ORGANIZER && (
              <Link
                to="/events/create"
                className="block px-4 py-2 text-slate-300 hover:text-white hover:bg-slate-700 rounded transition focus:outline-none focus:bg-slate-700"
              >
                Create Event
              </Link>
            )}
            <Link
              to="/profile"
              className="block px-4 py-2 text-slate-300 hover:text-white hover:bg-slate-700 rounded transition focus:outline-none focus:bg-slate-700"
            >
              Profile
            </Link>
            <Link
              to="/subscription"
              className="block px-4 py-2 text-slate-300 hover:text-white hover:bg-slate-700 rounded transition focus:outline-none focus:bg-slate-700"
            >
              Subscription
            </Link>
            <button
              onClick={handleLogout}
              className="w-full text-left px-4 py-2 text-red-400 hover:text-red-300 hover:bg-slate-700 rounded transition focus:outline-none focus:bg-slate-700"
            >
              Logout
            </button>
          </div>
        )}
      </div>
    </nav>

    {/* Bottom Navigation Bar */}
    {user && (
      <div className="fixed bottom-0 left-0 right-0 bg-slate-900 border-t border-slate-700 md:hidden z-40">
        <div className="flex items-center justify-between h-20 px-1">
          {/* Find Events / Browse Events */}
          <Link
            to="/events"
            className="flex-1 flex flex-col items-center gap-1 py-2 px-2 text-slate-400 hover:text-blue-400 transition"
            title="Find Events"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <span className="text-xs font-medium">Find Events</span>
          </Link>

          {/* How It Works */}
          <Link
            to="/how-it-works"
            className="flex-1 flex flex-col items-center gap-1 py-2 px-2 text-slate-400 hover:text-yellow-400 transition"
            title="How It Works"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span className="text-xs font-medium">How It Works</span>
          </Link>

          {/* Post Event (Organizer) or placeholder */}
          {user.role === USER_ROLES.ORGANIZER ? (
            <Link
              to="/events/create"
              className="flex-1 flex flex-col items-center gap-1 py-2 px-2 text-slate-400 hover:text-green-400 transition"
              title="Post Event"
            >
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
              <span className="text-xs font-medium">Post Event</span>
            </Link>
          ) : (
            <div className="flex-1 flex flex-col items-center gap-1 py-2 px-2" />
          )}

          {/* My Events (Organizer) / My Applications (Volunteer) */}
          {user.role === USER_ROLES.ORGANIZER ? (
            <Link
              to="/my-events"
              className="flex-1 flex flex-col items-center gap-1 py-2 px-2 text-slate-400 hover:text-indigo-400 transition"
              title="My Events"
            >
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
              <span className="text-xs font-medium">My Events</span>
            </Link>
          ) : user.role === USER_ROLES.VOLUNTEER ? (
            <Link
              to="/my-applications"
              className="flex-1 flex flex-col items-center gap-1 py-2 px-2 text-slate-400 hover:text-indigo-400 transition"
              title="My Applications"
            >
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
              <span className="text-xs font-medium">My Events</span>
            </Link>
          ) : (
            <div className="flex-1 flex flex-col items-center gap-1 py-2 px-2" />
          )}

          {/* Profile */}
          <Link
            to="/profile"
            className="flex-1 flex flex-col items-center gap-1 py-2 px-2 text-slate-400 hover:text-purple-400 transition"
            title="Profile"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
            <span className="text-xs font-medium">Profile</span>
          </Link>
        </div>
      </div>
    )}

    {/* Add padding to body to account for bottom nav on mobile */}
    <div className="md:hidden h-20"></div>
    </>
  );
};
