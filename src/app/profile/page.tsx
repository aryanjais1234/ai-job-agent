"use client";

import { useState } from "react";
import { UserProfile } from "@/types/profile";
import PersonalInfoSection from "@/components/PersonalInfoSection";
import SkillsSection from "@/components/SkillsSection";
import ExperienceSection from "@/components/ExperienceSection";
import JobPreferencesSection from "@/components/JobPreferencesSection";
import ResumeSection from "@/components/ResumeSection";

const defaultProfile: UserProfile = {
  personalInfo: {
    firstName: "",
    lastName: "",
    email: "",
    phone: "",
    location: "",
    linkedIn: "",
    summary: "",
  },
  skills: [],
  experience: [],
  jobPreferences: {
    desiredRole: "",
    desiredLocation: "",
    remotePreference: "",
    salaryMin: "",
    salaryMax: "",
    jobType: "",
  },
  resumeFileName: null,
};

export default function ProfilePage() {
  const [profile, setProfile] = useState<UserProfile>(defaultProfile);
  const [isEditing, setIsEditing] = useState(false);
  const [savedProfile, setSavedProfile] = useState<UserProfile>(defaultProfile);
  const [showSaveNotification, setShowSaveNotification] = useState(false);

  const handleSave = () => {
    setSavedProfile({ ...profile });
    setIsEditing(false);
    setShowSaveNotification(true);
    setTimeout(() => setShowSaveNotification(false), 3000);
  };

  const handleCancel = () => {
    setProfile({ ...savedProfile });
    setIsEditing(false);
  };

  return (
    <div className="min-h-screen bg-zinc-50 dark:bg-zinc-950">
      {/* Header */}
      <header className="border-b border-zinc-200 bg-white dark:border-zinc-800 dark:bg-zinc-900">
        <div className="mx-auto flex max-w-4xl items-center justify-between px-4 py-4 sm:px-6">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-full bg-blue-600 text-lg font-bold text-white">
              {profile.personalInfo.firstName
                ? profile.personalInfo.firstName[0].toUpperCase()
                : "U"}
            </div>
            <div>
              <h1 className="text-lg font-semibold text-zinc-900 dark:text-zinc-100">
                {profile.personalInfo.firstName || profile.personalInfo.lastName
                  ? `${profile.personalInfo.firstName} ${profile.personalInfo.lastName}`.trim()
                  : "Your Profile"}
              </h1>
              <p className="text-sm text-zinc-500 dark:text-zinc-400">
                AI Job Agent — Profile Settings
              </p>
            </div>
          </div>
          <div className="flex gap-2">
            {isEditing ? (
              <>
                <button
                  onClick={handleCancel}
                  className="rounded-lg border border-zinc-300 px-4 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-100 dark:border-zinc-600 dark:text-zinc-300 dark:hover:bg-zinc-800"
                >
                  Cancel
                </button>
                <button
                  onClick={handleSave}
                  className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                >
                  Save Profile
                </button>
              </>
            ) : (
              <button
                onClick={() => setIsEditing(true)}
                className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
              >
                Edit Profile
              </button>
            )}
          </div>
        </div>
      </header>

      {/* Save notification */}
      {showSaveNotification && (
        <div className="mx-auto max-w-4xl px-4 pt-4 sm:px-6">
          <div className="rounded-lg bg-green-50 p-3 text-sm text-green-700 dark:bg-green-900/30 dark:text-green-300">
            ✓ Profile saved successfully!
          </div>
        </div>
      )}

      {/* Profile sections */}
      <main className="mx-auto max-w-4xl space-y-6 px-4 py-8 sm:px-6">
        <PersonalInfoSection
          personalInfo={profile.personalInfo}
          isEditing={isEditing}
          onChange={(personalInfo) =>
            setProfile((prev) => ({ ...prev, personalInfo }))
          }
        />

        <SkillsSection
          skills={profile.skills}
          isEditing={isEditing}
          onChange={(skills) =>
            setProfile((prev) => ({ ...prev, skills }))
          }
        />

        <ExperienceSection
          experience={profile.experience}
          isEditing={isEditing}
          onChange={(experience) =>
            setProfile((prev) => ({ ...prev, experience }))
          }
        />

        <JobPreferencesSection
          preferences={profile.jobPreferences}
          isEditing={isEditing}
          onChange={(jobPreferences) =>
            setProfile((prev) => ({ ...prev, jobPreferences }))
          }
        />

        <ResumeSection
          resumeFileName={profile.resumeFileName}
          isEditing={isEditing}
          onFileChange={(resumeFileName) =>
            setProfile((prev) => ({ ...prev, resumeFileName }))
          }
        />
      </main>
    </div>
  );
}
