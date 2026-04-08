import { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Upload, Loader2, Star, Trash2, CheckCircle2, X, FileText,
  Briefcase, GraduationCap, ChevronRight, ChevronLeft,
} from 'lucide-react';
import * as resumesApi from '../api/resumes';
import * as usersApi from '../api/users';
import { Badge } from '../components/ui/Badge';
import { ProgressBar } from '../components/ui/ProgressBar';
import { useToast } from '../components/ui/Toast';

const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
const STEP_LABELS = ['Resume Upload', 'Profile & Preferences', 'Review & Confirm'];

function tryParse(val) {
  if (Array.isArray(val)) return val;
  if (typeof val === 'string') {
    try { return JSON.parse(val); } catch { return []; }
  }
  return [];
}

function formatFileSize(bytes) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export default function OnboardingPage() {
  const [step, setStep] = useState(1);
  const [resumes, setResumes] = useState([]);
  const [uploading, setUploading] = useState(false);
  const [deleteConfirmId, setDeleteConfirmId] = useState(null);
  const [profile, setProfile] = useState({
    fullName: '', phone: '', location: '', experienceYears: 0,
    linkedinUrl: '', githubUrl: '', portfolioUrl: '',
  });
  const [preferences, setPreferences] = useState({
    jobTitles: [], locations: [], minSalary: '', maxSalary: '',
    remoteOk: false, industries: [], experienceLevels: [], skillsRequired: [],
  });
  const [saving, setSaving] = useState(false);
  const [newItem, setNewItem] = useState({
    jobTitles: '', locations: '', industries: '', skillsRequired: '',
  });
  const [dragOver, setDragOver] = useState(false);
  const fileInputRef = useRef(null);
  const navigate = useNavigate();
  const toast = useToast();

  const loadData = useCallback(async () => {
    try {
      const [resumeData, profileData, prefData] = await Promise.allSettled([
        resumesApi.getResumes(),
        usersApi.getProfile(),
        usersApi.getPreferences(),
      ]);
      if (resumeData.status === 'fulfilled')
        setResumes(Array.isArray(resumeData.value) ? resumeData.value : []);
      if (profileData.status === 'fulfilled') {
        const p = profileData.value;
        setProfile({
          fullName: p.fullName || '', phone: p.phone || '', location: p.location || '',
          experienceYears: p.experienceYears || 0, linkedinUrl: p.linkedinUrl || '',
          githubUrl: p.githubUrl || '', portfolioUrl: p.portfolioUrl || '',
        });
      }
      if (prefData.status === 'fulfilled' && prefData.value) {
        const d = prefData.value;
        setPreferences({
          jobTitles: tryParse(d.jobTitles), locations: tryParse(d.locations),
          minSalary: d.minSalary ?? '', maxSalary: d.maxSalary ?? '',
          remoteOk: d.remoteOk || false, industries: tryParse(d.industries),
          experienceLevels: tryParse(d.experienceLevels),
          skillsRequired: tryParse(d.skillsRequired),
        });
      }
    } catch (err) {
      toast.error('Failed to load your data. Please refresh.');
    }
  }, [toast]);

  useEffect(() => { loadData(); }, [loadData]);

  const processFile = async (file) => {
    if (!file) return;
    if (file.size > MAX_FILE_SIZE) {
      toast.error('File exceeds 10 MB limit.');
      return;
    }
    const ext = file.name.split('.').pop().toLowerCase();
    if (!['pdf', 'doc', 'docx'].includes(ext)) {
      toast.error('Unsupported format. Use PDF, DOC, or DOCX.');
      return;
    }
    setUploading(true);
    try {
      await resumesApi.uploadResume(file);
      const data = await resumesApi.getResumes();
      setResumes(Array.isArray(data) ? data : []);
      toast.success('Resume uploaded successfully!');
    } catch {
      toast.error('Upload failed. Please try again.');
    } finally {
      setUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  const handleFileUpload = (e) => processFile(e.target.files[0]);

  const handleDrop = (e) => {
    e.preventDefault();
    setDragOver(false);
    processFile(e.dataTransfer.files[0]);
  };

  const handleSetPrimary = async (id) => {
    try {
      await resumesApi.setPrimary(id);
      const data = await resumesApi.getResumes();
      setResumes(Array.isArray(data) ? data : []);
    } catch {
      toast.error('Failed to set primary resume.');
    }
  };

  const handleDeleteResume = async (id) => {
    try {
      await resumesApi.deleteResume(id);
      const data = await resumesApi.getResumes();
      setResumes(Array.isArray(data) ? data : []);
      setDeleteConfirmId(null);
      toast.success('Resume deleted.');
    } catch {
      toast.error('Failed to delete resume.');
    }
  };

  const addToList = (field) => {
    const val = newItem[field].trim();
    if (val && !preferences[field].includes(val)) {
      setPreferences((prev) => ({ ...prev, [field]: [...prev[field], val] }));
      setNewItem((prev) => ({ ...prev, [field]: '' }));
    }
  };

  const removeFromList = (field, idx) => {
    setPreferences((prev) => ({
      ...prev,
      [field]: prev[field].filter((_, i) => i !== idx),
    }));
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      await usersApi.updateProfile(profile);
      await usersApi.updatePreferences(preferences);
      toast.success('Setup complete!');
      navigate('/dashboard');
    } catch {
      toast.error('Failed to save. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  /* ---- Reusable sub-components ---- */

  const StepIndicator = () => (
    <div className="mb-10 animate-fade-in">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Setup Your Profile</h1>
      <div className="flex items-center justify-center">
        {STEP_LABELS.map((label, i) => {
          const s = i + 1;
          const isActive = step === s;
          const isCompleted = step > s;
          return (
            <div key={s} className="flex items-center">
              <div className="flex flex-col items-center">
                <div
                  className={`w-10 h-10 rounded-full flex items-center justify-center text-sm font-semibold transition-all duration-300 ${
                    isCompleted
                      ? 'bg-green-500 text-white'
                      : isActive
                        ? 'bg-brand-600 text-white shadow-md shadow-brand-200'
                        : 'bg-gray-200 text-gray-500'
                  }`}
                >
                  {isCompleted ? <CheckCircle2 className="h-5 w-5" /> : s}
                </div>
                <span
                  className={`mt-2 text-xs font-medium ${
                    isActive ? 'text-brand-700' : isCompleted ? 'text-green-600' : 'text-gray-400'
                  }`}
                >
                  {label}
                </span>
              </div>
              {s < 3 && (
                <div
                  className={`w-20 h-0.5 mx-2 mb-6 transition-colors duration-300 ${
                    isCompleted ? 'bg-green-500' : 'bg-gray-200'
                  }`}
                />
              )}
            </div>
          );
        })}
      </div>
    </div>
  );

  const ChipInput = ({ label, field, placeholder }) => (
    <div className="mb-5">
      <label className="block text-sm font-medium text-gray-700 mb-1.5">{label}</label>
      <div className="flex gap-2">
        <input
          type="text"
          value={newItem[field]}
          onChange={(e) => setNewItem((prev) => ({ ...prev, [field]: e.target.value }))}
          onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); addToList(field); } }}
          placeholder={placeholder}
          className="input-field flex-1"
        />
        <button type="button" onClick={() => addToList(field)} className="btn-primary px-3">
          Add
        </button>
      </div>
      {preferences[field].length > 0 && (
        <div className="flex flex-wrap gap-2 mt-2">
          {preferences[field].map((item, i) => (
            <span
              key={i}
              className="inline-flex items-center gap-1 rounded-full bg-brand-50 text-brand-700 border border-brand-200 px-3 py-1 text-sm font-medium"
            >
              {item}
              <button
                type="button"
                onClick={() => removeFromList(field, i)}
                className="text-brand-400 hover:text-brand-700 transition-colors"
              >
                <X className="h-3.5 w-3.5" />
              </button>
            </span>
          ))}
        </div>
      )}
    </div>
  );

  const parsedData = resumes.find((r) => r.parsedData)?.parsedData;

  return (
    <div className="max-w-3xl mx-auto animate-fade-in">
      <StepIndicator />

      {/* Step 1 – Resume Upload */}
      {step === 1 && (
        <div className="card p-6 animate-slide-up">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Upload Your Resume</h2>

          {/* Drag & drop zone */}
          <div
            onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
            onDragLeave={() => setDragOver(false)}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
            className={`border-2 border-dashed rounded-xl p-10 text-center cursor-pointer transition-colors ${
              dragOver
                ? 'border-brand-500 bg-brand-50'
                : 'border-gray-300 hover:border-brand-400 hover:bg-gray-50'
            }`}
          >
            {uploading ? (
              <Loader2 className="h-10 w-10 mx-auto text-brand-600 animate-spin" />
            ) : (
              <Upload className="h-10 w-10 mx-auto text-gray-400" />
            )}
            <p className="mt-3 text-sm font-medium text-gray-700">
              {uploading ? 'Uploading…' : 'Drag & drop your resume here'}
            </p>
            <p className="mt-1 text-xs text-gray-500">or click to browse</p>
            <p className="mt-2 text-xs text-gray-400">Supported: PDF, DOC, DOCX (max 10 MB)</p>
            <input
              ref={fileInputRef}
              type="file"
              accept=".pdf,.doc,.docx"
              onChange={handleFileUpload}
              className="hidden"
            />
          </div>

          {uploading && (
            <div className="mt-4">
              <ProgressBar value={70} size="sm" color="bg-brand-600" />
            </div>
          )}

          {/* Resume list */}
          {resumes.length > 0 && (
            <div className="mt-6 space-y-3">
              <h3 className="text-sm font-semibold text-gray-700">Your Resumes</h3>
              {resumes.map((r) => (
                <div
                  key={r.id}
                  className="flex items-center justify-between p-3 rounded-lg bg-gray-50 border border-gray-100"
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <FileText className="h-5 w-5 text-gray-400 shrink-0" />
                    <div className="min-w-0">
                      <p className="text-sm font-medium text-gray-900 truncate">{r.originalFilename}</p>
                      <p className="text-xs text-gray-500">
                        {r.fileSize ? formatFileSize(r.fileSize) : ''}
                        {r.uploadedAt && ` · ${new Date(r.uploadedAt).toLocaleDateString()}`}
                      </p>
                    </div>
                    {r.isPrimary && <Badge variant="success">Primary</Badge>}
                  </div>
                  <div className="flex items-center gap-1 shrink-0">
                    {!r.isPrimary && (
                      <button
                        onClick={() => handleSetPrimary(r.id)}
                        title="Set as primary"
                        className="btn-ghost p-2"
                      >
                        <Star className="h-4 w-4" />
                      </button>
                    )}
                    {deleteConfirmId === r.id ? (
                      <div className="flex items-center gap-1">
                        <button onClick={() => handleDeleteResume(r.id)} className="btn-danger py-1 px-2 text-xs">
                          Confirm
                        </button>
                        <button onClick={() => setDeleteConfirmId(null)} className="btn-ghost py-1 px-2 text-xs">
                          Cancel
                        </button>
                      </div>
                    ) : (
                      <button
                        onClick={() => setDeleteConfirmId(r.id)}
                        title="Delete resume"
                        className="btn-ghost p-2 text-red-500 hover:text-red-700"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* Parsed data preview */}
          {parsedData && (
            <div className="mt-6 space-y-4 p-4 rounded-lg bg-brand-50/50 border border-brand-100">
              <h3 className="text-sm font-semibold text-gray-800">Parsed Resume Data</h3>
              {parsedData.skills?.length > 0 && (
                <div>
                  <p className="text-xs font-medium text-gray-500 mb-1.5">Skills</p>
                  <div className="flex flex-wrap gap-1.5">
                    {parsedData.skills.map((s, i) => (
                      <Badge key={i} variant="brand">{s}</Badge>
                    ))}
                  </div>
                </div>
              )}
              {parsedData.experiences?.length > 0 && (
                <div>
                  <p className="text-xs font-medium text-gray-500 mb-1.5">Experience</p>
                  {parsedData.experiences.map((exp, i) => (
                    <div key={i} className="flex items-start gap-2 mb-2">
                      <Briefcase className="h-4 w-4 text-gray-400 mt-0.5 shrink-0" />
                      <div>
                        <p className="text-sm font-medium text-gray-800">{exp.title}{exp.company && ` at ${exp.company}`}</p>
                        {(exp.startDate || exp.endDate) && (
                          <p className="text-xs text-gray-500">{exp.startDate} – {exp.endDate || 'Present'}</p>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
              {parsedData.educations?.length > 0 && (
                <div>
                  <p className="text-xs font-medium text-gray-500 mb-1.5">Education</p>
                  {parsedData.educations.map((edu, i) => (
                    <div key={i} className="flex items-start gap-2 mb-2">
                      <GraduationCap className="h-4 w-4 text-gray-400 mt-0.5 shrink-0" />
                      <div>
                        <p className="text-sm font-medium text-gray-800">{edu.degree}{edu.institution && ` – ${edu.institution}`}</p>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          <div className="mt-6 flex justify-end">
            <button
              onClick={() => setStep(2)}
              disabled={resumes.length === 0}
              className="btn-primary"
            >
              Next <ChevronRight className="h-4 w-4" />
            </button>
          </div>
        </div>
      )}

      {/* Step 2 – Profile & Preferences */}
      {step === 2 && (
        <div className="card p-6 animate-slide-up">
          <h2 className="text-lg font-semibold text-gray-900 mb-5">Profile</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-8">
            {[
              { label: 'Full Name', key: 'fullName', type: 'text' },
              { label: 'Phone', key: 'phone', type: 'text' },
              { label: 'Location', key: 'location', type: 'text' },
              { label: 'Experience (years)', key: 'experienceYears', type: 'number' },
              { label: 'LinkedIn URL', key: 'linkedinUrl', type: 'url' },
              { label: 'GitHub URL', key: 'githubUrl', type: 'url' },
              { label: 'Portfolio URL', key: 'portfolioUrl', type: 'url', colSpan: true },
            ].map(({ label, key, type, colSpan }) => (
              <div key={key} className={colSpan ? 'sm:col-span-2' : ''}>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">{label}</label>
                <input
                  type={type}
                  value={profile[key]}
                  onChange={(e) =>
                    setProfile((p) => ({
                      ...p,
                      [key]: type === 'number' ? parseInt(e.target.value) || 0 : e.target.value,
                    }))
                  }
                  className="input-field"
                />
              </div>
            ))}
          </div>

          <h2 className="text-lg font-semibold text-gray-900 mb-5">Preferences</h2>
          <ChipInput label="Job Titles" field="jobTitles" placeholder="e.g., Software Engineer" />
          <ChipInput label="Preferred Locations" field="locations" placeholder="e.g., San Francisco" />
          <ChipInput label="Industries" field="industries" placeholder="e.g., FinTech" />
          <ChipInput label="Key Skills" field="skillsRequired" placeholder="e.g., React" />

          <div className="grid grid-cols-2 gap-4 mb-5">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Min Salary</label>
              <input
                type="number"
                value={preferences.minSalary}
                onChange={(e) => setPreferences((p) => ({ ...p, minSalary: e.target.value }))}
                placeholder="e.g., 80000"
                className="input-field"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Max Salary</label>
              <input
                type="number"
                value={preferences.maxSalary}
                onChange={(e) => setPreferences((p) => ({ ...p, maxSalary: e.target.value }))}
                placeholder="e.g., 150000"
                className="input-field"
              />
            </div>
          </div>

          <label className="flex items-center gap-3 mb-6 cursor-pointer select-none">
            <input
              type="checkbox"
              checked={preferences.remoteOk}
              onChange={(e) => setPreferences((p) => ({ ...p, remoteOk: e.target.checked }))}
              className="h-4 w-4 rounded border-gray-300 text-brand-600 focus:ring-brand-500"
            />
            <span className="text-sm font-medium text-gray-700">Open to remote work</span>
          </label>

          <div className="flex justify-between">
            <button onClick={() => setStep(1)} className="btn-secondary">
              <ChevronLeft className="h-4 w-4" /> Back
            </button>
            <button onClick={() => setStep(3)} className="btn-primary">
              Next <ChevronRight className="h-4 w-4" />
            </button>
          </div>
        </div>
      )}

      {/* Step 3 – Review & Confirm */}
      {step === 3 && (
        <div className="card p-6 animate-slide-up">
          <h2 className="text-lg font-semibold text-gray-900 mb-5">Review &amp; Confirm</h2>

          <div className="space-y-5">
            {/* Resumes */}
            <div className="rounded-lg bg-gray-50 p-4">
              <h3 className="text-sm font-semibold text-gray-800 flex items-center gap-2 mb-2">
                <FileText className="h-4 w-4 text-gray-500" /> Resumes
              </h3>
              <p className="text-sm text-gray-600">{resumes.length} resume(s) uploaded</p>
              <ul className="mt-1 text-sm text-gray-600 list-disc list-inside">
                {resumes.map((r) => (
                  <li key={r.id}>{r.originalFilename}{r.isPrimary ? ' (Primary)' : ''}</li>
                ))}
              </ul>
            </div>

            {/* Profile */}
            <div className="rounded-lg bg-gray-50 p-4">
              <h3 className="text-sm font-semibold text-gray-800 mb-2">Profile</h3>
              <dl className="grid grid-cols-2 gap-x-4 gap-y-1 text-sm">
                {[
                  ['Name', profile.fullName],
                  ['Phone', profile.phone],
                  ['Location', profile.location],
                  ['Experience', `${profile.experienceYears} years`],
                  ['LinkedIn', profile.linkedinUrl],
                  ['GitHub', profile.githubUrl],
                  ['Portfolio', profile.portfolioUrl],
                ].map(([label, value]) => (
                  <div key={label} className="contents">
                    <dt className="text-gray-500">{label}</dt>
                    <dd className="text-gray-800 truncate">{value || '—'}</dd>
                  </div>
                ))}
              </dl>
            </div>

            {/* Preferences */}
            <div className="rounded-lg bg-gray-50 p-4">
              <h3 className="text-sm font-semibold text-gray-800 mb-3">Preferences</h3>
              {[
                ['Job Titles', preferences.jobTitles],
                ['Locations', preferences.locations],
                ['Industries', preferences.industries],
                ['Skills', preferences.skillsRequired],
              ].map(([label, items]) => (
                <div key={label} className="mb-2">
                  <p className="text-xs font-medium text-gray-500 mb-1">{label}</p>
                  <div className="flex flex-wrap gap-1.5">
                    {items.length > 0
                      ? items.map((item, i) => <Badge key={i} variant="brand">{item}</Badge>)
                      : <span className="text-xs text-gray-400">None</span>}
                  </div>
                </div>
              ))}
              <p className="text-sm text-gray-600 mt-2">
                Salary: {preferences.minSalary || '—'} – {preferences.maxSalary || '—'}
                {' · '}Remote: {preferences.remoteOk ? 'Yes' : 'No'}
              </p>
            </div>
          </div>

          <div className="flex justify-between mt-6">
            <button onClick={() => setStep(2)} className="btn-secondary">
              <ChevronLeft className="h-4 w-4" /> Back
            </button>
            <button onClick={handleSave} disabled={saving} className="btn-primary">
              {saving ? <Loader2 className="h-4 w-4 animate-spin" /> : <CheckCircle2 className="h-4 w-4" />}
              {saving ? 'Saving…' : 'Complete Setup'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
