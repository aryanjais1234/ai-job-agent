import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import * as jobsApi from '../api/jobs';
import * as tailoringApi from '../api/tailoring';
import * as applicationsApi from '../api/applications';

export default function MatchesPage() {
  const [matches, setMatches] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [tailoring, setTailoring] = useState({});

  const loadMatches = useCallback(async () => {
    setLoading(true);
    try {
      const data = await jobsApi.getMatches(page, 10);
      setMatches(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch {
      // ignore
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => { loadMatches(); }, [loadMatches]);

  const handleTailor = async (jobId, matchId) => {
    setTailoring(prev => ({ ...prev, [matchId]: 'tailoring' }));
    try {
      await tailoringApi.tailorResume(jobId);
      setTailoring(prev => ({ ...prev, [matchId]: 'done' }));
    } catch {
      setTailoring(prev => ({ ...prev, [matchId]: 'error' }));
    }
  };

  const handleApply = async (jobId, matchId) => {
    try {
      await applicationsApi.createApplication(jobId);
      setTailoring(prev => ({ ...prev, [matchId]: 'applied' }));
    } catch {
      // ignore duplicate
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-16">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600" />
      </div>
    );
  }

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Job Matches</h1>
        <p className="text-gray-500 mt-1">AI-matched jobs based on your resume and preferences.</p>
      </div>

      {matches.length === 0 ? (
        <div className="bg-white rounded-xl shadow-sm border p-12 text-center">
          <p className="text-3xl mb-4">🎯</p>
          <h2 className="text-xl font-semibold text-gray-800 mb-2">No matches yet</h2>
          <p className="text-gray-500 mb-4">Upload your resume and set your preferences to get started.</p>
          <Link to="/onboarding" className="inline-flex items-center px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700">
            Get Started
          </Link>
        </div>
      ) : (
        <div className="space-y-4">
          {matches.map((match) => (
            <div key={match.matchId} className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <h3 className="text-lg font-semibold text-gray-900">{match.job?.title}</h3>
                  <p className="text-gray-600">{match.job?.company} • {match.job?.location}</p>
                  {match.job?.jobType && (
                    <span className="inline-block mt-2 px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded-full">
                      {match.job.jobType}
                    </span>
                  )}
                </div>
                <div className="text-right">
                  <div className={`text-2xl font-bold ${
                    match.overallScore >= 80 ? 'text-green-600' :
                    match.overallScore >= 60 ? 'text-yellow-600' : 'text-red-600'
                  }`}>
                    {match.overallScore?.toFixed?.(1) || match.overallScore}%
                  </div>
                  <p className="text-xs text-gray-500 mt-1">Match Score</p>
                </div>
              </div>

              <div className="mt-4 grid grid-cols-4 gap-2">
                {[
                  { label: 'Skills', score: match.skillScore, weight: '50%' },
                  { label: 'Experience', score: match.experienceScore, weight: '25%' },
                  { label: 'Location', score: match.locationScore, weight: '15%' },
                  { label: 'Domain', score: match.domainScore, weight: '10%' },
                ].map(({ label, score, weight }) => (
                  <div key={label} className="text-center p-2 bg-gray-50 rounded-lg">
                    <div className="text-sm font-medium text-gray-900">{score?.toFixed?.(0) || score}</div>
                    <div className="text-xs text-gray-500">{label} ({weight})</div>
                  </div>
                ))}
              </div>

              <div className="mt-4 flex space-x-3">
                <button
                  onClick={() => handleTailor(match.job?.id, match.matchId)}
                  disabled={tailoring[match.matchId] === 'tailoring'}
                  className="px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed">
                  {tailoring[match.matchId] === 'tailoring' ? '⏳ Tailoring...' :
                   tailoring[match.matchId] === 'done' ? '✓ Tailored' : '✏️ Tailor Resume'}
                </button>
                <button
                  onClick={() => handleApply(match.job?.id, match.matchId)}
                  disabled={tailoring[match.matchId] === 'applied'}
                  className="px-4 py-2 border border-gray-300 text-gray-700 text-sm rounded-lg hover:bg-gray-50 disabled:opacity-50">
                  {tailoring[match.matchId] === 'applied' ? '✓ Applied' : '📋 Track Application'}
                </button>
                <Link to={`/match/${match.matchId}`}
                  className="px-4 py-2 border border-gray-300 text-gray-700 text-sm rounded-lg hover:bg-gray-50">
                  View Details →
                </Link>
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
