"use client";

import { JobPreferences } from "@/types/profile";

interface JobPreferencesSectionProps {
  preferences: JobPreferences;
  isEditing: boolean;
  onChange: (preferences: JobPreferences) => void;
}

export default function JobPreferencesSection({
  preferences,
  isEditing,
  onChange,
}: JobPreferencesSectionProps) {
  const handleChange = (
    field: keyof JobPreferences,
    value: string
  ) => {
    onChange({ ...preferences, [field]: value });
  };

  const inputClass =
    "w-full rounded-lg border border-zinc-300 px-3 py-2 text-sm text-zinc-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100";

  return (
    <section className="rounded-xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
      <h2 className="mb-4 text-xl font-semibold text-zinc-900 dark:text-zinc-100">
        Job Preferences
      </h2>
      <div className="grid gap-4 sm:grid-cols-2">
        <div>
          <label className="mb-1 block text-sm font-medium text-zinc-600 dark:text-zinc-400">
            Desired Role
          </label>
          {isEditing ? (
            <input
              type="text"
              value={preferences.desiredRole}
              onChange={(e) => handleChange("desiredRole", e.target.value)}
              placeholder="e.g. Full Stack Developer"
              className={inputClass}
            />
          ) : (
            <p className="py-2 text-sm text-zinc-900 dark:text-zinc-100">
              {preferences.desiredRole || "—"}
            </p>
          )}
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-zinc-600 dark:text-zinc-400">
            Preferred Location
          </label>
          {isEditing ? (
            <input
              type="text"
              value={preferences.desiredLocation}
              onChange={(e) => handleChange("desiredLocation", e.target.value)}
              placeholder="e.g. New York, NY"
              className={inputClass}
            />
          ) : (
            <p className="py-2 text-sm text-zinc-900 dark:text-zinc-100">
              {preferences.desiredLocation || "—"}
            </p>
          )}
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-zinc-600 dark:text-zinc-400">
            Work Arrangement
          </label>
          {isEditing ? (
            <select
              value={preferences.remotePreference}
              onChange={(e) => handleChange("remotePreference", e.target.value)}
              className={inputClass}
            >
              <option value="">Select...</option>
              <option value="remote">Remote</option>
              <option value="hybrid">Hybrid</option>
              <option value="onsite">On-site</option>
            </select>
          ) : (
            <p className="py-2 text-sm capitalize text-zinc-900 dark:text-zinc-100">
              {preferences.remotePreference || "—"}
            </p>
          )}
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-zinc-600 dark:text-zinc-400">
            Job Type
          </label>
          {isEditing ? (
            <select
              value={preferences.jobType}
              onChange={(e) => handleChange("jobType", e.target.value)}
              className={inputClass}
            >
              <option value="">Select...</option>
              <option value="full-time">Full-time</option>
              <option value="part-time">Part-time</option>
              <option value="contract">Contract</option>
              <option value="internship">Internship</option>
            </select>
          ) : (
            <p className="py-2 text-sm capitalize text-zinc-900 dark:text-zinc-100">
              {preferences.jobType || "—"}
            </p>
          )}
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-zinc-600 dark:text-zinc-400">
            Minimum Salary (USD)
          </label>
          {isEditing ? (
            <input
              type="text"
              inputMode="numeric"
              pattern="[0-9]*"
              value={preferences.salaryMin}
              onChange={(e) => {
                const value = e.target.value.replace(/[^0-9]/g, "");
                handleChange("salaryMin", value);
              }}
              placeholder="e.g. 80000"
              className={inputClass}
            />
          ) : (
            <p className="py-2 text-sm text-zinc-900 dark:text-zinc-100">
              {preferences.salaryMin
                ? `$${Number(preferences.salaryMin).toLocaleString()}`
                : "—"}
            </p>
          )}
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-zinc-600 dark:text-zinc-400">
            Maximum Salary (USD)
          </label>
          {isEditing ? (
            <input
              type="text"
              inputMode="numeric"
              pattern="[0-9]*"
              value={preferences.salaryMax}
              onChange={(e) => {
                const value = e.target.value.replace(/[^0-9]/g, "");
                handleChange("salaryMax", value);
              }}
              placeholder="e.g. 120000"
              className={inputClass}
            />
          ) : (
            <p className="py-2 text-sm text-zinc-900 dark:text-zinc-100">
              {preferences.salaryMax
                ? `$${Number(preferences.salaryMax).toLocaleString()}`
                : "—"}
            </p>
          )}
        </div>
      </div>
    </section>
  );
}
