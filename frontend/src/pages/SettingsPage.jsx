import { useState, useEffect, useCallback } from 'react';
import { Settings, Bell, Shield, Loader2, AlertTriangle, Save, Trash2 } from 'lucide-react';
import * as notificationsApi from '../api/notifications';
import * as usersApi from '../api/users';
import { Modal } from '../components/ui/Modal';
import { useToast } from '../components/ui/Toast';

function Toggle({ label, description, checked, onChange }) {
  return (
    <label className="flex items-center justify-between py-3 cursor-pointer select-none">
      <div>
        <p className="text-sm font-medium text-gray-700">{label}</p>
        {description && <p className="text-xs text-gray-500">{description}</p>}
      </div>
      <div
        className={`relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition-colors ${
          checked ? 'bg-brand-600' : 'bg-gray-300'
        }`}
      >
        <input
          type="checkbox"
          checked={checked}
          onChange={onChange}
          className="sr-only"
        />
        <span
          className={`inline-block h-4 w-4 rounded-full bg-white shadow transition-transform ${
            checked ? 'translate-x-6' : 'translate-x-1'
          }`}
        />
      </div>
    </label>
  );
}

export default function SettingsPage() {
  const toast = useToast();

  // Notification preferences
  const [prefs, setPrefs] = useState({
    emailEnabled: true, dailyDigest: true, matchThreshold: 70,
    newJobAlerts: true, applicationUpdates: true, digestTime: '07:00',
  });
  const [savingPrefs, setSavingPrefs] = useState(false);

  // Account
  const [email, setEmail] = useState('');
  const [passwords, setPasswords] = useState({ current: '', newPw: '', confirm: '' });
  const [savingPassword, setSavingPassword] = useState(false);

  // Delete account modal
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteConfirmText, setDeleteConfirmText] = useState('');

  const loadData = useCallback(async () => {
    try {
      const [prefsData, profileData] = await Promise.allSettled([
        notificationsApi.getNotificationPreferences(),
        usersApi.getProfile(),
      ]);
      if (prefsData.status === 'fulfilled') {
        const d = prefsData.value;
        setPrefs({
          emailEnabled: d.emailEnabled ?? true,
          dailyDigest: d.dailyDigest ?? true,
          matchThreshold: d.matchThreshold ?? 70,
          newJobAlerts: d.newJobAlerts ?? true,
          applicationUpdates: d.applicationUpdates ?? true,
          digestTime: d.digestTime || '07:00',
        });
      }
      if (profileData.status === 'fulfilled') {
        setEmail(profileData.value.email || '');
      }
    } catch {
      toast.error('Failed to load settings.');
    }
  }, [toast]);

  useEffect(() => { loadData(); }, [loadData]);

  const handleSavePrefs = async () => {
    setSavingPrefs(true);
    try {
      await notificationsApi.updateNotificationPreferences(prefs);
      toast.success('Notification preferences saved.');
    } catch {
      toast.error('Failed to save preferences.');
    } finally {
      setSavingPrefs(false);
    }
  };

  const handleSavePassword = async () => {
    if (passwords.newPw !== passwords.confirm) {
      toast.error('New passwords do not match.');
      return;
    }
    setSavingPassword(true);
    try {
      await usersApi.updateProfile({
        currentPassword: passwords.current,
        newPassword: passwords.newPw,
      });
      setPasswords({ current: '', newPw: '', confirm: '' });
      toast.success('Password updated.');
    } catch {
      toast.error('Failed to update password.');
    } finally {
      setSavingPassword(false);
    }
  };

  const handleDeleteAccount = async () => {
    try {
      await usersApi.updateProfile({ deleted: true });
      toast.success('Account deleted.');
    } catch {
      toast.error('Failed to delete account.');
    } finally {
      setShowDeleteModal(false);
      setDeleteConfirmText('');
    }
  };

  const passwordsEmpty =
    !passwords.current || !passwords.newPw || !passwords.confirm;

  return (
    <div className="max-w-2xl mx-auto animate-fade-in">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center gap-3">
          <Settings className="h-6 w-6 text-brand-600" />
          <h1 className="text-2xl font-bold text-gray-900">Settings</h1>
        </div>
        <p className="mt-1 text-sm text-gray-500">Manage your account and preferences</p>
      </div>

      {/* Section 1 – Notification Preferences */}
      <section className="card p-6 mb-6 animate-slide-up">
        <div className="flex items-center gap-2 mb-4">
          <Bell className="h-5 w-5 text-brand-600" />
          <h2 className="text-lg font-semibold text-gray-900">Notification Preferences</h2>
        </div>

        <div className="divide-y divide-gray-100">
          <Toggle
            label="Email Notifications"
            description="Receive emails for important updates"
            checked={prefs.emailEnabled}
            onChange={(e) => setPrefs((p) => ({ ...p, emailEnabled: e.target.checked }))}
          />
          <Toggle
            label="Daily Digest"
            description="A summary of new opportunities each day"
            checked={prefs.dailyDigest}
            onChange={(e) => setPrefs((p) => ({ ...p, dailyDigest: e.target.checked }))}
          />
          <Toggle
            label="New Job Alerts"
            description="Instant alerts when new matches appear"
            checked={prefs.newJobAlerts}
            onChange={(e) => setPrefs((p) => ({ ...p, newJobAlerts: e.target.checked }))}
          />
          <Toggle
            label="Application Updates"
            description="Status changes on your applications"
            checked={prefs.applicationUpdates}
            onChange={(e) => setPrefs((p) => ({ ...p, applicationUpdates: e.target.checked }))}
          />
        </div>

        {/* Match threshold slider */}
        <div className="mt-5">
          <div className="flex items-center justify-between mb-2">
            <label className="text-sm font-medium text-gray-700">Match Score Threshold</label>
            <span className="text-sm font-semibold text-brand-600">{prefs.matchThreshold}%</span>
          </div>
          <input
            type="range"
            min="0"
            max="100"
            value={prefs.matchThreshold}
            onChange={(e) => setPrefs((p) => ({ ...p, matchThreshold: parseInt(e.target.value) }))}
            className="w-full h-2 rounded-full appearance-none bg-gray-200 accent-brand-600"
          />
          <div className="flex justify-between text-xs text-gray-400 mt-1">
            <span>0</span><span>50</span><span>100</span>
          </div>
        </div>

        {/* Digest time */}
        <div className="mt-5">
          <label className="block text-sm font-medium text-gray-700 mb-1.5">Daily Digest Time</label>
          <input
            type="time"
            value={prefs.digestTime}
            onChange={(e) => setPrefs((p) => ({ ...p, digestTime: e.target.value }))}
            className="input-field w-40"
          />
        </div>

        <div className="mt-6">
          <button onClick={handleSavePrefs} disabled={savingPrefs} className="btn-primary">
            {savingPrefs ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
            {savingPrefs ? 'Saving…' : 'Save Preferences'}
          </button>
        </div>
      </section>

      {/* Section 2 – Account */}
      <section className="card p-6 animate-slide-up">
        <div className="flex items-center gap-2 mb-4">
          <Shield className="h-5 w-5 text-brand-600" />
          <h2 className="text-lg font-semibold text-gray-900">Account</h2>
        </div>

        {/* Email */}
        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-1.5">Email</label>
          <input type="email" value={email} readOnly className="input-field bg-gray-50 cursor-not-allowed" />
        </div>

        {/* Change Password */}
        <div className="mb-8">
          <h3 className="text-sm font-semibold text-gray-800 mb-3">Change Password</h3>
          <div className="space-y-3">
            <input
              type="password"
              placeholder="Current password"
              value={passwords.current}
              onChange={(e) => setPasswords((p) => ({ ...p, current: e.target.value }))}
              className="input-field"
            />
            <input
              type="password"
              placeholder="New password"
              value={passwords.newPw}
              onChange={(e) => setPasswords((p) => ({ ...p, newPw: e.target.value }))}
              className="input-field"
            />
            <input
              type="password"
              placeholder="Confirm new password"
              value={passwords.confirm}
              onChange={(e) => setPasswords((p) => ({ ...p, confirm: e.target.value }))}
              className="input-field"
            />
          </div>
          <button
            onClick={handleSavePassword}
            disabled={passwordsEmpty || savingPassword}
            className="btn-primary mt-4"
          >
            {savingPassword ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
            {savingPassword ? 'Saving…' : 'Save Password'}
          </button>
        </div>

        {/* Danger Zone */}
        <div className="rounded-lg border border-red-200 bg-red-50/50 p-4">
          <div className="flex items-center gap-2 mb-2">
            <AlertTriangle className="h-4 w-4 text-red-500" />
            <h3 className="text-sm font-semibold text-red-700">Danger Zone</h3>
          </div>
          <p className="text-sm text-red-600 mb-3">
            Permanently delete your account and all associated data.
          </p>
          <button onClick={() => setShowDeleteModal(true)} className="btn-danger">
            <Trash2 className="h-4 w-4" /> Delete Account
          </button>
        </div>
      </section>

      {/* Delete confirmation modal */}
      <Modal isOpen={showDeleteModal} onClose={() => { setShowDeleteModal(false); setDeleteConfirmText(''); }} title="Delete Account">
        <div className="space-y-4">
          <p className="text-sm text-gray-600">
            This will permanently delete your account and all data. This action cannot be undone.
          </p>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">
              Type <span className="font-bold text-red-600">DELETE</span> to confirm
            </label>
            <input
              type="text"
              value={deleteConfirmText}
              onChange={(e) => setDeleteConfirmText(e.target.value)}
              className="input-field"
              placeholder="DELETE"
            />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button onClick={() => { setShowDeleteModal(false); setDeleteConfirmText(''); }} className="btn-secondary">
              Cancel
            </button>
            <button
              onClick={handleDeleteAccount}
              disabled={deleteConfirmText !== 'DELETE'}
              className="btn-danger"
            >
              <Trash2 className="h-4 w-4" /> Delete Account
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
