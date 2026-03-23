import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useNotifications } from '../hooks/useNotifications';

const Navbar = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, isAuthenticated, logout } = useAuth();
  const { unreadCount } = useNotifications();
  const isActive = (path) => location.pathname === path;

  const getNavLinks = () => {
    const baseLinks = [
      { name: 'Find Events', path: '/events', icon: 'bx bxs-file-find' },
      { name: 'How It Works', path: '/how-it-works', icon: 'bx bx-question-mark' },
    ];

    if (user?.role === 'ORGANIZER') {
      return [
        ...baseLinks.slice(0, 1),
        { name: 'Post Event', path: '/events/create', icon: 'bx bxs-cloud-upload' },
        { name: 'My Events', path: '/my-events', icon: 'bx bxs-calendar-event' },
        { name: 'Alerts', path: '/notifications', icon: 'bx bxs-bell', badge: unreadCount },
        { name: 'Profile', path: '/organizer-profile', icon: 'bx bxs-user' },
      ];
    } else if (user?.role === 'VOLUNTEER') {
      return [
        ...baseLinks.slice(0, 1),
        { name: 'My Events', path: '/my-applications', icon: 'bx bxs-calendar-event' },
        { name: 'Alerts', path: '/notifications', icon: 'bx bxs-bell', badge: unreadCount },
        { name: 'Profile', path: '/profile', icon: 'bx bxs-user' },
      ];
    }

    return [
      ...baseLinks,
      { name: 'Profile', path: '/profile', icon: 'bx bxs-user' },
    ];
  };

  const navLinks = getNavLinks();

  const adminLinks = [
    { name: 'Dashboard', path: '/admin/dashboard', icon: 'bx bxs-dashboard' },
    { name: 'Users', path: '/admin/users', icon: 'bx bxs-group' },
    { name: 'Disputes', path: '/admin/disputes', icon: 'bx bxs-shield-alt-2' },
    { name: 'Analytics', path: '/admin/analytics', icon: 'bx bx-line-chart' },
    { name: 'Logs', path: '/admin/audit-logs', icon: 'bx bxs-notepad' },
  ];

  const isAdminRoute = location.pathname.startsWith('/admin');
  const displayLinks = isAdminRoute ? adminLinks : navLinks;

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <>
      {/* DESKTOP NAVBAR */}
      <nav className="hidden md:flex sticky top-0 z-50 bg-white/80 backdrop-blur-md px-10 py-4 justify-between items-center border-b border-[#807aeb]/10 shadow-sm">
        <Link to="/" className="text-2xl font-black tracking-tighter text-[#111827]">
          Vently<span className="text-[#807aeb]">.</span>
        </Link>

        <div className="flex gap-8 items-center">
          {isAdminRoute && user?.role === 'ADMIN' ? (
            adminLinks.map((link) => (
              <Link
                key={link.path}
                to={link.path}
                className={`text-sm font-medium transition ${isActive(link.path) ? 'text-[#807aeb]' : 'text-[#6B7280] hover:text-[#807aeb]'}`}
              >
                {link.name}
              </Link>
            ))
          ) : (
            navLinks.map((link) => (
              <Link
                key={link.path}
                to={link.path}
                className={`text-sm font-medium transition relative ${isActive(link.path) ? 'text-[#807aeb]' : 'text-[#6B7280] hover:text-[#807aeb]'}`}
              >
                {link.name}
                {isActive(link.path) && (
                  <span className="absolute -bottom-1 left-0 right-0 h-0.5 bg-[#807aeb] rounded-full" />
                )}
              </Link>
            ))
          )}

          {user?.role === 'ADMIN' && !isAdminRoute && (
            <Link to="/admin/dashboard" className="text-[#6B7280] hover:text-[#807aeb] text-sm font-medium">Admin</Link>
          )}

          {isAuthenticated && user ? (
            <>
              <button
                onClick={() => navigate('/notifications')}
                className="relative text-[#6B7280] hover:text-[#807aeb] p-2 rounded-lg hover:bg-[#807aeb]/10"
                title="Notifications"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                </svg>
                {unreadCount > 0 && (
                  <span className="absolute top-0 right-0 inline-flex items-center justify-center w-4 h-4 text-[9px] font-bold text-white bg-[#EF4444] rounded-full">
                    {unreadCount > 99 ? '99+' : unreadCount}
                  </span>
                )}
              </button>

              <div className="flex items-center gap-3">
                <span className="text-sm text-[#6B7280]">{user.fullName || user.email}</span>
                <span className="text-xs bg-[#807aeb]/10 text-[#807aeb] px-3 py-1 rounded-full font-semibold border border-[#807aeb]/20">
                  {user.role === 'ORGANIZER' ? 'Organizer' : 'Volunteer'}
                </span>
              </div>
              <button
                onClick={handleLogout}
                className="text-sm text-[#6B7280] hover:text-[#EF4444] font-medium px-3 py-1.5 rounded-lg hover:bg-red-50"
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="text-sm text-[#6B7280] hover:text-[#807aeb] font-medium">Login</Link>
              <Link to="/register" className="bg-[#807aeb] text-white px-5 py-2 rounded-xl font-semibold text-sm hover:bg-[#6b64d4] hover:shadow-md hover:shadow-[#807aeb]/30">
                Join Now
              </Link>
            </>
          )}
        </div>
      </nav>

      {/* MOBILE TOP BAR */}
      <div className="md:hidden flex bg-white/90 backdrop-blur-md text-[#111827] p-4 sticky top-0 z-50 justify-between items-center border-b border-[#807aeb]/10 shadow-sm">
        <span className="text-xl font-black tracking-tighter">Vently<span className="text-[#807aeb]">.</span></span>
        {isAuthenticated && user && (
          <button
            onClick={handleLogout}
            className="text-sm text-[#EF4444] font-medium"
          >
            Logout
          </button>
        )}
      </div>

      {/* MOBILE BOTTOM TAB BAR */}
      <div className="md:hidden fixed bottom-0 left-0 right-0 z-50 bg-white border-t border-[#807aeb]/10 px-6 py-3 shadow-lg">
        <div className="flex justify-between items-center">
          {displayLinks.map((link) => (
            <Link
              key={link.path}
              to={link.path}
              className="flex flex-col items-center gap-1 relative"
            >
              <span className="relative">
                <i className={`${link.icon} text-2xl`} style={{ color: isActive(link.path) ? '#807aeb' : '#9CA3AF' }} />
                {link.badge > 0 && (
                  <span className="absolute -top-1 -right-1 inline-flex items-center justify-center w-4 h-4 text-[9px] font-bold text-white bg-[#EF4444] rounded-full">
                    {link.badge > 9 ? '9+' : link.badge}
                  </span>
                )}
              </span>
              <span className={`text-[10px] font-medium ${isActive(link.path) ? 'text-[#807aeb]' : 'text-[#6B7280]'}`}>
                {link.name}
              </span>
              {isActive(link.path) && (
                <div className="w-1 h-1 bg-[#807aeb] rounded-full" />
              )}
            </Link>
          ))}
        </div>
      </div>
    </>
  );
};

export default Navbar;
