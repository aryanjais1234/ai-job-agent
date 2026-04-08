import { useState, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  ArrowLeft, Target, MapPin, Building2, Calendar, ExternalLink,
  Download, FileText, Sparkles, Loader2, CheckCircle2, Clock,
  Briefcase, GraduationCap, Globe,
} from 'lucide-react';
import { ProgressBar } from '../components/ui/ProgressBar';
import { Badge } from '../components/ui/Badge';
import { SkeletonCard } from '../components/ui/Skeleton';
import { EmptyState } from '../components/ui/EmptyState';
import * as jobsApi from '../api/jobs';
import * as tailoringApi from '../api/tailoring';

const SCORE_BREAKDOWN = [
  { key: 'skillScore', label: 'Skills', weight: '50%', icon: Target },
  { key: 'experienceScore', label: 'Experience', weight: '25%', icon: GraduationCap },
  { key: 'locationScore', label: 'Location', weight: '15%', icon: MapPin },
  { key: 'domainScore', label: 'Domain', weight: '10%', icon: Globe },
];

const PLATFORM_VARIANT = {
  INDEED: 'info',
  LINKEDIN: 'brand',
  NAUKRI: 'warning',
};

function scoreColor(value) {
  if (value >= 85) return 'text-brand-700';
  if (value >= 70) return 'text-green-700';
  if (value >= 50) return 'text-amber-700';
  return 'text-red-700';
}

function scoreBg(value) {
  if (value >= 85) return 'bg-brand-50 border-brand-200';
  if (value >= 70) return 'bg-green-50 border-green-200';
  if (value >= 50) return 'bg-amber-50 border-amber-200';
  return 'bg-red-50 border-red-200';
}

function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(new Blob([blob]));
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  window.URL.revokeObjectURL(url);
}

