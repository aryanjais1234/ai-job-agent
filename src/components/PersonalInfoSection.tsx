"use client";

import { PersonalInfo } from "@/types/profile";

interface PersonalInfoSectionProps {
  personalInfo: PersonalInfo;
  isEditing: boolean;
  onChange: (info: PersonalInfo) => void;
}

export default function PersonalInfoSection({
  personalInfo,
  isEditing,
  onChange,
}: PersonalInfoSectionProps) {
  const handleChange = (
    field: keyof PersonalInfo,
    value: string
  ) => {
    onChange({ ...personalInfo, [field]: value });
  };

  return (
    <section className="rounded-xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
      <h2 className="mb-4 text-xl font-semibold text-zinc-900 dark:text-zinc-100">
        Personal Information
      </h2>
      <div className="grid gap-4 sm:grid-cols-2">
        <div>
          <label className="mb-1 block text-sm font-medium text-zinc-600 dark:text-zinc-400">
            First Name
          </label>
          {isEditing ? (
            <input
              type="text"
              value={personalInfo.firstName}
              onChange={(e) => handleChange("firstName", e.target.value)}
              className="w-full rounded-lg border border-zinc-300 px-3 py-2 text-sm text-zinc-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100"
            />
          ) : (
            <p className="py-2 text-sm text-zinc-900 dark:text-zinc-100">
              {personalInfo.firstName || "—"}
            </p>
          )}
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-zinc-600 dark:text-zinc-400">
            Last Name
          </label>
          {isEditing ? (
            <input
              type="text"
              value={personalInfo.lastName}
              onChange={(e) => handleChange("lastName", e.target.value)}
              className="w-full rounded-lg border border-zinc-300 px-3 py-2 text-sm text-zinc-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100"
            />
          ) : (
            <p className="py-2 text-sm text-zinc-900 dark:text-zinc-100">
              {personalInfo.lastName || "—"}
            </p>
          )}
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-zinc-600 dark:text-zinc-400">
            Email
          </label>
          {isEditing ? (
            <input
              type="email"
              value={personalInfo.email}
              onChange={(e) => handleChange("email", e.target.value)}
              className="w-full rounded-lg border border-zinc-300 px-3 py-2 text-sm text-zinc-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100"
            />
          ) : (
            <p className="py-2 text-sm text-zinc-900 dark:text-zinc-100">
              {personalInfo.email || "—"}
            </p>
          )}
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-zinc-600 dark:text-zinc-400">
            Phone
          </label>
          {isEditing ? (
            <input
              type="tel"
              value={personalInfo.phone}
              onChange={(e) => handleChange("phone", e.target.value)}
              className="w-full rounded-lg border border-zinc-300 px-3 py-2 text-sm text-zinc-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100"
            />
          ) : (
            <p className="py-2 text-sm text-zinc-900 dark:text-zinc-100">
              {personalInfo.phone || "—"}
            </p>
          )}
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-zinc-600 dark:text-zinc-400">
            Location
          </label>
          {isEditing ? (
            <input
              type="text"
              value={personalInfo.location}
              onChange={(e) => handleChange("location", e.target.value)}
              placeholder="e.g. San Francisco, CA"
              className="w-full rounded-lg border border-zinc-300 px-3 py-2 text-sm text-zinc-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100"
            />
          ) : (
            <p className="py-2 text-sm text-zinc-900 dark:text-zinc-100">
              {personalInfo.location || "—"}
            </p>
          )}
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-zinc-600 dark:text-zinc-400">
            LinkedIn
          </label>
          {isEditing ? (
            <input
              type="url"
              value={personalInfo.linkedIn}
              onChange={(e) => handleChange("linkedIn", e.target.value)}
              placeholder="https://linkedin.com/in/..."
              className="w-full rounded-lg border border-zinc-300 px-3 py-2 text-sm text-zinc-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100"
            />
          ) : (
            <p className="py-2 text-sm text-zinc-900 dark:text-zinc-100">
              {personalInfo.linkedIn ? (
                <a
                  href={personalInfo.linkedIn}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-blue-600 hover:underline dark:text-blue-400"
                >
                  {personalInfo.linkedIn}
                </a>
              ) : (
                "—"
              )}
            </p>
          )}
        </div>
        <div className="sm:col-span-2">
          <label className="mb-1 block text-sm font-medium text-zinc-600 dark:text-zinc-400">
            Professional Summary
          </label>
          {isEditing ? (
            <textarea
              value={personalInfo.summary}
              onChange={(e) => handleChange("summary", e.target.value)}
              rows={3}
              placeholder="A brief summary of your professional background..."
              className="w-full rounded-lg border border-zinc-300 px-3 py-2 text-sm text-zinc-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100"
            />
          ) : (
            <p className="py-2 text-sm text-zinc-900 dark:text-zinc-100">
              {personalInfo.summary || "—"}
            </p>
          )}
        </div>
      </div>
    </section>
  );
}
