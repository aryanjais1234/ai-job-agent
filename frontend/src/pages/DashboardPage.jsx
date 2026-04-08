import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import * as jobsApi from '../api/jobs';
import * as applicationsApi from '../api/applications';
import * as resumesApi from '../api/resumes';

export default function DashboardPage() {
  const { user } = useAuth();
  const [matches, setMatches] = useState([]);
  const [matchCount, setMatchCount] = useState(0);
  const [appCount, setAppCount] = useState(0);
  const [resumeCount, setResumeCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [matchData, appData, resumeData] = await Promise.allSettled([
        jobsApi.getMatches(0, 5),
        applicationsApi.getApplications(null, 0, 1),
        resumesApi.getResumes(),
      ]);
      if (matchData.status === 'fulfilled') {
        setMatches(matchData.value.content || []);
        setMatchCount(matchData.value.totalElements || 0);
      }
      if (appData.status === 'fulfilled') {
        setAppCount(appData.value.totalElements || 0);
      }
      if (resumeData.status === 'fulfilled') {
        setResumeCount(Array.isArray(resumeData.value) ? resumeData.value.length : 0);
      }
    } catch (err) {
      setError('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadData(); }, [loadData]);

  const cards = [
    { title: 'Job Matches', icon: '🎯', count: matchCount, color: 'bg-blue-50 text-blue-700', link: '/matches' },
    { title: 'Applications', icon: '📋', count: appCount, color: 'bg-green-50 text-green-700', link: '/applications' },
    { title: 'Resumes', icon: '📄', count: resumeCount, color: 'bg-purple-50 text-purple-700', link: '/onboarding' },
  ];

  if (loading) {
    return (
      <div className="flex items-center justify-center py-16">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600" />
      </div>
    );
  }

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">Welcome, {user?.fullName || 'User'}!</h1>
        <p className="text-gray-500 mt-1">Here&apos;s an overview of your job search progress.</p>
      </div>

      {error && (
        <div className="mb-6 bg-red-50 border border-red-200 text-red-700 rounded-lg p-4">{error}</div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        {cards.map((card) => (
          <Link key={card.title} to={card.link}
            className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow">
            <div className="flex items-center justify-between mb-4">
              <span className="text-3xl">{card.icon}</span>
              <span className={`text-2xl font-bold px-3 py-1 rounded-lg ${card.color}`}>{card.count}</span>
            </div>
            <h2 className="text-lg font-semibold text-gray-800">{card.title}</h2>
          </Link>
        ))}
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold text-gray-800">Top Job Matches</h2>
          <Link to="/matches" className="text-indigo-600 hover:text-indigo-700 text-sm font-medium">
            View all →
          </Link>
        </div>

        {matches.length === 0 ? (
          <p className="text-gray-500 text-center py-8">No job matches yet. Upload your resume to get started!</p>
        ) : (
          <div className="space-y-3">
            {matches.map((match) => (
              <div key={match.matchId} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors">
                <div>
                  <h3 className="font-medium text-gray-900">{match.job?.title || 'Job Title'}</h3>
                  <p className="text-sm text-gray-500">{match.job?.company || 'Company'} • {match.job?.location || 'Location'}</p>
                </div>
                <div className="flex items-center space-x-3">
                  <span className={`text-lg font-bold px-3 py-1 rounded-lg ${
                    match.overallScore >= 80 ? 'bg-green-100 text-green-700' :
                    match.overallScore >= 60 ? 'bg-yellow-100 text-yellow-700' :
                    'bg-red-100 text-red-700'
                  }`}>
                    {match.overallScore?.toFixed?.(1) || match.overallScore}%
                  </span>
                  <Link to={`/match/${match.matchId}`}
                    className="text-indigo-600 hover:text-indigo-700 text-sm font-medium">
                    Details →
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
