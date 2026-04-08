import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { Target, ClipboardList, FileText, TrendingUp, MapPin, Building2, Upload, Sparkles } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { StatCard } from '../components/ui/StatCard';
import { ProgressBar } from '../components/ui/ProgressBar';
import { SkeletonCard } from '../components/ui/Skeleton';
import { EmptyState } from '../components/ui/EmptyState';
import { Badge } from '../components/ui/Badge';
import * as jobsApi from '../api/jobs';
import * as applicationsApi from '../api/applications';
import * as resumesApi from '../api/resumes';

const STATUS_VARIANT = {
  NEW: 'info',
  VIEWED: 'neutral',
  TAILORING: 'warning',
  READY: 'brand',
  APPLIED: 'success',
};

const APP_STATUS_VARIANT = {
  PENDING: 'warning',
  APPLIED: 'info',
  INTERVIEW: 'brand',
  OFFER: 'success',
  REJECTED: 'danger',
};

export default function DashboardPage() {
  const { user } = useAuth();
  const [matches, setMatches] = useState([]);
  const [applications, setApplications] = useState([]);
  const [matchCount, setMatchCount] = useState(0);
  const [appCount, setAppCount] = useState(0);
  const [resumeCount, setResumeCount] = useState(0);
  const [avgScore, setAvgScore] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [matchData, appData, resumeData] = await Promise.allSettled([
        jobsApi.getMatches(0, 5),
        applicationsApi.getApplications(null, 0, 5),
        resumesApi.getResumes(),
      ]);

      if (matchData.status === 'fulfilled') {
        const content = matchData.value.content || [];
        setMatches(content);
        setMatchCount(matchData.value.totalElements || 0);
        if (content.length > 0) {
          const total = content.reduce((sum, m) => sum + (m.overallScore || 0), 0);
          setAvgScore(Math.round(total / content.length));
        }
      }
      if (appData.status === 'fulfilled') {
        setApplications(appData.value.content || []);
        setAppCount(appData.value.totalElements || 0);
      }
      if (resumeData.status === 'fulfilled') {
        setResumeCount(Array.isArray(resumeData.value) ? resumeData.value.length : 0);
      }

      const allRejected = [matchData, appData, resumeData].every(r => r.status === 'rejected');
      if (allRejected) setError('Failed to load dashboard data. Please try again.');
    } catch {
      setError('Failed to load dashboard data. Please try again.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadData(); }, [loadData]);

  if (loading) {
    return (
      <div className="space-y-8 animate-fade-in">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <SkeletonCard count={4} />
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <SkeletonCard count={2} />
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8 animate-fade-in">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Welcome back, {user?.fullName || 'User'} 👋
          </h1>
          <p className="text-gray-500 mt-1">Here&apos;s what&apos;s happening with your job search</p>
        </div>
        <div className="flex items-center gap-3">
          <Link to="/onboarding" className="btn-secondary text-sm">
            <Upload className="h-4 w-4" />
            Upload Resume
          </Link>
          <Link to="/matches" className="btn-primary text-sm">
            <Sparkles className="h-4 w-4" />
            Browse Matches
          </Link>
        </div>
      </div>

      {/* Error */}
      {error && (
        <div className="flex items-center justify-between rounded-lg border border-red-200 bg-red-50 p-4">
          <p className="text-sm text-red-700">{error}</p>
          <button onClick={loadData} className="btn-ghost text-sm text-red-700 hover:bg-red-100">
            Retry
          </button>
        </div>
      )}

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard
          title="Job Matches"
          value={matchCount}
          subtitle="Total matched jobs"
          icon={Target}
          color="brand"
        />
        <StatCard
          title="Applications"
          value={appCount}
          subtitle="Tracked applications"
          icon={ClipboardList}
          color="green"
        />
        <StatCard
          title="Resumes"
          value={resumeCount}
          subtitle="Uploaded resumes"
          icon={FileText}
          color="blue"
        />
        <StatCard
          title="Avg Match Score"
          value={avgScore ? `${avgScore}%` : '—'}
          subtitle="Across top matches"
          icon={TrendingUp}
          color="amber"
        />
      </div>

      {/* Top Job Matches */}
      <section className="card p-6 animate-slide-up">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-lg font-semibold text-gray-900">Top Job Matches</h2>
          <Link to="/matches" className="text-sm font-medium text-brand-600 hover:text-brand-700">
            View all →
          </Link>
        </div>

        {matches.length === 0 ? (
          <EmptyState
            icon={Target}
            title="No matches yet"
            description="Upload your resume and set your preferences to start receiving AI-powered job matches."
            action={
              <Link to="/onboarding" className="btn-primary text-sm">
                Get Started
              </Link>
            }
          />
        ) : (
          <div className="space-y-4">
            {matches.map((match) => (
              <div
                key={match.matchId}
                className="group rounded-lg border border-gray-100 bg-gray-50/50 p-4 hover:border-brand-200 hover:bg-brand-50/30 transition-all"
              >
                <div className="flex flex-col sm:flex-row sm:items-start gap-4">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <h3 className="font-semibold text-gray-900 truncate">
                        {match.job?.title || 'Job Title'}
                      </h3>
                      {match.status && (
                        <Badge variant={STATUS_VARIANT[match.status] || 'neutral'} dot>
                          {match.status}
                        </Badge>
                      )}
                    </div>
                    <div className="mt-1 flex items-center gap-3 text-sm text-gray-500">
                      <span className="inline-flex items-center gap-1">
                        <Building2 className="h-3.5 w-3.5" />
                        {match.job?.company || 'Company'}
                      </span>
                      <span className="inline-flex items-center gap-1">
                        <MapPin className="h-3.5 w-3.5" />
                        {match.job?.location || 'Location'}
                      </span>
                    </div>

                    <div className="mt-3">
                      <ProgressBar value={match.overallScore || 0} size="sm" showLabel />
                    </div>

                    <div className="mt-2 flex flex-wrap gap-2">
                      {[
                        { label: 'Skills', score: match.skillScore },
                        { label: 'Experience', score: match.experienceScore },
                        { label: 'Location', score: match.locationScore },
                        { label: 'Domain', score: match.domainScore },
                      ].map(({ label, score }) => (
                        <span key={label} className="badge bg-gray-100 text-gray-600">
                          {label}: {score != null ? Math.round(score) : '—'}%
                        </span>
                      ))}
                    </div>
                  </div>

                  <Link
                    to={`/match/${match.matchId}`}
                    className="btn-ghost text-sm whitespace-nowrap"
                  >
                    View Details →
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      {/* Recent Applications */}
      <section className="card animate-slide-up">
        <div className="flex items-center justify-between p-6 pb-0">
          <h2 className="text-lg font-semibold text-gray-900">Recent Applications</h2>
          <Link to="/applications" className="text-sm font-medium text-brand-600 hover:text-brand-700">
            View all →
          </Link>
        </div>

        {applications.length === 0 ? (
          <EmptyState
            icon={ClipboardList}
            title="No applications yet"
            description="Track your first application from the job matches page."
            action={
              <Link to="/matches" className="btn-primary text-sm">
                Browse Matches
              </Link>
            }
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-100">
                  <th className="px-6 py-3 text-left font-medium text-gray-500">Company</th>
                  <th className="px-6 py-3 text-left font-medium text-gray-500">Role</th>
                  <th className="px-6 py-3 text-left font-medium text-gray-500">Status</th>
                  <th className="px-6 py-3 text-left font-medium text-gray-500">Applied</th>
                  <th className="px-6 py-3 text-right font-medium text-gray-500">Match</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {applications.slice(0, 5).map((app) => (
                  <tr key={app.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-6 py-3 font-medium text-gray-900">{app.company || '—'}</td>
                    <td className="px-6 py-3 text-gray-600">{app.jobTitle || '—'}</td>
                    <td className="px-6 py-3">
                      <Badge variant={APP_STATUS_VARIANT[app.status] || 'neutral'}>
                        {app.status || '—'}
                      </Badge>
                    </td>
                    <td className="px-6 py-3 text-gray-500">
                      {app.appliedAt ? new Date(app.appliedAt).toLocaleDateString() : '—'}
                    </td>
                    <td className="px-6 py-3 text-right font-medium text-gray-900">
                      {app.matchScore != null ? `${Math.round(app.matchScore)}%` : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}
