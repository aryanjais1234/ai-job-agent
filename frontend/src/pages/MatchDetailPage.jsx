import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import * as tailoringApi from '../api/tailoring';

export default function MatchDetailPage() {
  const { id } = useParams();
  const [tailored, setTailored] = useState(null);
  const [coverLetter, setCoverLetter] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleTailor = async (jobId) => {
    setLoading(true);
    setError(null);
    try {
      const data = await tailoringApi.tailorResume(jobId);
      setTailored(data);
    } catch {
      setError('Failed to tailor resume. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateCoverLetter = async (jobId) => {
    setLoading(true);
    setError(null);
    try {
      const data = await tailoringApi.generateCoverLetter(jobId);
      setCoverLetter(data);
    } catch {
      setError('Failed to generate cover letter. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadResume = async () => {
    if (!tailored?.id) return;
    try {
      const blob = await tailoringApi.downloadResumePdf(tailored.id);
      const url = window.URL.createObjectURL(new Blob([blob]));
      const a = document.createElement('a');
      a.href = url;
      a.download = 'tailored_resume.pdf';
      a.click();
      window.URL.revokeObjectURL(url);
    } catch {
      setError('Failed to download PDF');
    }
  };

  const handleDownloadCoverLetter = async () => {
    if (!coverLetter?.id) return;
    try {
      const blob = await tailoringApi.downloadCoverLetterPdf(coverLetter.id);
      const url = window.URL.createObjectURL(new Blob([blob]));
      const a = document.createElement('a');
      a.href = url;
      a.download = 'cover_letter.pdf';
      a.click();
      window.URL.revokeObjectURL(url);
    } catch {
      setError('Failed to download PDF');
    }
  };

  return (
    <div className="max-w-4xl mx-auto">
      <Link to="/matches" className="text-indigo-600 hover:text-indigo-700 text-sm mb-4 inline-block">← Back to Matches</Link>

      <h1 className="text-2xl font-bold text-gray-900 mb-6">Match Detail #{id}</h1>

      {error && (
        <div className="mb-4 bg-red-50 border border-red-200 text-red-700 rounded-lg p-4">{error}</div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl shadow-sm border p-6">
          <h2 className="text-lg font-semibold mb-4">✏️ Tailored Resume</h2>
          {tailored ? (
            <div>
              <div className="flex items-center justify-between mb-3">
                <span className="text-sm text-gray-600">ATS Score: <strong className="text-green-600">{tailored.atsScore}%</strong></span>
                <button onClick={handleDownloadResume}
                  className="px-3 py-1 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700">
                  📥 Download PDF
                </button>
              </div>
              <div className="bg-gray-50 rounded-lg p-4 max-h-96 overflow-y-auto text-sm whitespace-pre-wrap">
                {tailored.tailoredContent}
              </div>
            </div>
          ) : (
            <div className="text-center py-8">
              <p className="text-gray-500 mb-4">Tailor your resume for this job using AI</p>
              <button onClick={() => handleTailor(id)} disabled={loading}
                className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50">
                {loading ? '⏳ Tailoring...' : '✏️ Tailor Resume'}
              </button>
            </div>
          )}
        </div>

        <div className="bg-white rounded-xl shadow-sm border p-6">
          <h2 className="text-lg font-semibold mb-4">📝 Cover Letter</h2>
          {coverLetter ? (
            <div>
              <div className="flex justify-end mb-3">
                <button onClick={handleDownloadCoverLetter}
                  className="px-3 py-1 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700">
                  📥 Download PDF
                </button>
              </div>
              <div className="bg-gray-50 rounded-lg p-4 max-h-96 overflow-y-auto text-sm whitespace-pre-wrap">
                {coverLetter.content}
              </div>
            </div>
          ) : (
            <div className="text-center py-8">
              <p className="text-gray-500 mb-4">Generate a personalized cover letter</p>
              <button onClick={() => handleGenerateCoverLetter(id)} disabled={loading}
                className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50">
                {loading ? '⏳ Generating...' : '📝 Generate Cover Letter'}
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
