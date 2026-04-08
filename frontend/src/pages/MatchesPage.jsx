import { useState, useEffect, useCallback, useMemo } from 'react';
import { Link } from 'react-router-dom';
import {
  Target, Search, MapPin, Building2, ChevronLeft, ChevronRight,
  Loader2, CheckCircle2, Sparkles, Filter, ArrowUpDown, ExternalLink, Briefcase,
} from 'lucide-react';
import { ProgressBar } from '../components/ui/ProgressBar';
import { SkeletonCard } from '../components/ui/Skeleton';
import { EmptyState } from '../components/ui/EmptyState';
import { Badge } from '../components/ui/Badge';
import * as jobsApi from '../api/jobs';
import * as tailoringApi from '../api/tailoring';
import * as applicationsApi from '../api/applications';

const PAGE_SIZE = 9;

const STATUS_OPTIONS = ['All', 'NEW', 'VIEWED', 'TAILORING', 'READY', 'APPLIED'];

const STATUS_VARIANT = {
  NEW: 'info',
  VIEWED: 'neutral',
  TAILORING: 'warning',
  READY: 'brand',
  APPLIED: 'success',
};

const SORT_OPTIONS = [
  { label: 'Score: High → Low', value: 'score-desc' },
  { label: 'Score: Low → High', value: 'score-asc' },
  { label: 'Newest First', value: 'newest' },
  { label: 'Company A-Z', value: 'company-az' },
];

const SCORE_CATEGORIES = [
  { key: 'skillScore', label: 'Skills', weight: '50%' },
  { key: 'experienceScore', label: 'Experience', weight: '25%' },
  { key: 'locationScore', label: 'Location', weight: '15%' },
  { key: 'domainScore', label: 'Domain', weight: '10%' },
];

function sortMatches(matches, sortValue) {
  const sorted = [...matches];
  switch (sortValue) {
    case 'score-asc':
      return sorted.sort((a, b) => (a.overallScore || 0) - (b.overallScore || 0));
    case 'newest':
      return sorted.sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));
    case 'company-az':
      return sorted.sort((a, b) =>
        (a.job?.company || '').localeCompare(b.job?.company || '')
      );
    case 'score-desc':
    default:
      return sorted.sort((a, b) => (b.overallScore || 0) - (a.overallScore || 0));
  }
}

