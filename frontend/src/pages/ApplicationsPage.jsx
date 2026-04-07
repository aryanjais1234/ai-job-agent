import { useState, useEffect, useCallback } from 'react';
import * as applicationsApi from '../api/applications';

const STATUS_OPTIONS = ['ALL', 'PENDING', 'APPLIED', 'INTERVIEW', 'OFFER', 'REJECTED'];
const STATUS_COLORS = {
  PENDING: 'bg-yellow-100 text-yellow-700',
  APPLIED: 'bg-blue-100 text-blue-700',
  INTERVIEW: 'bg-purple-100 text-purple-700',
  OFFER: 'bg-green-100 text-green-700',
  REJECTED: 'bg-red-100 text-red-700',
};

export default function ApplicationsPage() {
  const [applications, setApplications] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [editId, setEditId] = useState(null);
  const [editStatus, setEditStatus] = useState('');

  const loadApplications = useCallback(async () => {
    setLoading(true);
    try {
      const status = filter === 'ALL' ? null : filter;
      const data = await applicationsApi.getApplications(status, page, 10);
      setApplications(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch {
      // ignore
    } finally {
      setLoading(false);
    }
  }, [filter, page]);

  useEffect(() => { loadApplications(); }, [loadApplications]);

  const handleUpdateStatus = async (id) => {
    try {
      await applicationsApi.updateApplication(id, { status: editStatus });
      setEditId(null);
      loadApplications();
    } catch {
      // ignore
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this application?')) return;
    try {
      await applicationsApi.deleteApplication(id);
      loadApplications();
    } catch {
      // ignore
    }
  };

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Application Tracker</h1>
        <p className="text-gray-500 mt-1">Track and manage your job applications.</p>
      </div>

      <div className="flex space-x-2 mb-6 overflow-x-auto">
        {STATUS_OPTIONS.map((status) => (
          <button key={status} onClick={() => { setFilter(status); setPage(0); }}
            className={`px-4 py-2 rounded-lg text-sm font-medium whitespace-nowrap transition-colors ${
              filter === status ? 'bg-indigo-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}>
            {status}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-16">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600" />
        </div>
      ) : applications.length === 0 ? (
        <div className="bg-white rounded-xl shadow-sm border p-12 text-center">
          <p className="text-3xl mb-4">📋</p>
          <h2 className="text-xl font-semibold text-gray-800 mb-2">No applications yet</h2>
          <p className="text-gray-500">Start tracking your applications from the job matches page.</p>
        </div>
      ) : (
        <div className="space-y-4">
          {applications.map((app) => (
            <div key={app.id} className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="text-lg font-semibold text-gray-900">{app.jobTitle}</h3>
                  <p className="text-gray-600">{app.company} • {app.location}</p>
                  {app.appliedAt && (
                    <p className="text-sm text-gray-500 mt-1">
                      Applied: {new Date(app.appliedAt).toLocaleDateString()}
                    </p>
                  )}
                  {app.interviewDate && (
                    <p className="text-sm text-purple-600 mt-1">
                      🗓️ Interview: {new Date(app.interviewDate).toLocaleDateString()}
                    </p>
                  )}
                </div>
                <div className="flex items-center space-x-2">
                  {editId === app.id ? (
                    <div className="flex items-center space-x-2">
                      <select value={editStatus} onChange={(e) => setEditStatus(e.target.value)}
                        className="border rounded-lg px-2 py-1 text-sm">
                        {STATUS_OPTIONS.filter(s => s !== 'ALL').map(s => (
                          <option key={s} value={s}>{s}</option>
                        ))}
                      </select>
                      <button onClick={() => handleUpdateStatus(app.id)}
                        className="px-3 py-1 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700">Save</button>
                      <button onClick={() => setEditId(null)}
                        className="px-3 py-1 border text-sm rounded-lg hover:bg-gray-50">Cancel</button>
                    </div>
                  ) : (
                    <>
                      <span className={`px-3 py-1 rounded-full text-sm font-medium ${STATUS_COLORS[app.status] || 'bg-gray-100'}`}>
                        {app.status}
                      </span>
                      <button onClick={() => { setEditId(app.id); setEditStatus(app.status); }}
                        className="p-2 text-gray-400 hover:text-gray-600" title="Edit status">✏️</button>
                      <button onClick={() => handleDelete(app.id)}
                        className="p-2 text-gray-400 hover:text-red-600" title="Delete">🗑️</button>
                    </>
                  )}
                </div>
              </div>
              {app.notes && (
                <p className="mt-3 text-sm text-gray-600 bg-gray-50 rounded-lg p-3">{app.notes}</p>
              )}
              <div className="mt-3 flex space-x-3">
                {app.hasTailoredResume && (
                  <span className="text-xs text-green-600 bg-green-50 px-2 py-1 rounded-full">✓ Tailored Resume</span>
                )}
                {app.hasCoverLetter && (
                  <span className="text-xs text-blue-600 bg-blue-50 px-2 py-1 rounded-full">✓ Cover Letter</span>
                )}
              </div>
            </div>
          ))}

          {totalPages > 1 && (
            <div className="flex justify-center space-x-2 mt-6">
              <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}
                className="px-4 py-2 border rounded-lg disabled:opacity-50 hover:bg-gray-50">Previous</button>
              <span className="px-4 py-2 text-gray-600">Page {page + 1} of {totalPages}</span>
              <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1}
                className="px-4 py-2 border rounded-lg disabled:opacity-50 hover:bg-gray-50">Next</button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
