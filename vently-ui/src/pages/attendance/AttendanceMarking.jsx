import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../api/axios';

const AttendanceMarking = () => {
  const { eventId } = useParams();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('code');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [event, setEvent] = useState(null);
  const [volunteers, setVolunteers] = useState([]);
  const [attendanceCode, setAttendanceCode] = useState('');
  const [codeError, setCodeError] = useState('');
  const [codeSuccess, setCodeSuccess] = useState('');
  const [uploadFile, setUploadFile] = useState(null);
  const [isUploading, setIsUploading] = useState(false);
  const [codesGenerated, setCodesGenerated] = useState(false);
  const [generating, setGenerating] = useState(false);

  useEffect(() => { fetchData(); }, [eventId]);

  const fetchData = async () => {
    setLoading(true);
    setError('');
    try {
      const eventRes = await api.get(`/events/${eventId}`);
      setEvent(eventRes.data);
      try {
        const volRes = await api.get(`/attendance/${eventId}`);
        const volData = Array.isArray(volRes.data) ? volRes.data : [];
        setVolunteers(volData);
        setCodesGenerated(volData.length > 0);
      } catch {
        setVolunteers([]);
        setCodesGenerated(false);
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateCodes = async () => {
    setGenerating(true);
    setError('');
    try {
      await api.post(`/attendance/${eventId}/generate-codes`);
      await fetchData();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to generate codes');
    } finally {
      setGenerating(false);
    }
  };

  const handleMarkByCode = async (e, status = 'PRESENT') => {
    e.preventDefault();
    setCodeError('');
    setCodeSuccess('');
    if (!attendanceCode.trim()) { setCodeError('Please enter an attendance code'); return; }
    try {
      const endpoint = status === 'LATE'
        ? `/attendance/${eventId}/mark-late-by-code`
        : `/attendance/${eventId}/mark-by-code`;
      await api.post(endpoint, { code: attendanceCode });
      setCodeSuccess(`Marked as ${status}`);
      setAttendanceCode('');
      fetchData();
    } catch (err) {
      setCodeError(err.response?.data?.message || 'Invalid code or already marked');
    }
  };

  const handleUploadFile = async () => {
    if (!uploadFile) return;
    setIsUploading(true);
    try {
      const formData = new FormData();
      formData.append('file', uploadFile);
      await api.post(`/attendance/${eventId}/mark-by-excel`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setUploadFile(null);
      fetchData();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload file');
    } finally {
      setIsUploading(false);
    }
  };

  const handleDownloadTemplate = async () => {
    try {
      const res = await api.get(`/attendance/${eventId}/template`, { responseType: 'blob' });
      const url = window.URL.createObjectURL(res.data);
      const a = document.createElement('a');
      a.href = url;
      a.download = `attendance-template-${eventId}.xlsx`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch {
      setError('Failed to download template');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-[#ebf2fa] flex items-center justify-center">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-[#807aeb]" />
      </div>
    );
  }

  const presentCount = volunteers.filter(v => v.attendanceStatus === 'PRESENT').length;
  const lateCount = volunteers.filter(v => v.attendanceStatus === 'LATE').length;
  const absentCount = volunteers.filter(v => !v.marked).length;
  const tabs = ['code', 'excel', 'list'];
  const tabLabels = { code: 'Mark by Code', excel: 'Upload Excel', list: 'Volunteer List' };

  return (
    <div className="min-h-screen bg-[#ebf2fa] py-8 animate-fade-in">
      <div className="max-w-3xl mx-auto px-4 sm:px-6">

        <button onClick={() => navigate(-1)}
          className="flex items-center gap-2 text-[#807aeb] hover:text-[#6b64d4] transition mb-6 text-sm font-medium">
          ← Back
        </button>

        <div className="mb-6">
          <h1 className="text-3xl font-bold text-[#111827]">Mark Attendance</h1>
          <p className="text-[#6B7280] mt-1">{event?.title}</p>
        </div>

        {error && (
          <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-xl flex items-center justify-between">
            <p className="text-[#EF4444] text-sm">{error}</p>
            <button onClick={() => setError('')} className="text-[#EF4444] ml-3">✕</button>
          </div>
        )}

        {!codesGenerated && (
          <div className="mb-6 p-4 bg-yellow-50 border border-yellow-200 rounded-2xl flex items-center justify-between gap-4">
            <p className="text-yellow-700 text-sm font-medium">
              Attendance codes not generated yet. Volunteers won't receive their codes until you generate them.
            </p>
            <button onClick={handleGenerateCodes} disabled={generating}
              className="px-4 py-2 bg-yellow-500 text-white rounded-xl text-sm font-semibold hover:bg-yellow-400 transition disabled:opacity-50 flex-shrink-0">
              {generating ? 'Generating...' : 'Generate Codes'}
            </button>
          </div>
        )}

        <div className="grid grid-cols-4 gap-3 mb-6">
          {[
            { label: 'Total', value: volunteers.length, color: 'text-[#111827]' },
            { label: 'Present', value: presentCount, color: 'text-[#10B981]' },
            { label: 'Late', value: lateCount, color: 'text-yellow-500' },
            { label: 'Absent', value: absentCount, color: 'text-[#EF4444]' },
          ].map(({ label, value, color }) => (
            <div key={label} className="bg-white rounded-2xl border border-[#807aeb]/10 p-4 shadow-sm text-center">
              <p className="text-[#6B7280] text-xs mb-1">{label}</p>
              <p className={`text-2xl font-bold ${color}`}>{value}</p>
            </div>
          ))}
        </div>

        <div className="flex gap-1 mb-6 bg-white rounded-2xl border border-[#807aeb]/10 p-1 shadow-sm">
          {tabs.map(tab => (
            <button key={tab} onClick={() => setActiveTab(tab)}
              className={`flex-1 py-2 px-3 rounded-xl text-sm font-medium transition ${
                activeTab === tab ? 'bg-[#807aeb] text-white' : 'text-[#6B7280] hover:text-[#111827]'
              }`}>
              {tabLabels[tab]}
            </button>
          ))}
        </div>

        {activeTab === 'code' && (
          <div className="bg-white rounded-2xl border border-[#807aeb]/10 p-6 shadow-sm">
            <form onSubmit={handleMarkByCode} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-[#111827] mb-2">Attendance Code</label>
                <input type="text" value={attendanceCode}
                  onChange={e => setAttendanceCode(e.target.value.toUpperCase())}
                  placeholder="Enter volunteer's code (e.g. AB12CD34)"
                  className="w-full px-4 py-3 bg-[#ebf2fa] border border-transparent rounded-xl text-[#111827] placeholder-[#9CA3AF] focus:outline-none focus:border-[#807aeb] font-mono text-lg tracking-widest transition" />
              </div>
              {codeError && (
                <p className="text-[#EF4444] text-sm p-3 bg-red-50 rounded-xl border border-red-100">{codeError}</p>
              )}
              {codeSuccess && (
                <p className="text-[#10B981] text-sm p-3 bg-green-50 rounded-xl border border-green-100">{codeSuccess}</p>
              )}
              <div className="flex gap-3">
                <button type="submit"
                  className="flex-1 py-3 bg-[#10B981] text-white font-semibold rounded-xl hover:bg-[#059669] transition">
                  Mark Present
                </button>
                <button type="button" onClick={e => handleMarkByCode(e, 'LATE')}
                  className="flex-1 py-3 bg-yellow-500 text-white font-semibold rounded-xl hover:bg-yellow-400 transition">
                  Mark Late
                </button>
              </div>
            </form>
          </div>
        )}

        {activeTab === 'excel' && (
          <div className="bg-white rounded-2xl border border-[#807aeb]/10 p-6 shadow-sm space-y-4">
            <label className="block border-2 border-dashed border-[#807aeb]/30 rounded-xl p-8 text-center cursor-pointer hover:border-[#807aeb] transition">
              <p className="text-[#6B7280] text-sm">
                {uploadFile ? uploadFile.name : 'Click to select .xlsx file'}
              </p>
              <input type="file" accept=".xlsx,.xls"
                onChange={e => setUploadFile(e.target.files?.[0])} className="hidden" />
            </label>
            <button onClick={handleDownloadTemplate}
              className="w-full py-2 bg-[#ebf2fa] text-[#111827] font-medium rounded-xl hover:bg-gray-200 transition text-sm">
              Download Template
            </button>
            {uploadFile && (
              <button onClick={handleUploadFile} disabled={isUploading}
                className="w-full py-3 bg-[#807aeb] text-white font-semibold rounded-xl hover:bg-[#6b64d4] transition disabled:opacity-50">
                {isUploading ? 'Uploading...' : 'Upload File'}
              </button>
            )}
          </div>
        )}

        {activeTab === 'list' && (
          <div className="space-y-3">
            {volunteers.length === 0 ? (
              <div className="bg-white rounded-2xl border border-[#807aeb]/10 p-8 text-center shadow-sm">
                <p className="text-[#6B7280]">No volunteers yet. Generate codes first.</p>
              </div>
            ) : volunteers.map(v => (
              <div key={v.id} className="bg-white rounded-2xl border border-[#807aeb]/10 p-4 shadow-sm flex items-center justify-between">
                <div>
                  <p className="text-[#111827] font-medium">{v.volunteerName}</p>
                  <p className="text-xs text-[#6B7280]">{v.volunteerEmail}</p>
                  <p className="text-xs text-[#9CA3AF] font-mono mt-0.5">{v.code}</p>
                </div>
                <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
                  v.attendanceStatus === 'PRESENT' ? 'bg-[#10B981]/10 text-[#10B981]' :
                  v.attendanceStatus === 'LATE' ? 'bg-yellow-100 text-yellow-600' :
                  'bg-[#ebf2fa] text-[#6B7280]'
                }`}>
                  {v.attendanceStatus === 'PRESENT' ? 'Present' :
                   v.attendanceStatus === 'LATE' ? 'Late' : 'Absent'}
                </span>
              </div>
            ))}
          </div>
        )}

      </div>
    </div>
  );
};

export default AttendanceMarking;
