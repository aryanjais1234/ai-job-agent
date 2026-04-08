import { useState, useEffect, useCallback, useMemo } from 'react';
import { Link } from 'react-router-dom';
import {
  ClipboardList, Search, Calendar, Edit3, Trash2, ChevronLeft,
  ChevronRight, CheckCircle2, XCircle, Clock, AlertCircle,
  Briefcase, MapPin, Loader2, ExternalLink,
} from 'lucide-react';
import { Badge } from '../components/ui/Badge';
import { ProgressBar } from '../components/ui/ProgressBar';
import { SkeletonCard } from '../components/ui/Skeleton';
import { EmptyState } from '../components/ui/EmptyState';
import { Modal } from '../components/ui/Modal';
import * as applicationsApi from '../api/applications';

const PAGE_SIZE = 10;

const STATUS_OPTIONS = ['ALL', 'PENDING', 'APPLIED', 'INTERVIEW', 'OFFER', 'REJECTED', 'WITHDRAWN'];

const STATUS_VARIANT = {
  PENDING: 'neutral',
  APPLIED: 'info',
  INTERVIEW: 'warning',
  OFFER: 'success',
  REJECTED: 'danger',
  WITHDRAWN: 'neutral',
};

const STATUS_ICON = {
  PENDING: Clock,
  APPLIED: CheckCircle2,
  INTERVIEW: Calendar,
  OFFER: CheckCircle2,
  REJECTED: XCircle,
  WITHDRAWN: AlertCircle,
};

