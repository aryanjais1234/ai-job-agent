"use client";

import { WorkExperience } from "@/types/profile";

interface ExperienceSectionProps {
  experience: WorkExperience[];
  isEditing: boolean;
  onChange: (experience: WorkExperience[]) => void;
}

function generateId(): string {
  return crypto.randomUUID();
}

export default function ExperienceSection({
  experience,
  isEditing,
  onChange,
}: ExperienceSectionProps) {
  const addExperience = () => {
    const newExp: WorkExperience = {
      id: generateId(),
      title: "",
      company: "",
      startDate: "",
      endDate: "",
      current: false,
      description: "",
    };
    onChange([...experience, newExp]);
  };

  const updateExperience = (
    id: string,
    field: keyof WorkExperience,
    value: string | boolean
  ) => {
    onChange(
      experience.map((exp) =>
        exp.id === id ? { ...exp, [field]: value } : exp
      )
    );
  };

  const removeExperience = (id: string) => {
    onChange(experience.filter((exp) => exp.id !== id));
  };

  return (
    <section className="rounded-xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-xl font-semibold text-zinc-900 dark:text-zinc-100">
          Work Experience
        </h2>
        {isEditing && (
          <button
            onClick={addExperience}
            className="rounded-lg bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          >
            + Add
          </button>
        )}
      </div>
      <div className="space-y-6">
        {experience.map((exp) => (
          <div
            key={exp.id}
            className="rounded-lg border border-zinc-100 bg-zinc-50 p-4 dark:border-zinc-800 dark:bg-zinc-800/50"
          >
            {isEditing ? (
              <div className="space-y-3">
                <div className="flex items-start justify-between">
                  <div className="grid flex-1 gap-3 sm:grid-cols-2">
                    <div>
                      <label className="mb-1 block text-xs font-medium text-zinc-500 dark:text-zinc-400">
                        Job Title
                      </label>
                      <input
                        type="text"
                        value={exp.title}
                        onChange={(e) =>
                          updateExperience(exp.id, "title", e.target.value)
                        }
                        placeholder="e.g. Software Engineer"
                        className="w-full rounded-lg border border-zinc-300 px-3 py-2 text-sm text-zinc-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100"
                      />
                    </div>
                    <div>
                      <label className="mb-1 block text-xs font-medium text-zinc-500 dark:text-zinc-400">
                        Company
                      </label>
                      <input
                        type="text"
                        value={exp.company}
                        onChange={(e) =>
                          updateExperience(exp.id, "company", e.target.value)
                        }
                        placeholder="e.g. Acme Corp"
                        className="w-full rounded-lg border border-zinc-300 px-3 py-2 text-sm text-zinc-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100"
                      />
                    </div>
                    <div>
                      <label className="mb-1 block text-xs font-medium text-zinc-500 dark:text-zinc-400">
                        Start Date
                      </label>
                      <input
                        type="month"
                        value={exp.startDate}
                        onChange={(e) =>
                          updateExperience(exp.id, "startDate", e.target.value)
                        }
                        className="w-full rounded-lg border border-zinc-300 px-3 py-2 text-sm text-zinc-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100"
                      />
                    </div>
                    <div>
                      <label className="mb-1 block text-xs font-medium text-zinc-500 dark:text-zinc-400">
                        End Date
                      </label>
                      <input
                        type="month"
                        value={exp.endDate}
                        onChange={(e) =>
                          updateExperience(exp.id, "endDate", e.target.value)
                        }
                        disabled={exp.current}
                        className="w-full rounded-lg border border-zinc-300 px-3 py-2 text-sm text-zinc-900 disabled:opacity-50 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100"
                      />
                      <label className="mt-1 flex items-center gap-2 text-xs text-zinc-500 dark:text-zinc-400">
                        <input
                          type="checkbox"
                          checked={exp.current}
                          onChange={(e) =>
                            updateExperience(
                              exp.id,
                              "current",
                              e.target.checked
                            )
                          }
                          className="rounded border-zinc-300"
                        />
                        Currently working here
                      </label>
                    </div>
                  </div>
                  <button
                    onClick={() => removeExperience(exp.id)}
                    className="ml-3 text-zinc-400 hover:text-red-500"
                    aria-label="Remove experience"
                  >
                    <svg
                      className="h-5 w-5"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                      />
                    </svg>
                  </button>
                </div>
                <div>
                  <label className="mb-1 block text-xs font-medium text-zinc-500 dark:text-zinc-400">
                    Description
                  </label>
                  <textarea
                    value={exp.description}
                    onChange={(e) =>
                      updateExperience(exp.id, "description", e.target.value)
                    }
                    rows={2}
                    placeholder="Describe your responsibilities and achievements..."
                    className="w-full rounded-lg border border-zinc-300 px-3 py-2 text-sm text-zinc-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100"
                  />
                </div>
              </div>
            ) : (
              <div>
                <div className="flex items-start justify-between">
                  <div>
                    <h3 className="font-medium text-zinc-900 dark:text-zinc-100">
                      {exp.title || "Untitled Role"}
                    </h3>
                    <p className="text-sm text-zinc-600 dark:text-zinc-400">
                      {exp.company || "Company not specified"}
                    </p>
                  </div>
                  <span className="text-xs text-zinc-500 dark:text-zinc-400">
                    {exp.startDate || "?"} –{" "}
                    {exp.current ? "Present" : exp.endDate || "?"}
                  </span>
                </div>
                {exp.description && (
                  <p className="mt-2 text-sm text-zinc-700 dark:text-zinc-300">
                    {exp.description}
                  </p>
                )}
              </div>
            )}
          </div>
        ))}
        {experience.length === 0 && (
          <p className="text-sm text-zinc-500 dark:text-zinc-400">
            No work experience added yet.
          </p>
        )}
      </div>
    </section>
  );
}