export default function MatchDetailPage() {
  const { id } = useParams();

  // Match data
  const [match, setMatch] = useState(null);
  const [pageLoading, setPageLoading] = useState(true);
  const [pageError, setPageError] = useState(null);

  // Tailoring state
  const [tailored, setTailored] = useState(null);
  const [tailoring, setTailoring] = useState(false);
  const [tailorError, setTailorError] = useState(null);

  // Cover letter state
  const [coverLetter, setCoverLetter] = useState(null);
  const [generatingCover, setGeneratingCover] = useState(false);
  const [coverError, setCoverError] = useState(null);

  // Download state
  const [downloadingResume, setDownloadingResume] = useState(false);
  const [downloadingCover, setDownloadingCover] = useState(false);

  const loadMatch = useCallback(async () => {
    setPageLoading(true);
    setPageError(null);
    try {
      const data = await jobsApi.getMatches(0, 100);
      const found = (data.content || []).find(
        (m) => String(m.matchId) === String(id),
      );
      setMatch(found || null);
    } catch {
      setPageError('Failed to load match details. Please try again.');
    } finally {
      setPageLoading(false);
    }
  }, [id]);

  useEffect(() => { loadMatch(); }, [loadMatch]);

  const handleTailor = async () => {
    if (!match?.job?.id) return;
    setTailoring(true);
    setTailorError(null);
    try {
      const data = await tailoringApi.tailorResume(match.job.id);
      setTailored(data);
    } catch {
      setTailorError('Failed to tailor resume. Please try again.');
    } finally {
      setTailoring(false);
    }
  };

  const handleGenerateCoverLetter = async () => {
    if (!match?.job?.id) return;
    setGeneratingCover(true);
    setCoverError(null);
    try {
      const data = await tailoringApi.generateCoverLetter(match.job.id);
      setCoverLetter(data);
    } catch {
      setCoverError('Failed to generate cover letter. Please try again.');
    } finally {
      setGeneratingCover(false);
    }
  };

  const handleDownloadResume = async () => {
    if (!tailored?.id) return;
    setDownloadingResume(true);
    try {
      const blob = await tailoringApi.downloadResumePdf(tailored.id);
      downloadBlob(blob, 'tailored_resume.pdf');
    } catch {
      setTailorError('Failed to download resume PDF.');
    } finally {
      setDownloadingResume(false);
    }
  };

  const handleDownloadCoverLetter = async () => {
    if (!coverLetter?.id) return;
    setDownloadingCover(true);
    try {
      const blob = await tailoringApi.downloadCoverLetterPdf(coverLetter.id);
      downloadBlob(blob, 'cover_letter.pdf');
    } catch {
      setCoverError('Failed to download cover letter PDF.');
    } finally {
      setDownloadingCover(false);
    }
  };

  /* ---------- Loading state ---------- */
  if (pageLoading) {
    return (
      <div className="max-w-5xl mx-auto space-y-6 animate-fade-in">
        <div className="h-4 w-32 rounded bg-gray-200 animate-pulse" />
        <SkeletonCard count={1} />
        <SkeletonCard count={1} />
        <SkeletonCard count={1} />
      </div>
    );
  }

  /* ---------- Error state ---------- */
  if (pageError) {
    return (
      <div className="max-w-5xl mx-auto animate-fade-in">
        <Link to="/matches" className="inline-flex items-center gap-1.5 text-sm text-brand-600 hover:text-brand-700 mb-6">
          <ArrowLeft className="h-4 w-4" /> Back to Matches
        </Link>
        <div className="card p-6 flex items-center justify-between border-red-200 bg-red-50">
          <p className="text-sm text-red-700">{pageError}</p>
          <button onClick={loadMatch} className="btn-ghost text-sm text-red-700 hover:bg-red-100">Retry</button>
        </div>
      </div>
    );
  }

  /* ---------- Not found ---------- */
  if (!match) {
    return (
      <div className="max-w-5xl mx-auto animate-fade-in">
        <Link to="/matches" className="inline-flex items-center gap-1.5 text-sm text-brand-600 hover:text-brand-700 mb-6">
          <ArrowLeft className="h-4 w-4" /> Back to Matches
        </Link>
        <EmptyState
          icon={Target}
          title="Match not found"
          description="This match may have been removed or the link is invalid."
          action={<Link to="/matches" className="btn-primary text-sm">Browse Matches</Link>}
        />
      </div>
    );
  }

  const { job } = match;
  const analysis = job?.analysis || {};
  const overall = Math.round(match.overallScore || 0);

  return (
    <div className="max-w-5xl mx-auto space-y-8 animate-fade-in">
      {/* ---------- Back Link ---------- */}
      <Link
        to="/matches"
        className="inline-flex items-center gap-1.5 text-sm font-medium text-brand-600 hover:text-brand-700 transition-colors"
      >
        <ArrowLeft className="h-4 w-4" /> Back to Matches
      </Link>

      {/* ========== TOP SECTION ========== */}
      <div className="card p-6 animate-slide-up">
        <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-4">
          <div className="min-w-0 flex-1">
            <h1 className="text-2xl font-bold text-gray-900 truncate">{job?.title || 'Job Title'}</h1>
            <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-1 text-sm text-gray-500">
              <span className="inline-flex items-center gap-1.5">
                <Building2 className="h-4 w-4 shrink-0" /> {job?.company || 'Company'}
              </span>
              <span className="inline-flex items-center gap-1.5">
                <MapPin className="h-4 w-4 shrink-0" /> {job?.location || 'Location'}
              </span>
              {job?.postedAt && (
                <span className="inline-flex items-center gap-1.5">
                  <Calendar className="h-4 w-4 shrink-0" />
                  {new Date(job.postedAt).toLocaleDateString()}
                </span>
              )}
              {job?.jobType && (
                <span className="inline-flex items-center gap-1.5">
                  <Briefcase className="h-4 w-4 shrink-0" /> {job.jobType}
                </span>
              )}
            </div>
            {(job?.salaryMin || job?.salaryMax) && (
              <p className="mt-1 text-sm text-gray-600">
                💰 {job.salaryMin ? `$${job.salaryMin.toLocaleString()}` : ''}
                {job.salaryMin && job.salaryMax ? ' – ' : ''}
                {job.salaryMax ? `$${job.salaryMax.toLocaleString()}` : ''}
              </p>
            )}
          </div>
          <div className="flex items-center gap-2 shrink-0">
            {job?.sourcePlatform && (
              <Badge variant={PLATFORM_VARIANT[job.sourcePlatform] || 'neutral'} dot>
                {job.sourcePlatform}
              </Badge>
            )}
            {job?.sourceUrl && (
              <a
                href={job.sourceUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="btn-ghost text-sm px-3 py-1.5"
              >
                <ExternalLink className="h-4 w-4" /> View Listing
              </a>
            )}
          </div>
        </div>
      </div>

      {/* ========== MATCH SCORE CARD ========== */}
      <div className="card p-6 animate-slide-up">
        <h2 className="text-lg font-semibold text-gray-900 mb-5 flex items-center gap-2">
          <Target className="h-5 w-5 text-brand-600" /> Match Score
        </h2>
        <div className="flex flex-col lg:flex-row gap-8">
          {/* Overall score circle */}
          <div className="flex flex-col items-center justify-center shrink-0">
            <div className={`relative h-32 w-32 rounded-full border-4 flex items-center justify-center ${scoreBg(overall)}`}>
              <span className={`text-4xl font-extrabold ${scoreColor(overall)}`}>{overall}</span>
              <span className={`absolute bottom-3 text-xs font-medium ${scoreColor(overall)}`}>/ 100</span>
            </div>
            <p className="mt-2 text-sm font-medium text-gray-600">Overall Match</p>
          </div>

          {/* Breakdown */}
          <div className="flex-1 grid grid-cols-1 sm:grid-cols-2 gap-4">
            {SCORE_BREAKDOWN.map(({ key, label, weight, icon: Icon }) => {
              const val = Math.round(match[key] || 0);
              return (
                <div key={key} className="rounded-lg bg-gray-50 p-4">
                  <div className="flex items-center gap-2 mb-2">
                    <Icon className="h-4 w-4 text-gray-400" />
                    <span className="text-sm font-medium text-gray-700">{label}</span>
                    <span className="ml-auto text-xs text-gray-400">{weight}</span>
                  </div>
                  <div className="flex items-center gap-3">
                    <ProgressBar value={val} size="md" className="flex-1" />
                    <span className={`text-sm font-bold ${scoreColor(val)}`}>{val}%</span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* ========== JOB ANALYSIS ========== */}
      {Object.keys(analysis).length > 0 && (
        <div className="card p-6 animate-slide-up">
          <h2 className="text-lg font-semibold text-gray-900 mb-5 flex items-center gap-2">
            <Briefcase className="h-5 w-5 text-brand-600" /> Job Analysis
          </h2>
          <div className="space-y-5">
            {/* Required Skills */}
            {analysis.requiredSkills?.length > 0 && (
              <div>
                <h3 className="text-sm font-medium text-gray-700 mb-2">Required Skills</h3>
                <div className="flex flex-wrap gap-2">
                  {analysis.requiredSkills.map((skill) => (
                    <Badge key={skill} variant="brand">{skill}</Badge>
                  ))}
                </div>
              </div>
            )}

            {/* Nice-to-Have Skills */}
            {analysis.niceToHaveSkills?.length > 0 && (
              <div>
                <h3 className="text-sm font-medium text-gray-700 mb-2">Nice-to-Have Skills</h3>
                <div className="flex flex-wrap gap-2">
                  {analysis.niceToHaveSkills.map((skill) => (
                    <Badge key={skill} variant="neutral">{skill}</Badge>
                  ))}
                </div>
              </div>
            )}

            {/* Metadata row */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
              {(analysis.experienceMin != null || analysis.experienceMax != null) && (
                <div>
                  <h3 className="text-xs font-medium text-gray-500 mb-1">Experience Range</h3>
                  <Badge variant="info">
                    <GraduationCap className="h-3 w-3" />
                    {analysis.experienceMin ?? 0}–{analysis.experienceMax ?? '?'} years
                  </Badge>
                </div>
              )}
              {analysis.seniorityLevel && (
                <div>
                  <h3 className="text-xs font-medium text-gray-500 mb-1">Seniority</h3>
                  <Badge variant="warning">{analysis.seniorityLevel}</Badge>
                </div>
              )}
              {analysis.remoteType && (
                <div>
                  <h3 className="text-xs font-medium text-gray-500 mb-1">Remote Type</h3>
                  <Badge variant="success">
                    <Globe className="h-3 w-3" /> {analysis.remoteType}
                  </Badge>
                </div>
              )}
              {analysis.domain && (
                <div>
                  <h3 className="text-xs font-medium text-gray-500 mb-1">Domain</h3>
                  <Badge variant="brand">{analysis.domain}</Badge>
                </div>
              )}
            </div>

            {/* ATS Keywords */}
            {analysis.keywords?.length > 0 && (
              <div>
                <h3 className="text-sm font-medium text-gray-700 mb-2">ATS Keywords</h3>
                <div className="flex flex-wrap gap-2">
                  {analysis.keywords.map((kw) => (
                    <Badge key={kw} variant="info">{kw}</Badge>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* ========== TAILORING SECTION ========== */}
      <div className="card p-6 animate-slide-up">
        <h2 className="text-lg font-semibold text-gray-900 mb-5 flex items-center gap-2">
          <Sparkles className="h-5 w-5 text-brand-600" /> Resume Tailoring
        </h2>

        {tailorError && (
          <div className="mb-4 rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">
            {tailorError}
          </div>
        )}

        {!tailored ? (
          <div className="text-center py-10">
            <div className="mb-3 inline-flex rounded-xl bg-brand-50 p-4">
              <Sparkles className="h-8 w-8 text-brand-500" />
            </div>
            <p className="text-gray-500 mb-5">Tailor your resume for this job using AI analysis</p>
            <button onClick={handleTailor} disabled={tailoring} className="btn-primary">
              {tailoring ? (
                <><Loader2 className="h-4 w-4 animate-spin" /> Tailoring Resume…</>
              ) : (
                <><Sparkles className="h-4 w-4" /> Tailor Resume</>
              )}
            </button>
          </div>
        ) : (
          <div className="space-y-6">
            {/* ATS Score */}
            <div className="rounded-lg bg-gray-50 p-5">
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium text-gray-700">ATS Compatibility Score</span>
                <span className={`text-lg font-bold ${scoreColor(tailored.atsScore || 0)}`}>
                  {Math.round(tailored.atsScore || 0)}%
                </span>
              </div>
              <ProgressBar value={tailored.atsScore || 0} size="lg" showLabel={false} />
            </div>

            {/* Modifications Log */}
            {tailored.modificationsLog?.length > 0 && (
              <div>
                <h3 className="text-sm font-semibold text-gray-700 mb-3 flex items-center gap-1.5">
                  <CheckCircle2 className="h-4 w-4 text-green-600" /> Modifications ({tailored.modificationsLog.length})
                </h3>
                <div className="overflow-x-auto rounded-lg border border-gray-200">
                  <table className="min-w-full text-sm">
                    <thead className="bg-gray-50 border-b border-gray-200">
                      <tr>
                        <th className="px-4 py-2.5 text-left font-medium text-gray-600">Field</th>
                        <th className="px-4 py-2.5 text-left font-medium text-gray-600">Original</th>
                        <th className="px-4 py-2.5 text-left font-medium text-gray-600">Modified</th>
                        <th className="px-4 py-2.5 text-left font-medium text-gray-600">Reason</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                      {tailored.modificationsLog.map((mod, idx) => (
                        <tr key={idx} className="hover:bg-gray-50/50">
                          <td className="px-4 py-3 font-medium text-gray-900 whitespace-nowrap">{mod.field}</td>
                          <td className="px-4 py-3 text-gray-500">{mod.original || '—'}</td>
                          <td className="px-4 py-3 text-green-700 bg-green-50/50">{mod.modified}</td>
                          <td className="px-4 py-3 text-gray-500 italic">{mod.reason}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {/* Resume Content Preview */}
            {tailored.tailoredContent && (
              <div>
                <h3 className="text-sm font-semibold text-gray-700 mb-3 flex items-center gap-1.5">
                  <FileText className="h-4 w-4 text-brand-600" /> Tailored Resume Preview
                </h3>
                <div className="rounded-lg border border-gray-200 bg-white p-5 max-h-96 overflow-y-auto text-sm leading-relaxed whitespace-pre-wrap text-gray-700">
                  {tailored.tailoredContent}
                </div>
              </div>
            )}

            {/* Download Buttons */}
            <div className="flex flex-wrap gap-3 pt-2">
              <button onClick={handleDownloadResume} disabled={downloadingResume} className="btn-primary">
                {downloadingResume ? (
                  <><Loader2 className="h-4 w-4 animate-spin" /> Downloading…</>
                ) : (
                  <><Download className="h-4 w-4" /> Download Resume PDF</>
                )}
              </button>

              {coverLetter ? (
                <button onClick={handleDownloadCoverLetter} disabled={downloadingCover} className="btn-secondary">
                  {downloadingCover ? (
                    <><Loader2 className="h-4 w-4 animate-spin" /> Downloading…</>
                  ) : (
                    <><Download className="h-4 w-4" /> Download Cover Letter PDF</>
                  )}
                </button>
              ) : (
                <button onClick={handleGenerateCoverLetter} disabled={generatingCover} className="btn-secondary">
                  {generatingCover ? (
                    <><Loader2 className="h-4 w-4 animate-spin" /> Generating…</>
                  ) : (
                    <><FileText className="h-4 w-4" /> Generate Cover Letter</>
                  )}
                </button>
              )}
            </div>
          </div>
        )}
      </div>

      {/* ========== COVER LETTER SECTION ========== */}
      {coverLetter && (
        <div className="card p-6 animate-slide-up">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
            <FileText className="h-5 w-5 text-brand-600" /> Cover Letter
          </h2>

          {coverError && (
            <div className="mb-4 rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">
              {coverError}
            </div>
          )}

          <div className="rounded-lg border border-gray-200 bg-white p-5 max-h-96 overflow-y-auto text-sm leading-relaxed whitespace-pre-wrap text-gray-700">
            {coverLetter.content}
          </div>
          <div className="mt-4">
            <button onClick={handleDownloadCoverLetter} disabled={downloadingCover} className="btn-primary">
              {downloadingCover ? (
                <><Loader2 className="h-4 w-4 animate-spin" /> Downloading…</>
              ) : (
                <><Download className="h-4 w-4" /> Download Cover Letter PDF</>
              )}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
