export interface UserProfile {
  personalInfo: PersonalInfo;
  skills: string[];
  experience: WorkExperience[];
  jobPreferences: JobPreferences;
  resumeFileName: string | null;
}

export interface PersonalInfo {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  location: string;
  linkedIn: string;
  summary: string;
}

export interface WorkExperience {
  id: string;
  title: string;
  company: string;
  startDate: string;
  endDate: string;
  current: boolean;
  description: string;
}

export interface JobPreferences {
  desiredRole: string;
  desiredLocation: string;
  remotePreference: "remote" | "hybrid" | "onsite" | "";
  salaryMin: string;
  salaryMax: string;
  jobType: "full-time" | "part-time" | "contract" | "internship" | "";
}
