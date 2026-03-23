import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './index.css'
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import FloatingDashboardButton from './components/FloatingDashboardButton';
import ErrorBoundary from './components/ErrorBoundary';
import ErrorPopup from './components/ErrorPopup';
import ScrollToTop from './components/ScrollToTop';
import { ToastContainer } from './components/Toast';
import { ErrorProvider } from './context/ErrorContext';
import { useAuthStore } from './store/authStore';

import Home from './pages/Home';
import HowItWorks from './pages/HowItWorks';
import Login from './pages/Login';
import Register from './pages/Register';
import ContactUs from './pages/ContactUs';
import TermsAndConditions from './pages/TermsAndConditions';
import CancellationRefunds from './pages/CancellationRefunds';
import PrivacyPolicy from './pages/PrivacyPolicy';
import Notifications from './pages/notifications/Notifications';
import VolunteerProfile from './pages/profile/VolunteerProfile';
import OrganizerProfile from './pages/profile/OrganizerProfile';
import Subscription from './pages/subscription/Subscription';
import { ProtectedRoute } from './components/routes/ProtectedRoute';
import UserManagement from './pages/admin/UserManagement';
import DisputeManagement from './pages/admin/DisputeManagement';
import Analytics from './pages/admin/Analytics';
import AuditLogs from './pages/admin/AuditLogs';
import AdminDashboard from './pages/admin/AdminDashboard';
import VolunteerDashboard from './pages/dashboards/VolunteerDashboard';
import OrganizerDashboard from './pages/dashboards/OrganizerDashboard';
import EventBrowse from './pages/events/EventBrowse';
import EventDetails from './pages/events/EventDetails';
import EventApplications from './pages/events/EventApplications';
import CreateEvent from './pages/events/CreateEvent';
import EditEvent from './pages/events/EditEvent';
import MyEvents from './pages/events/MyEvents';
import MyApplications from './pages/applications/MyApplications';
import PaymentDeposit from './pages/payments/PaymentDeposit';
import PaymentHistory from './pages/payments/PaymentHistory';
import AttendanceMarking from './pages/attendance/AttendanceMarking';
import DisputeSubmission from './pages/disputes/DisputeSubmission';
import DisputeList from './pages/disputes/DisputeList';
import DisputeDetails from './pages/disputes/DisputeDetails';

function AdminRoute({ children }) {
  const { user } = useAuthStore();
  if (!user) return <Navigate to="/login" replace />;
  if (user.role !== 'ADMIN') return <Navigate to="/" replace />;
  return children;
}

function App() {
  return (
    <ErrorProvider>
      <ErrorBoundary>
        <Router>
          <Navbar />
          <FloatingDashboardButton />
          <ErrorPopup />
          <ScrollToTop />
          <ToastContainer />
        <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        
        {/* Policy and Legal Pages */}
        <Route path="/contact" element={<ContactUs />} />
        <Route path="/terms" element={<TermsAndConditions />} />
        <Route path="/refunds" element={<CancellationRefunds />} />
        <Route path="/privacy" element={<PrivacyPolicy />} />
        
        {/* Unauthenticated landing pages */}
        <Route path="/gigs" element={<EventBrowse />} />
        <Route path="/post-event" element={<CreateEvent />} />
        <Route path="/how-it-works" element={<HowItWorks />} />
        
        {/* Event Routes - More specific routes MUST come before less specific ones */}
        <Route path="/events" element={<ProtectedRoute><EventBrowse /></ProtectedRoute>} />
        <Route path="/events/create" element={<ProtectedRoute><CreateEvent /></ProtectedRoute>} />
        <Route path="/events/:id/edit" element={<ProtectedRoute><EditEvent /></ProtectedRoute>} />
        <Route path="/events/:id/manage-applications" element={<ProtectedRoute><EventApplications /></ProtectedRoute>} />
        <Route path="/events/:id" element={<ProtectedRoute><EventDetails /></ProtectedRoute>} />
        <Route path="/my-events" element={<ProtectedRoute><MyEvents /></ProtectedRoute>} />
        
        {/* Application Routes */}
        <Route path="/my-applications" element={<ProtectedRoute><MyApplications /></ProtectedRoute>} />
        
        {/* Payment Routes */}
        <Route path="/payments/deposit" element={<ProtectedRoute><PaymentDeposit /></ProtectedRoute>} />
        <Route path="/payments/history" element={<ProtectedRoute><PaymentHistory /></ProtectedRoute>} />
        
        {/* Attendance Routes */}
        <Route path="/attendance/:eventId" element={<ProtectedRoute><AttendanceMarking /></ProtectedRoute>} />
        
        {/* Dispute Routes */}
        <Route path="/disputes/submit" element={<ProtectedRoute><DisputeSubmission /></ProtectedRoute>} />
        <Route path="/disputes" element={<ProtectedRoute><DisputeList /></ProtectedRoute>} />
        <Route path="/disputes/:id" element={<ProtectedRoute><DisputeDetails /></ProtectedRoute>} />
        
        {/* Dashboard Routes */}
        <Route path="/volunteer/dashboard" element={<ProtectedRoute><VolunteerDashboard /></ProtectedRoute>} />
        <Route path="/organizer/dashboard" element={<ProtectedRoute><OrganizerDashboard /></ProtectedRoute>} />
        
        {/* Protected Routes */}
        <Route path="/notifications" element={<ProtectedRoute><Notifications /></ProtectedRoute>} />
        <Route path="/profile" element={<ProtectedRoute><VolunteerProfile /></ProtectedRoute>} />
        <Route path="/profile/:userId" element={<ProtectedRoute><VolunteerProfile /></ProtectedRoute>} />
        <Route path="/organizer-profile" element={<ProtectedRoute><OrganizerProfile /></ProtectedRoute>} />
        <Route path="/organizer-profile/:userId" element={<ProtectedRoute><OrganizerProfile /></ProtectedRoute>} />
        <Route path="/subscription" element={<ProtectedRoute><Subscription /></ProtectedRoute>} />

        {/* Admin Routes */}
        <Route path="/admin/dashboard" element={<AdminRoute><AdminDashboard /></AdminRoute>} />
        <Route path="/admin/users" element={<AdminRoute><UserManagement /></AdminRoute>} />
        <Route path="/admin/disputes" element={<AdminRoute><DisputeManagement /></AdminRoute>} />
        <Route path="/admin/analytics" element={<AdminRoute><Analytics /></AdminRoute>} />
        <Route path="/admin/audit-logs" element={<AdminRoute><AuditLogs /></AdminRoute>} />
      </Routes>
      <Footer />
      </Router>
    </ErrorBoundary>
    </ErrorProvider>
  );
}

export default App;