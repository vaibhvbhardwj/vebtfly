import React, { useState, useEffect } from 'react';
import { Pagination } from '../../components/shared/Pagination';
import { formatCurrency, formatDate } from '../../utils/formatters';
import { API_BASE_URL } from '../../utils/constants';

const PaymentHistory = () => {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState('all'); // all, deposits, payouts, refunds
  const [pagination, setPagination] = useState({
    page: 1,
    pageSize: 10,
    total: 0,
  });

  useEffect(() => {
    fetchTransactions();
  }, [pagination.page, filter]);

  const fetchTransactions = async () => {
    try {
      setLoading(true);
      setError(null);

      const params = new URLSearchParams({
        page: pagination.page - 1,
        size: pagination.pageSize,
      });

      if (filter !== 'all') {
        params.append('type', filter.toUpperCase());
      }

      const response = await fetch(`${API_BASE_URL}/payments/history?${params}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
      });

      if (!response.ok) throw new Error('Failed to fetch payment history');

      const data = await response.json();
      setTransactions(data.content || []);
      setPagination((prev) => ({
        ...prev,
        total: data.totalElements || 0,
      }));
    } catch (err) {
      console.error('Error fetching transactions:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handlePageChange = (newPage) => {
    setPagination((prev) => ({
      ...prev,
      page: newPage,
    }));
  };

  const getTransactionTypeColor = (type) => {
    switch (type) {
      case 'DEPOSIT':
        return 'bg-blue-500/20 text-blue-400';
      case 'PAYOUT':
        return 'bg-green-500/20 text-green-400';
      case 'REFUND':
        return 'bg-orange-500/20 text-orange-400';
      default:
        return 'bg-slate-500/20 text-slate-400';
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'COMPLETED':
        return 'bg-green-500/20 text-green-400';
      case 'PENDING':
        return 'bg-yellow-500/20 text-yellow-400';
      case 'FAILED':
        return 'bg-red-500/20 text-red-400';
      default:
        return 'bg-slate-500/20 text-slate-400';
    }
  };

  const getAmountColor = (type) => {
    if (type === 'PAYOUT') return 'text-green-400';
    if (type === 'REFUND') return 'text-orange-400';
    return 'text-white';
  };

  if (loading && transactions.length === 0) {
    return (
      <div className="min-h-screen bg-slate-900 py-8">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="animate-pulse space-y-4">
            <div className="h-8 bg-slate-800 rounded w-1/4" />
            <div className="space-y-3">
              {[...Array(5)].map((_, i) => (
                <div key={i} className="h-16 bg-slate-800 rounded" />
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-900 py-8">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-white mb-2">Payment History</h1>
          <p className="text-slate-400">View all your transactions and payment details</p>
        </div>

        {/* Error Message */}
        {error && (
          <div className="mb-6 p-4 bg-red-500/10 border border-red-500/20 rounded-lg">
            <p className="text-red-400 text-sm">{error}</p>
          </div>
        )}

        {/* Filter Buttons */}
        <div className="mb-6 flex flex-wrap gap-2">
          {['all', 'deposits', 'payouts', 'refunds'].map((type) => (
            <button
              key={type}
              onClick={() => {
                setFilter(type);
                setPagination((prev) => ({ ...prev, page: 1 }));
              }}
              className={`px-4 py-2 rounded-lg font-medium transition duration-200 capitalize ${
                filter === type
                  ? 'bg-blue-600 text-white'
                  : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
              }`}
            >
              {type}
            </button>
          ))}
        </div>

        {/* Transactions Table */}
        <div className="bg-slate-800 rounded-lg border border-slate-700 overflow-hidden">
          {transactions.length > 0 ? (
            <>
              {/* Desktop View */}
              <div className="hidden md:block overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b border-slate-700 bg-slate-900">
                      <th className="text-left py-4 px-6 text-slate-400 font-semibold text-sm">
                        Date
                      </th>
                      <th className="text-left py-4 px-6 text-slate-400 font-semibold text-sm">
                        Type
                      </th>
                      <th className="text-left py-4 px-6 text-slate-400 font-semibold text-sm">
                        Event
                      </th>
                      <th className="text-left py-4 px-6 text-slate-400 font-semibold text-sm">
                        Amount
                      </th>
                      <th className="text-left py-4 px-6 text-slate-400 font-semibold text-sm">
                        Status
                      </th>
                      <th className="text-left py-4 px-6 text-slate-400 font-semibold text-sm">
                        Details
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {transactions.map((transaction) => (
                      <tr
                        key={transaction.id}
                        className="border-b border-slate-700 hover:bg-slate-700/50 transition duration-150"
                      >
                        <td className="py-4 px-6 text-white text-sm">
                          {formatDate(transaction.createdAt)}
                        </td>
                        <td className="py-4 px-6">
                          <span
                            className={`px-3 py-1 rounded-full text-xs font-medium ${getTransactionTypeColor(
                              transaction.type
                            )}`}
                          >
                            {transaction.type}
                          </span>
                        </td>
                        <td className="py-4 px-6 text-slate-300 text-sm">
                          {transaction.eventTitle || 'N/A'}
                        </td>
                        <td
                          className={`py-4 px-6 font-semibold text-sm ${getAmountColor(
                            transaction.type
                          )}`}
                        >
                          {transaction.type === 'REFUND' ? '-' : '+'}
                          {formatCurrency(transaction.amount)}
                        </td>
                        <td className="py-4 px-6">
                          <span
                            className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(
                              transaction.status
                            )}`}
                          >
                            {transaction.status}
                          </span>
                        </td>
                        <td className="py-4 px-6">
                          <button className="text-blue-400 hover:text-blue-300 text-sm font-medium transition duration-200">
                            View
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Mobile View */}
              <div className="md:hidden space-y-3 p-4">
                {transactions.map((transaction) => (
                  <div
                    key={transaction.id}
                    className="bg-slate-900 rounded-lg p-4 border border-slate-700"
                  >
                    <div className="flex items-start justify-between mb-3">
                      <div>
                        <p className="text-white font-semibold text-sm">
                          {transaction.eventTitle || 'N/A'}
                        </p>
                        <p className="text-slate-400 text-xs mt-1">
                          {formatDate(transaction.createdAt)}
                        </p>
                      </div>
                      <span
                        className={`px-3 py-1 rounded-full text-xs font-medium ${getTransactionTypeColor(
                          transaction.type
                        )}`}
                      >
                        {transaction.type}
                      </span>
                    </div>

                    <div className="flex items-center justify-between">
                      <div>
                        <p
                          className={`font-semibold text-sm ${getAmountColor(
                            transaction.type
                          )}`}
                        >
                          {transaction.type === 'REFUND' ? '-' : '+'}
                          {formatCurrency(transaction.amount)}
                        </p>
                      </div>
                      <div className="flex items-center gap-2">
                        <span
                          className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(
                            transaction.status
                          )}`}
                        >
                          {transaction.status}
                        </span>
                        <button className="text-blue-400 hover:text-blue-300 text-sm font-medium transition duration-200">
                          View
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </>
          ) : (
            <div className="p-12 text-center">
              <svg
                className="w-16 h-16 mx-auto mb-4 text-slate-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
              <p className="text-slate-400 text-lg">No transactions yet</p>
            </div>
          )}
        </div>

        {/* Pagination */}
        {transactions.length > 0 && (
          <div className="mt-8">
            <Pagination
              currentPage={pagination.page}
              totalPages={Math.ceil(pagination.total / pagination.pageSize)}
              onPageChange={handlePageChange}
            />
          </div>
        )}

        {/* Summary Card */}
        {transactions.length > 0 && (
          <div className="mt-8 bg-slate-800 rounded-lg border border-slate-700 p-6">
            <h2 className="text-lg font-bold text-white mb-4">Summary</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="bg-slate-900 rounded-lg p-4">
                <p className="text-slate-400 text-sm mb-1">Total Deposits</p>
                <p className="text-2xl font-bold text-blue-400">
                  {formatCurrency(
                    transactions
                      .filter((t) => t.type === 'DEPOSIT' && t.status === 'COMPLETED')
                      .reduce((sum, t) => sum + t.amount, 0)
                  )}
                </p>
              </div>
              <div className="bg-slate-900 rounded-lg p-4">
                <p className="text-slate-400 text-sm mb-1">Total Payouts</p>
                <p className="text-2xl font-bold text-green-400">
                  {formatCurrency(
                    transactions
                      .filter((t) => t.type === 'PAYOUT' && t.status === 'COMPLETED')
                      .reduce((sum, t) => sum + t.amount, 0)
                  )}
                </p>
              </div>
              <div className="bg-slate-900 rounded-lg p-4">
                <p className="text-slate-400 text-sm mb-1">Total Refunds</p>
                <p className="text-2xl font-bold text-orange-400">
                  {formatCurrency(
                    transactions
                      .filter((t) => t.type === 'REFUND' && t.status === 'COMPLETED')
                      .reduce((sum, t) => sum + t.amount, 0)
                  )}
                </p>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};


export default PaymentHistory;