export default function ApplicationsPage() {
  const [applications, setApplications] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');

  // Status counts
  const [statusCounts, setStatusCounts] = useState({});

  // Edit modal state
  const [editApp, setEditApp] = useState(null);
  const [editForm, setEditForm] = useState({ status: '', notes: '', interviewDate: '' });
  const [saving, setSaving] = useState(false);

  // Delete modal state
  const [deleteApp, setDeleteApp] = useState(null);
  const [deleting, setDeleting] = useState(false);

  // Expanded notes tracking
  const [expandedNotes, setExpandedNotes] = useState({});

  const loadApplications = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const status = filter === 'ALL' ? null : filter;
      const data = await applicationsApi.getApplications(status, page, PAGE_SIZE);
      setApplications(data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
    } catch {
      setError('Failed to load applications. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [filter, page]);

  // Load all statuses for counts on mount
  const loadStatusCounts = useCallback(async () => {
    try {
      const data = await applicationsApi.getApplications(null, 0, 1);
      const total = data.totalElements || 0;
      const counts = { ALL: total };
      const statuses = ['PENDING', 'APPLIED', 'INTERVIEW', 'OFFER', 'REJECTED', 'WITHDRAWN'];
      const results = await Promise.allSettled(
        statuses.map((s) => applicationsApi.getApplications(s, 0, 1)),
      );
      statuses.forEach((s, i) => {
        counts[s] = results[i].status === 'fulfilled' ? (results[i].value.totalElements || 0) : 0;
      });
      setStatusCounts(counts);
    } catch {
      // Non-critical, keep going
    }
  }, []);

  useEffect(() => { loadApplications(); }, [loadApplications]);
  useEffect(() => { loadStatusCounts(); }, [loadStatusCounts]);

  // Client-side search filter
  const filteredApps = useMemo(() => {
    if (!searchQuery.trim()) return applications;
    const q = searchQuery.toLowerCase();
    return applications.filter(
      (app) =>
        (app.jobTitle || '').toLowerCase().includes(q) ||
        (app.company || '').toLowerCase().includes(q),
    );
  }, [applications, searchQuery]);

  // Edit handlers
  const openEdit = (app) => {
    setEditApp(app);
    setEditForm({
      status: app.status || 'PENDING',
      notes: app.notes || '',
      interviewDate: app.interviewDate ? app.interviewDate.slice(0, 10) : '',
    });
  };

  const handleSave = async () => {
    if (!editApp) return;
    setSaving(true);
    try {
      await applicationsApi.updateApplication(editApp.id, {
        status: editForm.status,
        notes: editForm.notes,
        interviewDate: editForm.interviewDate || null,
      });
      setEditApp(null);
      loadApplications();
      loadStatusCounts();
    } catch {
      setError('Failed to update application.');
    } finally {
      setSaving(false);
    }
  };

  // Delete handlers
  const handleDelete = async () => {
    if (!deleteApp) return;
    setDeleting(true);
    try {
      await applicationsApi.deleteApplication(deleteApp.id);
      setDeleteApp(null);
      loadApplications();
      loadStatusCounts();
    } catch {
      setError('Failed to delete application.');
    } finally {
      setDeleting(false);
    }
  };

  const toggleNotes = (id) => {
    setExpandedNotes((prev) => ({ ...prev, [id]: !prev[id] }));
  };

  const stats = [
    { label: 'Total', value: statusCounts.ALL ?? totalElements, variant: 'neutral' },
    { label: 'Applied', value: statusCounts.APPLIED ?? 0, variant: 'info' },
    { label: 'Interview', value: statusCounts.INTERVIEW ?? 0, variant: 'warning' },
    { label: 'Offer', value: statusCounts.OFFER ?? 0, variant: 'success' },
    { label: 'Rejected', value: statusCounts.REJECTED ?? 0, variant: 'danger' },
  ];

  return (
    <div className="space-y-6 animate-fade-in">
      {/* ========== HEADER ========== */}
      <div>
        <div className="flex items-center gap-3 mb-1">
          <ClipboardList className="h-7 w-7 text-brand-600" />
          <h1 className="text-2xl font-bold text-gray-900">Application Tracker</h1>
        </div>
        <p className="text-gray-500">Track and manage your job applications</p>

        {/* Stats Row */}
        <div className="flex flex-wrap gap-2 mt-4">
          {stats.map((s) => (
            <Badge key={s.label} variant={s.variant}>
              {s.label}: {s.value}
            </Badge>
          ))}
        </div>
      </div>

      {/* ========== FILTER BAR ========== */}
      <div className="card p-4 space-y-3">
        {/* Status Tabs */}
        <div className="flex flex-wrap gap-2">
          {STATUS_OPTIONS.map((status) => {
            const isActive = filter === status;
            const count = statusCounts[status];
            return (
              <button
                key={status}
                onClick={() => { setFilter(status); setPage(0); }}
                className={`inline-flex items-center gap-1.5 px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-brand-600 text-white shadow-sm'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}
              >
                {status === 'ALL' ? 'All' : status.charAt(0) + status.slice(1).toLowerCase()}
                {count != null && (
                  <span className={`text-xs rounded-full px-1.5 py-0.5 ${
                    isActive ? 'bg-white/20 text-white' : 'bg-gray-200 text-gray-500'
                  }`}>
                    {count}
                  </span>
                )}
              </button>
            );
          })}
        </div>

        {/* Search */}
        <div className="relative">
          <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Search by company or role…"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="input-field pl-9"
          />
        </div>
      </div>

      {/* ========== ERROR ========== */}
      {error && (
        <div className="flex items-center justify-between rounded-lg border border-red-200 bg-red-50 p-4">
          <div className="flex items-center gap-2 text-sm text-red-700">
            <AlertCircle className="h-4 w-4 shrink-0" />
            {error}
          </div>
          <button onClick={() => { setError(null); loadApplications(); }} className="btn-ghost text-sm text-red-700 hover:bg-red-100">
            Retry
          </button>
        </div>
      )}

      {/* ========== CONTENT ========== */}
      {loading ? (
        <div className="space-y-4">
          <SkeletonCard count={4} />
        </div>
      ) : filteredApps.length === 0 ? (
        <EmptyState
          icon={ClipboardList}
          title="No applications yet"
          description="Start by browsing your job matches and tracking applications"
          action={
            <Link to="/matches" className="btn-primary text-sm">
              <Briefcase className="h-4 w-4" /> Browse Matches
            </Link>
          }
        />
      ) : (
        <>
          {/* Results Count */}
          <p className="text-sm text-gray-500">
            Showing {filteredApps.length} application{filteredApps.length !== 1 ? 's' : ''}
          </p>

          {/* Application Cards */}
          <div className="space-y-4">
            {filteredApps.map((app) => {
              const SIcon = STATUS_ICON[app.status] || Clock;
              const isExpanded = expandedNotes[app.id];
              const hasLongNotes = app.notes && app.notes.length > 120;

              return (
                <div
                  key={app.id}
                  className="card p-5 hover:shadow-md transition-shadow animate-slide-up"
                >
                  <div className="flex flex-col sm:flex-row sm:items-start gap-4">
                    {/* Left: Info */}
                    <div className="flex-1 min-w-0">
                      <div className="flex items-start gap-3">
                        <div className="min-w-0 flex-1">
                          <h3 className="font-semibold text-gray-900 truncate">
                            {app.company || 'Company'}
                          </h3>
                          <p className="text-sm text-gray-600 truncate flex items-center gap-1.5 mt-0.5">
                            <Briefcase className="h-3.5 w-3.5 shrink-0" />
                            {app.jobTitle || 'Job Title'}
                          </p>
                          {app.location && (
                            <p className="text-sm text-gray-500 flex items-center gap-1.5 mt-0.5">
                              <MapPin className="h-3.5 w-3.5 shrink-0" />
                              {app.location}
                            </p>
                          )}
                        </div>
                        <Badge variant={STATUS_VARIANT[app.status] || 'neutral'} dot>
                          <SIcon className="h-3 w-3" />
                          {app.status}
                        </Badge>
                      </div>

                      {/* Meta row */}
                      <div className="flex flex-wrap items-center gap-x-4 gap-y-1 mt-3 text-xs text-gray-500">
                        {app.appliedAt && (
                          <span className="inline-flex items-center gap-1">
                            <Clock className="h-3 w-3" />
                            Applied {new Date(app.appliedAt).toLocaleDateString()}
                          </span>
                        )}
                        {app.interviewDate && (
                          <span className="inline-flex items-center gap-1 text-amber-600 font-medium">
                            <Calendar className="h-3 w-3" />
                            Interview {new Date(app.interviewDate).toLocaleDateString()}
                          </span>
                        )}
                        {app.matchScore != null && (
                          <span className="inline-flex items-center gap-2">
                            Match:
                            <ProgressBar value={app.matchScore} size="sm" className="w-20" />
                            <span className="font-medium">{Math.round(app.matchScore)}%</span>
                          </span>
                        )}
                      </div>

                      {/* Notes */}
                      {app.notes && (
                        <div className="mt-3">
                          <p className="text-sm text-gray-600 bg-gray-50 rounded-lg p-3">
                            {hasLongNotes && !isExpanded
                              ? `${app.notes.slice(0, 120)}…`
                              : app.notes}
                          </p>
                          {hasLongNotes && (
                            <button
                              onClick={() => toggleNotes(app.id)}
                              className="text-xs text-brand-600 hover:text-brand-700 mt-1"
                            >
                              {isExpanded ? 'Show less' : 'Show more'}
                            </button>
                          )}
                        </div>
                      )}

                      {/* Tags */}
                      <div className="flex flex-wrap gap-2 mt-3">
                        {app.hasTailoredResume && (
                          <Badge variant="success">
                            <CheckCircle2 className="h-3 w-3" /> Tailored Resume
                          </Badge>
                        )}
                        {app.hasCoverLetter && (
                          <Badge variant="info">
                            <CheckCircle2 className="h-3 w-3" /> Cover Letter
                          </Badge>
                        )}
                      </div>
                    </div>

                    {/* Right: Actions */}
                    <div className="flex sm:flex-col gap-2 shrink-0">
                      <button
                        onClick={() => openEdit(app)}
                        className="btn-ghost text-sm px-3 py-1.5"
                        title="Edit application"
                      >
                        <Edit3 className="h-4 w-4" /> Edit
                      </button>
                      <button
                        onClick={() => setDeleteApp(app)}
                        className="btn-ghost text-sm px-3 py-1.5 text-red-600 hover:bg-red-50"
                        title="Delete application"
                      >
                        <Trash2 className="h-4 w-4" /> Delete
                      </button>
                      {app.sourceUrl && (
                        <a
                          href={app.sourceUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="btn-ghost text-sm px-3 py-1.5"
                        >
                          <ExternalLink className="h-4 w-4" /> View
                        </a>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-4 pt-2">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="btn-secondary text-sm"
              >
                <ChevronLeft className="h-4 w-4" /> Previous
              </button>
              <span className="text-sm text-gray-600">
                Page {page + 1} of {totalPages}
              </span>
              <button
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
                className="btn-secondary text-sm"
              >
                Next <ChevronRight className="h-4 w-4" />
              </button>
            </div>
          )}
        </>
      )}

      {/* ========== EDIT MODAL ========== */}
      <Modal
        isOpen={!!editApp}
        onClose={() => setEditApp(null)}
        title="Edit Application"
        size="md"
      >
        <div className="space-y-4">
          {/* Status */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
            <select
              value={editForm.status}
              onChange={(e) => setEditForm((f) => ({ ...f, status: e.target.value }))}
              className="input-field"
            >
              {STATUS_OPTIONS.filter((s) => s !== 'ALL').map((s) => (
                <option key={s} value={s}>
                  {s.charAt(0) + s.slice(1).toLowerCase()}
                </option>
              ))}
            </select>
          </div>

          {/* Interview Date */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              <span className="flex items-center gap-1.5"><Calendar className="h-3.5 w-3.5" /> Interview Date</span>
            </label>
            <input
              type="date"
              value={editForm.interviewDate}
              onChange={(e) => setEditForm((f) => ({ ...f, interviewDate: e.target.value }))}
              className="input-field"
            />
          </div>

          {/* Notes */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Notes</label>
            <textarea
              rows={4}
              value={editForm.notes}
              onChange={(e) => setEditForm((f) => ({ ...f, notes: e.target.value }))}
              placeholder="Add notes about this application…"
              className="input-field resize-none"
            />
          </div>

          {/* Actions */}
          <div className="flex justify-end gap-2 pt-2">
            <button onClick={() => setEditApp(null)} className="btn-secondary text-sm">
              Cancel
            </button>
            <button onClick={handleSave} disabled={saving} className="btn-primary text-sm">
              {saving ? (
                <><Loader2 className="h-4 w-4 animate-spin" /> Saving…</>
              ) : (
                'Save Changes'
              )}
            </button>
          </div>
        </div>
      </Modal>

      {/* ========== DELETE CONFIRMATION MODAL ========== */}
      <Modal
        isOpen={!!deleteApp}
        onClose={() => setDeleteApp(null)}
        title="Delete Application"
        size="sm"
      >
        <div className="space-y-4">
          <div className="flex items-start gap-3">
            <div className="rounded-full bg-red-100 p-2 shrink-0">
              <Trash2 className="h-5 w-5 text-red-600" />
            </div>
            <div>
              <p className="text-sm text-gray-700">
                Are you sure you want to delete the application for{' '}
                <strong>{deleteApp?.jobTitle}</strong> at{' '}
                <strong>{deleteApp?.company}</strong>?
              </p>
              <p className="text-xs text-gray-500 mt-1">This action cannot be undone.</p>
            </div>
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <button onClick={() => setDeleteApp(null)} className="btn-secondary text-sm">
              Cancel
            </button>
            <button onClick={handleDelete} disabled={deleting} className="btn-danger text-sm">
              {deleting ? (
                <><Loader2 className="h-4 w-4 animate-spin" /> Deleting…</>
              ) : (
                <><Trash2 className="h-4 w-4" /> Delete</>
              )}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