export default function MatchesPage() {
  const [allMatches, setAllMatches] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Action states per match
  const [tailoring, setTailoring] = useState({});
  const [applying, setApplying] = useState({});

  // Filters
  const [searchQuery, setSearchQuery] = useState('');
  const [minScore, setMinScore] = useState(0);
  const [statusFilter, setStatusFilter] = useState('All');
  const [sortBy, setSortBy] = useState('score-desc');

  const loadMatches = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await jobsApi.getMatches(page, PAGE_SIZE);
      setAllMatches(data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
    } catch {
      setError('Failed to load job matches. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => { loadMatches(); }, [loadMatches]);

  // Client-side filtering + sorting
  const filteredMatches = useMemo(() => {
    let result = allMatches;

    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      result = result.filter(
        (m) =>
          (m.job?.title || '').toLowerCase().includes(q) ||
          (m.job?.company || '').toLowerCase().includes(q)
      );
    }

    if (minScore > 0) {
      result = result.filter((m) => (m.overallScore || 0) >= minScore);
    }

    if (statusFilter !== 'All') {
      result = result.filter((m) => m.status === statusFilter);
    }

    return sortMatches(result, sortBy);
  }, [allMatches, searchQuery, minScore, statusFilter, sortBy]);

  const handleTailor = async (jobId, matchId) => {
    setTailoring((prev) => ({ ...prev, [matchId]: 'loading' }));
    try {
      await tailoringApi.tailorResume(jobId);
      setTailoring((prev) => ({ ...prev, [matchId]: 'done' }));
    } catch {
      setTailoring((prev) => ({ ...prev, [matchId]: 'error' }));
      setTimeout(() => setTailoring((prev) => ({ ...prev, [matchId]: null })), 2000);
    }
  };

  const handleApply = async (jobId, matchId) => {
    setApplying((prev) => ({ ...prev, [matchId]: 'loading' }));
    try {
      await applicationsApi.createApplication(jobId);
      setApplying((prev) => ({ ...prev, [matchId]: 'done' }));
    } catch {
      setApplying((prev) => ({ ...prev, [matchId]: 'error' }));
      setTimeout(() => setApplying((prev) => ({ ...prev, [matchId]: null })), 2000);
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Job Matches</h1>
        <p className="text-gray-500 mt-1">AI-matched jobs ranked by compatibility</p>
      </div>

      {/* Filters */}
      <div className="card p-4">
        <div className="flex flex-col lg:flex-row gap-3">
          {/* Search */}
          <div className="relative flex-1">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              placeholder="Search by title or company…"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="input-field pl-9"
            />
          </div>

          {/* Min Score */}
          <div className="flex items-center gap-2">
            <Filter className="h-4 w-4 text-gray-400 shrink-0" />
            <input
              type="number"
              min={0}
              max={100}
              placeholder="Min score"
              value={minScore || ''}
              onChange={(e) => setMinScore(Number(e.target.value) || 0)}
              className="input-field w-28"
            />
          </div>

          {/* Status */}
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="input-field w-auto"
          >
            {STATUS_OPTIONS.map((s) => (
              <option key={s} value={s}>{s === 'All' ? 'All Statuses' : s}</option>
            ))}
          </select>

          {/* Sort */}
          <div className="flex items-center gap-2">
            <ArrowUpDown className="h-4 w-4 text-gray-400 shrink-0" />
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="input-field w-auto"
            >
              {SORT_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>{opt.label}</option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Error */}
      {error && (
        <div className="flex items-center justify-between rounded-lg border border-red-200 bg-red-50 p-4">
          <p className="text-sm text-red-700">{error}</p>
          <button onClick={loadMatches} className="btn-ghost text-sm text-red-700 hover:bg-red-100">
            Retry
          </button>
        </div>
      )}

      {/* Content */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
          <SkeletonCard count={6} />
        </div>
      ) : filteredMatches.length === 0 ? (
        <EmptyState
          icon={Target}
          title="No matches found"
          description={
            allMatches.length === 0
              ? 'Upload your resume and set preferences to start receiving AI-powered matches.'
              : 'Try adjusting your filters to see more results.'
          }
          action={
            allMatches.length === 0 ? (
              <Link to="/onboarding" className="btn-primary text-sm">Get Started</Link>
            ) : (
              <button
                onClick={() => { setSearchQuery(''); setMinScore(0); setStatusFilter('All'); }}
                className="btn-secondary text-sm"
              >
                Clear Filters
              </button>
            )
          }
        />
      ) : (
        <>
          {/* Results count */}
          <p className="text-sm text-gray-500">
            Showing {filteredMatches.length} of {totalElements} matches
          </p>

          {/* Match Cards Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
            {filteredMatches.map((match) => (
              <div key={match.matchId} className="card p-5 flex flex-col gap-4 animate-slide-up hover:shadow-md transition-shadow">
                {/* Top: Title + Status */}
                <div className="flex items-start justify-between gap-2">
                  <div className="min-w-0 flex-1">
                    <h3 className="font-semibold text-lg text-gray-900 truncate">
                      {match.job?.title || 'Job Title'}
                    </h3>
                    <div className="mt-1 space-y-0.5 text-sm text-gray-500">
                      <p className="flex items-center gap-1.5 truncate">
                        <Building2 className="h-3.5 w-3.5 shrink-0" />
                        {match.job?.company || 'Company'}
                      </p>
                      <p className="flex items-center gap-1.5 truncate">
                        <MapPin className="h-3.5 w-3.5 shrink-0" />
                        {match.job?.location || 'Location'}
                      </p>
                    </div>
                  </div>
                  <div className="flex flex-col items-end gap-2 shrink-0">
                    {match.status && (
                      <Badge variant={STATUS_VARIANT[match.status] || 'neutral'} dot>
                        {match.status}
                      </Badge>
                    )}
                    {match.job?.jobType && (
                      <Badge variant="neutral">
                        <Briefcase className="h-3 w-3" />
                        {match.job.jobType}
                      </Badge>
                    )}
                  </div>
                </div>

                {/* Overall Score */}
                <div>
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-sm font-medium text-gray-700">Overall Match</span>
                    <span className="text-sm font-bold text-gray-900">
                      {match.overallScore != null ? `${Math.round(match.overallScore)}%` : '—'}
                    </span>
                  </div>
                  <ProgressBar value={match.overallScore || 0} size="lg" />
                </div>

                {/* Score Breakdown 2x2 */}
                <div className="grid grid-cols-2 gap-2">
                  {SCORE_CATEGORIES.map(({ key, label, weight }) => (
                    <div key={key} className="rounded-lg bg-gray-50 p-2.5">
                      <div className="flex items-center justify-between mb-1">
                        <span className="text-xs text-gray-500">{label} ({weight})</span>
                        <span className="text-xs font-semibold text-gray-700">
                          {match[key] != null ? Math.round(match[key]) : '—'}%
                        </span>
                      </div>
                      <ProgressBar value={match[key] || 0} size="sm" />
                    </div>
                  ))}
                </div>

                {/* Actions */}
                <div className="flex flex-wrap items-center gap-2 pt-1 mt-auto border-t border-gray-100">
                  <Link
                    to={`/match/${match.matchId}`}
                    className="btn-ghost text-xs px-3 py-1.5"
                  >
                    View Details
                    <ExternalLink className="h-3 w-3" />
                  </Link>

                  <button
                    onClick={() => handleTailor(match.job?.id, match.matchId)}
                    disabled={tailoring[match.matchId] === 'loading' || tailoring[match.matchId] === 'done'}
                    className={`btn-primary text-xs px-3 py-1.5 ${
                      tailoring[match.matchId] === 'done' ? '!bg-green-600 hover:!bg-green-700' : ''
                    }`}
                  >
                    {tailoring[match.matchId] === 'loading' ? (
                      <><Loader2 className="h-3 w-3 animate-spin" /> Tailoring…</>
                    ) : tailoring[match.matchId] === 'done' ? (
                      <><CheckCircle2 className="h-3 w-3" /> Tailored</>
                    ) : (
                      <><Sparkles className="h-3 w-3" /> Tailor Resume</>
                    )}
                  </button>

                  <button
                    onClick={() => handleApply(match.job?.id, match.matchId)}
                    disabled={applying[match.matchId] === 'loading' || applying[match.matchId] === 'done'}
                    className={`btn-secondary text-xs px-3 py-1.5 ${
                      applying[match.matchId] === 'done' ? '!border-green-300 !text-green-700 !bg-green-50' : ''
                    }`}
                  >
                    {applying[match.matchId] === 'loading' ? (
                      <><Loader2 className="h-3 w-3 animate-spin" /> Applying…</>
                    ) : applying[match.matchId] === 'done' ? (
                      <><CheckCircle2 className="h-3 w-3" /> Applied</>
                    ) : (
                      'Track Application'
                    )}
                  </button>
                </div>
              </div>
            ))}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-4 pt-2">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="btn-secondary text-sm"
              >
                <ChevronLeft className="h-4 w-4" />
                Previous
              </button>
              <span className="text-sm text-gray-600">
                Page {page + 1} of {totalPages}
              </span>
              <button
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
                className="btn-secondary text-sm"
              >
                Next
                <ChevronRight className="h-4 w-4" />
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
