import { useState, useEffect, useCallback } from 'react';
import * as notificationsApi from '../api/notifications';

export default function SettingsPage() {
  const [prefs, setPrefs] = useState({ emailEnabled: true, dailyDigest: true, matchThreshold: 70, newJobAlerts: true, applicationUpdates: true, digestTime: '07:00' });
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);

  const loadPrefs = useCallback(async () => {
    try {
      const data = await notificationsApi.getNotificationPreferences();
      setPrefs({
        emailEnabled: data.emailEnabled ?? true,
        dailyDigest: data.dailyDigest ?? true,
        matchThreshold: data.matchThreshold ?? 70,
        newJobAlerts: data.newJobAlerts ?? true,
        applicationUpdates: data.applicationUpdates ?? true,
        digestTime: data.digestTime || '07:00',
      });
    } catch { /* use defaults */ }
  }, []);

  useEffect(() => { loadPrefs(); }, [loadPrefs]);

  const handleSave = async () => {
    setSaving(true);
    try {
      await notificationsApi.updateNotificationPreferences(prefs);
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    } catch { /* ignore */ }
    setSaving(false);
  };

  return (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Settings</h1>

      <div className="bg-white rounded-xl shadow-sm border p-6">
        <h2 className="text-lg font-semibold mb-4">🔔 Notification Preferences</h2>

        <div className="space-y-4">
          <label className="flex items-center justify-between">
            <span className="text-sm text-gray-700">Email Notifications</span>
            <input type="checkbox" checked={prefs.emailEnabled} onChange={(e) => setPrefs(p => ({ ...p, emailEnabled: e.target.checked }))}
              className="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500" />
          </label>
          <label className="flex items-center justify-between">
            <span className="text-sm text-gray-700">Daily Digest</span>
            <input type="checkbox" checked={prefs.dailyDigest} onChange={(e) => setPrefs(p => ({ ...p, dailyDigest: e.target.checked }))}
              className="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500" />
          </label>
          <label className="flex items-center justify-between">
            <span className="text-sm text-gray-700">New Job Alerts</span>
            <input type="checkbox" checked={prefs.newJobAlerts} onChange={(e) => setPrefs(p => ({ ...p, newJobAlerts: e.target.checked }))}
              className="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500" />
          </label>
          <label className="flex items-center justify-between">
            <span className="text-sm text-gray-700">Application Updates</span>
            <input type="checkbox" checked={prefs.applicationUpdates} onChange={(e) => setPrefs(p => ({ ...p, applicationUpdates: e.target.checked }))}
              className="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500" />
          </label>
          <div className="flex items-center justify-between">
            <span className="text-sm text-gray-700">Match Score Threshold</span>
            <input type="number" min="0" max="100" value={prefs.matchThreshold}
              onChange={(e) => setPrefs(p => ({ ...p, matchThreshold: parseInt(e.target.value) || 70 }))}
              className="w-20 border rounded-lg px-2 py-1 text-sm text-center" />
          </div>
          <div className="flex items-center justify-between">
            <span className="text-sm text-gray-700">Digest Time</span>
            <input type="time" value={prefs.digestTime}
              onChange={(e) => setPrefs(p => ({ ...p, digestTime: e.target.value }))}
              className="border rounded-lg px-2 py-1 text-sm" />
          </div>
        </div>

        <div className="mt-6 flex items-center space-x-3">
          <button onClick={handleSave} disabled={saving}
            className="px-6 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50">
            {saving ? '⏳ Saving...' : 'Save Preferences'}
          </button>
          {saved && <span className="text-green-600 text-sm">✓ Saved successfully!</span>}
        </div>
      </div>
    </div>
  );
}
