"use client";

import { useState } from "react";

interface SkillsSectionProps {
  skills: string[];
  isEditing: boolean;
  onChange: (skills: string[]) => void;
}

export default function SkillsSection({
  skills,
  isEditing,
  onChange,
}: SkillsSectionProps) {
  const [newSkill, setNewSkill] = useState("");

  const addSkill = () => {
    const trimmed = newSkill.trim();
    if (trimmed && !skills.includes(trimmed)) {
      onChange([...skills, trimmed]);
      setNewSkill("");
    }
  };

  const removeSkill = (skillToRemove: string) => {
    onChange(skills.filter((s) => s !== skillToRemove));
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      e.preventDefault();
      addSkill();
    }
  };

  return (
    <section className="rounded-xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
      <h2 className="mb-4 text-xl font-semibold text-zinc-900 dark:text-zinc-100">
        Skills
      </h2>
      <div className="flex flex-wrap gap-2">
        {skills.map((skill) => (
          <span
            key={skill}
            className="inline-flex items-center gap-1 rounded-full bg-blue-50 px-3 py-1 text-sm font-medium text-blue-700 dark:bg-blue-900/30 dark:text-blue-300"
          >
            {skill}
            {isEditing && (
              <button
                onClick={() => removeSkill(skill)}
                className="ml-1 text-blue-400 hover:text-blue-600 dark:hover:text-blue-200"
                aria-label={`Remove ${skill}`}
              >
                ×
              </button>
            )}
          </span>
        ))}
        {skills.length === 0 && !isEditing && (
          <p className="text-sm text-zinc-500 dark:text-zinc-400">
            No skills added yet.
          </p>
        )}
      </div>
      {isEditing && (
        <div className="mt-4 flex gap-2">
          <input
            type="text"
            value={newSkill}
            onChange={(e) => setNewSkill(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Add a skill..."
            className="flex-1 rounded-lg border border-zinc-300 px-3 py-2 text-sm text-zinc-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100"
          />
          <button
            onClick={addSkill}
            className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          >
            Add
          </button>
        </div>
      )}
    </section>
  );
}
