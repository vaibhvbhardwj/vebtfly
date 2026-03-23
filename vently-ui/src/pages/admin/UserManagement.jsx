import React, { useState, useEffect } from 'react';
import { adminApi } from '../../api/adminApi';
import { Pagination } from '../../components/shared/Pagination';
import { SuspendUserModal } from '../../components/admin/SuspendUserModal';
import { BanUserModal } from '../../components/admin/BanUserModal';
import { ResetPasswordModal } from '../../components/admin/ResetPasswordModal';
import { AdjustNoShowsModal } from '../../components/admin/AdjustNoShowsModal';

const UserManagement = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [searchTerm, setSearchTerm] = useState('');
  const [roleFilter, setRoleFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [verificationFilter, setVerificationFilter] = useState('');
  const [selectedUser, setSelectedUser] = useState(null);
  const [activeModal, setActiveModal] = useState(null);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const PAGE_SIZE = 10;

  useEffect(() => { fetchUsers(); }, [currentPage, roleFilter, statusFilter, verificationFilter]);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const filters = {};
      if (roleFilter) filters.role = roleFilter;
      if (statusFilter) filters.status = statusFilter;
      if (verificationFilter) filters.verified = verificationFilter === 'verified';
      if (searchTerm) filters.search = searchTerm;
      const response = await adminApi.getAllUsers(currentPage, PAGE_SIZE, filters);
      setUsers(response.content || []);
      setTotalPages(response.totalPages || 1);
    } catch {
      setErrorMessage('Failed to fetch users');
    } finally {
      setLoading(false);
    }
  };

  const handleVerify = async (user) => {
    try {
      await adminApi.verifyUser(user.id);
      setSuccessMessage(`${user.fullName} has been verified`);
      fetchUsers();
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch {
      setErrorMessage('Failed to verify user');
    }
  };

  const handleModalClose = () => { setActiveModal(null); setSelectedUser(null); };
  const handleModalSuccess = () => {
    setSuccessMessage('Action completed successfully');
    handleModalClose();
    fetchUsers();
    setTimeout(() => setSuccessMessage(''), 3000);
  };

  const statusBadge = (user) => {
    if (user.banned) return <span className="px-2 py-1 rounded-full text-xs font-medium" style={{ background: '#FEE2E2', color: '#EF4444' }}>Banned</span>;
    if (user.suspended) return <span className="px-2 py-1 rounded-full text-xs font-medium" style={{ background: '#FEF3C7', color: '#D97706' }}>Suspended</span>;
    return <span className="px-2 py-1 rounded-full text-xs font-medium" style={{ background: '#D1FAE5', color: '#059669' }}>Active</span>;
  };

  const roleBadge = (role) => {
    const styles = {
      VOLUNTEER: { background: '#EDE9FE', color: '#7C3AED' },
      ORGANIZER: { background: '#E0E7FF', color: '#4338CA' },
      ADMIN: { background: '#FEE2E2', color: '#DC2626' },
    };
    return (
      <span className="px-2 py-1 rounded-full text-xs font-medium" style={styles[role] || { background: '#F3F4F6', color: '#6B7280' }}>
        {role}
      </span>
    );
  };

  return (
    <div className="min-h-screen p-6 pb-24" style={{ background: '#ebf2fa' }}>
      <div className="max-w-7xl mx-auto">

        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold mb-1" style={{ color: '#111827' }}>User Management</h1>
          <p style={{ color: '#6B7280' }}>Manage user accounts, suspensions, and verifications</p>
        </div>

        {successMessage && (
          <div className="mb-4 p-4 rounded-xl border text-sm" style={{ background: '#D1FAE5', borderColor: '#059669', color: '#059669' }}>
            {successMessage}
          </div>
        )}
        {errorMessage && (
          <div className="mb-4 p-4 rounded-xl border text-sm" style={{ background: '#FEE2E2', borderColor: '#EF4444', color: '#EF4444' }}>
            {errorMessage}
          </div>
        )}

        {/* Filters */}
        <div className="rounded-2xl p-6 shadow-sm mb-6" style={{ background: '#fff', border: '1.5px solid #e5e7eb' }}>
          <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
            <div className="md:col-span-2">
              <label className="block text-sm font-medium mb-2" style={{ color: '#6B7280' }}>Search by Name/Email</label>
              <div className="flex gap-2">
                <input
                  type="text"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && fetchUsers()}
                  placeholder="Search users..."
                  className="flex-1 px-4 py-2 rounded-xl border text-sm outline-none"
                  style={{ borderColor: '#e5e7eb', color: '#111827' }}
                />
                <button onClick={() => { setCurrentPage(1); fetchUsers(); }}
                  className="px-4 py-2 rounded-xl text-sm font-medium text-white" style={{ background: '#807aeb' }}>
                  Search
                </button>
              </div>
            </div>

            {[
              { label: 'Role', value: roleFilter, setter: setRoleFilter, options: [['', 'All Roles'], ['VOLUNTEER', 'Volunteer'], ['ORGANIZER', 'Organizer'], ['ADMIN', 'Admin']] },
              { label: 'Status', value: statusFilter, setter: setStatusFilter, options: [['', 'All Status'], ['active', 'Active'], ['suspended', 'Suspended'], ['banned', 'Banned']] },
              { label: 'Verification', value: verificationFilter, setter: setVerificationFilter, options: [['', 'All Users'], ['verified', 'Verified'], ['unverified', 'Unverified']] },
            ].map(({ label, value, setter, options }) => (
              <div key={label}>
                <label className="block text-sm font-medium mb-2" style={{ color: '#6B7280' }}>{label}</label>
                <select value={value} onChange={(e) => { setter(e.target.value); setCurrentPage(1); }}
                  className="w-full px-4 py-2 rounded-xl border text-sm outline-none"
                  style={{ borderColor: '#e5e7eb', color: '#111827', background: '#fff' }}>
                  {options.map(([v, l]) => <option key={v} value={v}>{l}</option>)}
                </select>
              </div>
            ))}
          </div>
        </div>

        {/* Table */}
        <div className="rounded-2xl shadow-sm overflow-hidden" style={{ background: '#fff', border: '1.5px solid #e5e7eb' }}>
          {loading ? (
            <div className="p-8 text-center" style={{ color: '#6B7280' }}>Loading users...</div>
          ) : users.length === 0 ? (
            <div className="p-8 text-center" style={{ color: '#6B7280' }}>No users found</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead style={{ background: '#F9FAFB', borderBottom: '1px solid #e5e7eb' }}>
                  <tr>
                    {['Name', 'Email', 'Role', 'Status', 'Verified', 'No-Shows', 'Actions'].map((h) => (
                      <th key={h} className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wide" style={{ color: '#6B7280' }}>{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {users.map((user) => (
                    <tr key={user.id} className="transition" style={{ borderBottom: '1px solid #f3f4f6' }}
                      onMouseEnter={(e) => e.currentTarget.style.background = '#F9FAFB'}
                      onMouseLeave={(e) => e.currentTarget.style.background = 'transparent'}>
                      <td className="px-6 py-4 text-sm font-medium" style={{ color: '#111827' }}>{user.fullName}</td>
                      <td className="px-6 py-4 text-sm" style={{ color: '#6B7280' }}>{user.email}</td>
                      <td className="px-6 py-4 text-sm">{roleBadge(user.role)}</td>
                      <td className="px-6 py-4 text-sm">{statusBadge(user)}</td>
                      <td className="px-6 py-4 text-sm">
                        {user.verified
                          ? <span style={{ color: '#059669' }}>✓ Verified</span>
                          : <span style={{ color: '#9CA3AF' }}>Unverified</span>}
                      </td>
                      <td className="px-6 py-4 text-sm" style={{ color: '#111827' }}>{user.noShowCount || 0}</td>
                      <td className="px-6 py-4 text-sm">
                        <div className="flex flex-wrap gap-1">
                          {!user.verified && (
                            <button onClick={() => handleVerify(user)}
                              className="px-2 py-1 rounded-lg text-xs font-medium text-white" style={{ background: '#059669' }}>
                              Verify
                            </button>
                          )}
                          {!user.suspended && !user.banned && (
                            <button onClick={() => { setSelectedUser(user); setActiveModal('suspend'); }}
                              className="px-2 py-1 rounded-lg text-xs font-medium text-white" style={{ background: '#D97706' }}>
                              Suspend
                            </button>
                          )}
                          {!user.banned && (
                            <button onClick={() => { setSelectedUser(user); setActiveModal('ban'); }}
                              className="px-2 py-1 rounded-lg text-xs font-medium text-white" style={{ background: '#EF4444' }}>
                              Ban
                            </button>
                          )}
                          <button onClick={() => { setSelectedUser(user); setActiveModal('resetPassword'); }}
                            className="px-2 py-1 rounded-lg text-xs font-medium text-white" style={{ background: '#807aeb' }}>
                            Reset Pwd
                          </button>
                          {user.role === 'VOLUNTEER' && (
                            <button onClick={() => { setSelectedUser(user); setActiveModal('adjustNoShows'); }}
                              className="px-2 py-1 rounded-lg text-xs font-medium text-white" style={{ background: '#6366f1' }}>
                              No-Shows
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        <Pagination currentPage={currentPage} totalPages={totalPages} onPageChange={setCurrentPage} loading={loading} />
      </div>

      {activeModal === 'suspend' && selectedUser && <SuspendUserModal user={selectedUser} onClose={handleModalClose} onSuccess={handleModalSuccess} />}
      {activeModal === 'ban' && selectedUser && <BanUserModal user={selectedUser} onClose={handleModalClose} onSuccess={handleModalSuccess} />}
      {activeModal === 'resetPassword' && selectedUser && <ResetPasswordModal user={selectedUser} onClose={handleModalClose} onSuccess={handleModalSuccess} />}
      {activeModal === 'adjustNoShows' && selectedUser && <AdjustNoShowsModal user={selectedUser} onClose={handleModalClose} onSuccess={handleModalSuccess} />}
    </div>
  );
};

export default UserManagement;
