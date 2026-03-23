import React, { useState, useEffect } from 'react';
import { adminApi } from '../../api/adminApi';
import { Pagination } from '../../components/shared/Pagination';

const ACTION_COLORS = {
  LOGIN:            { background: '#EDE9FE', color: '#7C3AED' },
  LOGOUT:           { background: '#F3F4F6', color: '#6B7280' },
  CREATE:           { background: '#D1FAE5', color: '#059669' },
  UPDATE:           { background: '#FEF3C7', color: '#D97706' },
  DELETE:           { background: '#FEE2E2', color: '#EF4444' },
  SUSPEND:          { background: '#FFEDD5', color: '#EA580C' },
  BAN:              { background: '#FEE2E2', color: '#DC2626' },
  VERIFY:           { background: '#D1FAE5', color: '#059669' },
  RESOLVE_DISPUTE:  { background: '#EDE9FE', color: '#7C3AED' },
  PAYMENT:          { background: '#E0E7FF', color: '#4338CA' },
};

const AuditLogs = () => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [searchTerm, setSearchTerm] = useState('');
  const [actionFilter, setActionFilter] = useState('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [error, setError] = useState('');

  const PAGE_SIZE = 20;

  useEffect(() => { fetchLogs(); }, [currentPage, actionFilter]);

  const fetchLogs = async () => {
    try {
      setLoading(true);
      setError('');
      const filters = {};
      if (actionFilter) filters.action = actionFilter;
      if (searchTerm) filters.search = searchTerm;
      if (dateFrom) filters.dateFrom = dateFrom;
      if (dateTo) filters.dateTo = dateTo;
      const response = await adminApi.getAuditLogs(currentPage, PAGE_SIZE, filters);
      setLogs(response.content || []);
      setTotalPages(response.totalPages || 1);
    } catch (err) {
      setError('Failed to fetch audit logs');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (d) => new Date(d).toLocaleDateString('en-US', {
    year: 'numeric', month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit', second: '2-digit',
  });

  const actionBadge = (action) => {
    const style = ACTION_COLORS[action] || { background: '#F3F4F6', color: '#6B7280' };
    return <span className="px-2 py-1 rounded-lg text-xs font-medium" style={style}>{action}</span>;
  };

  return (
    <div className="min-h-screen p-6 pb-24" style={{ background: '#ebf2fa' }}>
      <div className="max-w-7xl mx-auto">

        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold mb-1" style={{ color: '#111827' }}>Audit Logs</h1>
          <p style={{ color: '#6B7280' }}>Track all system activities and user actions</p>
        </div>

        {error && (
          <div className="mb-4 p-4 rounded-xl border text-sm" style={{ background: '#FEE2E2', borderColor: '#EF4444', color: '#EF4444' }}>
            {error}
          </div>
        )}

        {/* Filters */}
        <div className="rounded-2xl p-6 shadow-sm mb-6" style={{ background: '#fff', border: '1.5px solid #e5e7eb' }}>
          <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
            <div className="md:col-span-2">
              <label className="block text-sm font-medium mb-2" style={{ color: '#6B7280' }}>Search by User/IP</label>
              <div className="flex gap-2">
                <input
                  type="text"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && fetchLogs()}
                  placeholder="Search logs..."
                  className="flex-1 px-4 py-2 rounded-xl border text-sm outline-none"
                  style={{ borderColor: '#e5e7eb', color: '#111827' }}
                />
                <button onClick={() => { setCurrentPage(1); fetchLogs(); }}
                  className="px-4 py-2 rounded-xl text-sm font-medium text-white" style={{ background: '#807aeb' }}>
                  Search
                </button>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium mb-2" style={{ color: '#6B7280' }}>Action Type</label>
              <select value={actionFilter} onChange={(e) => { setActionFilter(e.target.value); setCurrentPage(1); }}
                className="w-full px-4 py-2 rounded-xl border text-sm outline-none"
                style={{ borderColor: '#e5e7eb', color: '#111827', background: '#fff' }}>
                <option value="">All Actions</option>
                {Object.keys(ACTION_COLORS).map((a) => <option key={a} value={a}>{a}</option>)}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium mb-2" style={{ color: '#6B7280' }}>From Date</label>
              <input type="date" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)}
                className="w-full px-4 py-2 rounded-xl border text-sm outline-none"
                style={{ borderColor: '#e5e7eb', color: '#111827' }} />
            </div>

            <div>
              <label className="block text-sm font-medium mb-2" style={{ color: '#6B7280' }}>To Date</label>
              <input type="date" value={dateTo} onChange={(e) => setDateTo(e.target.value)}
                className="w-full px-4 py-2 rounded-xl border text-sm outline-none"
                style={{ borderColor: '#e5e7eb', color: '#111827' }} />
            </div>
          </div>
        </div>

        {/* Table */}
        <div className="rounded-2xl shadow-sm overflow-hidden" style={{ background: '#fff', border: '1.5px solid #e5e7eb' }}>
          {loading ? (
            <div className="p-8 text-center" style={{ color: '#6B7280' }}>Loading logs...</div>
          ) : logs.length === 0 ? (
            <div className="p-8 text-center" style={{ color: '#6B7280' }}>No logs found</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead style={{ background: '#F9FAFB', borderBottom: '1px solid #e5e7eb' }}>
                  <tr>
                    {['Timestamp', 'User', 'Action', 'Entity Type', 'Entity ID', 'IP Address', 'Details'].map((h) => (
                      <th key={h} className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wide" style={{ color: '#6B7280' }}>{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {logs.map((log) => (
                    <tr key={log.id} style={{ borderBottom: '1px solid #f3f4f6' }}
                      onMouseEnter={(e) => e.currentTarget.style.background = '#F9FAFB'}
                      onMouseLeave={(e) => e.currentTarget.style.background = 'transparent'}>
                      <td className="px-6 py-4 text-xs whitespace-nowrap" style={{ color: '#6B7280' }}>{formatDate(log.timestamp)}</td>
                      <td className="px-6 py-4 text-sm" style={{ color: '#111827' }}>{log.userName || 'System'}</td>
                      <td className="px-6 py-4 text-sm">{actionBadge(log.action)}</td>
                      <td className="px-6 py-4 text-sm" style={{ color: '#6B7280' }}>{log.entityType || 'N/A'}</td>
                      <td className="px-6 py-4 text-sm font-mono" style={{ color: '#6B7280' }}>{log.entityId || 'N/A'}</td>
                      <td className="px-6 py-4 text-sm font-mono" style={{ color: '#6B7280' }}>{log.ipAddress || 'N/A'}</td>
                      <td className="px-6 py-4 text-sm">
                        {log.details && (
                          <details className="cursor-pointer">
                            <summary className="text-sm font-medium" style={{ color: '#807aeb' }}>View</summary>
                            <div className="mt-2 p-2 rounded-lg text-xs overflow-auto max-w-xs" style={{ background: '#F9FAFB', border: '1px solid #e5e7eb', color: '#374151' }}>
                              <pre>{JSON.stringify(log.details, null, 2)}</pre>
                            </div>
                          </details>
                        )}
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
    </div>
  );
};

export default AuditLogs;
