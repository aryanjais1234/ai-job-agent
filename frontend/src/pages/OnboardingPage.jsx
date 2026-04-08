import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import * as resumesApi from '../api/resumes';
import * as usersApi from '../api/users';

function tryParse(val) {
  if (Array.isArray(val)) return val;
  if (typeof val === 'string') {
    try { return JSON.parse(val); } catch { return []; }
  }
  return [];
}

export default function OnboardingPage() {
  const [step, setStep] = useState(1);
  const [resumes, setResumes] = useState([]);
  const [uploading, setUploading] = useState(false);
  const [profile, setProfile] = useState({ fullName: '', location: '', experienceYears: 0, phone: '', linkedinUrl: '' });
  const [preferences, setPreferences] = useState({
    jobTitles: [], locations: [], remoteOk: false, industries: [], skillsRequired: []
  });
  const [saving, setSaving] = useState(false);
  const [newItem, setNewItem] = useState({ jobTitles: '', locations: '', industries: '', skillsRequired: '' });
  const navigate = useNavigate();

  const loadData = useCallback(async () => {
    try {
      const [resumeData, profileData] = await Promise.allSettled([
        resumesApi.getResumes(),
        usersApi.getProfile(),
      ]);
      if (resumeData.status === 'fulfilled') setResumes(Array.isArray(resumeData.value) ? resumeData.value : []);
      if (profileData.status === 'fulfilled') {
        const p = profileData.value;
        setProfile({ fullName: p.fullName || '', location: p.location || '', experienceYears: p.experienceYears || 0,
          phone: p.phone || '', linkedinUrl: p.linkedinUrl || '' });
      }
      try {
        const prefData = await usersApi.getPreferences();
        if (prefData) {
          setPreferences({
            jobTitles: tryParse(prefData.jobTitles) || [],
            locations: tryParse(prefData.locations) || [],
            remoteOk: prefData.remoteOk || false,
            industries: tryParse(prefData.industries) || [],
            skillsRequired: tryParse(prefData.skillsRequired) || [],
          });
        }
      } catch { /* preferences may not exist yet */ }
    } catch { /* ignore */ }
  }, []);

  useEffect(() => { loadData(); }, [loadData]);

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setUploading(true);
    try {
      await resumesApi.uploadResume(file);
      const data = await resumesApi.getResumes();
      setResumes(Array.isArray(data) ? data : []);
    } catch { /* ignore */ }
    setUploading(false);
  };

  const handleSetPrimary = async (id) => {
    try {
      await resumesApi.setPrimary(id);
      const data = await resumesApi.getResumes();
      setResumes(Array.isArray(data) ? data : []);
    } catch { /* ignore */ }
  };

  const handleDeleteResume = async (id) => {
    try {
      await resumesApi.deleteResume(id);
      const data = await resumesApi.getResumes();
      setResumes(Array.isArray(data) ? data : []);
    } catch { /* ignore */ }
  };

  const addToList = (field) => {
    const val = newItem[field].trim();
    if (val && !preferences[field].includes(val)) {
      setPreferences(prev => ({ ...prev, [field]: [...prev[field], val] }));
      setNewItem(prev => ({ ...prev, [field]: '' }));
    }
  };

  const removeFromList = (field, idx) => {
    setPreferences(prev => ({ ...prev, [field]: prev[field].filter((_, i) => i !== idx) }));
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      await usersApi.updateProfile(profile);
      await usersApi.updatePreferences(preferences);
      navigate('/dashboard');
    } catch { /* ignore */ }
    setSaving(false);
  };

  const ListInput = ({ label, field, placeholder }) => (
    <div className="mb-4">
      <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
      <div className="flex space-x-2">
        <input type="text" value={newItem[field]} onChange={(e) => setNewItem(prev => ({ ...prev, [field]: e.target.value }))}
          onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); addToList(field); } }}
          placeholder={placeholder} className="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500" />
        <button type="button" onClick={() => addToList(field)} className="px-3 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700">Add</button>
      </div>
      <div className="flex flex-wrap gap-2 mt-2">
        {preferences[field].map((item, i) => (
          <span key={i} className="inline-flex items-center px-3 py-1 bg-indigo-50 text-indigo-700 text-sm rounded-full">
            {item}
            <button type="button" onClick={() => removeFromList(field, i)} className="ml-2 text-indigo-400 hover:text-indigo-600">×</button>
          </span>
        ))}
      </div>
    </div>
  );

  return (
    <div className="max-w-2xl mx-auto">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">Setup Your Profile</h1>
        <div className="flex items-center mt-4 space-x-2">
          {[1, 2, 3].map((s) => (
            <div key={s} className="flex items-center">
              <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium ${
                step >= s ? 'bg-indigo-600 text-white' : 'bg-gray-200 text-gray-500'}`}>{s}</div>
              {s < 3 && <div className={`w-16 h-1 mx-1 ${step > s ? 'bg-indigo-600' : 'bg-gray-200'}`} />}
            </div>
          ))}
        </div>
        <div className="flex mt-2 text-xs text-gray-500">
          <span className="w-8 text-center">Resume</span><span className="w-16" />
          <span className="w-8 text-center">Profile</span><span className="w-16" />
          <span className="w-8 text-center">Done</span>
        </div>
      </div>

      {step === 1 && (
        <div className="bg-white rounded-xl shadow-sm border p-6">
          <h2 className="text-lg font-semibold mb-4">Upload Your Resume</h2>
          <div className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center">
            <p className="text-3xl mb-2">📄</p>
            <p className="text-gray-600 mb-4">Drag and drop or click to upload (PDF, DOC, DOCX)</p>
            <input type="file" accept=".pdf,.doc,.docx" onChange={handleFileUpload} className="hidden" id="resume-upload" />
            <label htmlFor="resume-upload"
              className="inline-flex items-center px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 cursor-pointer">
              {uploading ? '⏳ Uploading...' : '📁 Choose File'}
            </label>
          </div>
          {resumes.length > 0 && (
            <div className="mt-4 space-y-2">
              <h3 className="text-sm font-medium text-gray-700">Your Resumes:</h3>
              {resumes.map((r) => (
                <div key={r.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <div>
                    <span className="text-sm font-medium">{r.originalFilename}</span>
                    {r.isPrimary && <span className="ml-2 text-xs text-green-600 bg-green-50 px-2 py-1 rounded-full">Primary</span>}
                  </div>
                  <div className="flex space-x-2">
                    {!r.isPrimary && (
                      <button onClick={() => handleSetPrimary(r.id)} className="text-xs text-indigo-600 hover:text-indigo-700">Set Primary</button>
                    )}
                    <button onClick={() => handleDeleteResume(r.id)} className="text-xs text-red-600 hover:text-red-700">Delete</button>
                  </div>
                </div>
              ))}
            </div>
          )}
          <div className="mt-6 flex justify-end">
            <button onClick={() => setStep(2)} className="px-6 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700">
              Next →
            </button>
          </div>
        </div>
      )}

      {step === 2 && (
        <div className="bg-white rounded-xl shadow-sm border p-6">
          <h2 className="text-lg font-semibold mb-4">Profile &amp; Preferences</h2>
          <div className="grid grid-cols-2 gap-4 mb-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Full Name</label>
              <input type="text" value={profile.fullName} onChange={(e) => setProfile(p => ({ ...p, fullName: e.target.value }))}
                className="w-full border rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Location</label>
              <input type="text" value={profile.location} onChange={(e) => setProfile(p => ({ ...p, location: e.target.value }))}
                className="w-full border rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Experience (years)</label>
              <input type="number" value={profile.experienceYears} onChange={(e) => setProfile(p => ({ ...p, experienceYears: parseInt(e.target.value) || 0 }))}
                className="w-full border rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Phone</label>
              <input type="text" value={profile.phone} onChange={(e) => setProfile(p => ({ ...p, phone: e.target.value }))}
                className="w-full border rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500" />
            </div>
          </div>
          <ListInput label="Desired Job Titles" field="jobTitles" placeholder="e.g., Software Engineer" />
          <ListInput label="Preferred Locations" field="locations" placeholder="e.g., Bangalore" />
          <ListInput label="Target Industries" field="industries" placeholder="e.g., FinTech" />
          <ListInput label="Key Skills" field="skillsRequired" placeholder="e.g., Java" />
          <div className="mb-4">
            <label className="flex items-center space-x-2">
              <input type="checkbox" checked={preferences.remoteOk} onChange={(e) => setPreferences(p => ({ ...p, remoteOk: e.target.checked }))}
                className="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500" />
              <span className="text-sm text-gray-700">Open to remote work</span>
            </label>
          </div>
          <div className="flex justify-between mt-6">
            <button onClick={() => setStep(1)} className="px-6 py-2 border rounded-lg hover:bg-gray-50">← Back</button>
            <button onClick={() => setStep(3)} className="px-6 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700">Next →</button>
          </div>
        </div>
      )}

      {step === 3 && (
        <div className="bg-white rounded-xl shadow-sm border p-6">
          <h2 className="text-lg font-semibold mb-4">Review &amp; Confirm</h2>
          <div className="space-y-4">
            <div className="p-4 bg-gray-50 rounded-lg">
              <h3 className="font-medium text-gray-800 mb-2">📄 Resumes</h3>
              <p className="text-sm text-gray-600">{resumes.length} resume(s) uploaded</p>
            </div>
            <div className="p-4 bg-gray-50 rounded-lg">
              <h3 className="font-medium text-gray-800 mb-2">👤 Profile</h3>
              <p className="text-sm text-gray-600">{profile.fullName} • {profile.location} • {profile.experienceYears} years exp</p>
            </div>
            <div className="p-4 bg-gray-50 rounded-lg">
              <h3 className="font-medium text-gray-800 mb-2">🎯 Preferences</h3>
              <p className="text-sm text-gray-600">
                Titles: {preferences.jobTitles.join(', ') || 'None'}<br />
                Locations: {preferences.locations.join(', ') || 'None'}<br />
                Industries: {preferences.industries.join(', ') || 'None'}<br />
                Skills: {preferences.skillsRequired.join(', ') || 'None'}<br />
                Remote: {preferences.remoteOk ? 'Yes' : 'No'}
              </p>
            </div>
          </div>
          <div className="flex justify-between mt-6">
            <button onClick={() => setStep(2)} className="px-6 py-2 border rounded-lg hover:bg-gray-50">← Back</button>
            <button onClick={handleSave} disabled={saving}
              className="px-6 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50">
              {saving ? '⏳ Saving...' : '✓ Complete Setup'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
