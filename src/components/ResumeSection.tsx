"use client";

interface ResumeSectionProps {
  resumeFileName: string | null;
  isEditing: boolean;
  onFileChange: (fileName: string | null) => void;
}

export default function ResumeSection({
  resumeFileName,
  isEditing,
  onFileChange,
}: ResumeSectionProps) {
  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      onFileChange(file.name);
    }
  };

  return (
    <section className="rounded-xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
      <h2 className="mb-4 text-xl font-semibold text-zinc-900 dark:text-zinc-100">
        Resume
      </h2>
      {resumeFileName ? (
        <div className="flex items-center gap-3 rounded-lg border border-zinc-200 bg-zinc-50 p-4 dark:border-zinc-700 dark:bg-zinc-800">
          <svg
            className="h-8 w-8 text-blue-500"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
            />
          </svg>
          <div className="flex-1">
            <p className="text-sm font-medium text-zinc-900 dark:text-zinc-100">
              {resumeFileName}
            </p>
            <p className="text-xs text-zinc-500 dark:text-zinc-400">
              Uploaded resume
            </p>
          </div>
          {isEditing && (
            <button
              onClick={() => onFileChange(null)}
              className="text-sm text-red-500 hover:text-red-700"
            >
              Remove
            </button>
          )}
        </div>
      ) : isEditing ? (
        <label className="flex cursor-pointer flex-col items-center justify-center rounded-lg border-2 border-dashed border-zinc-300 p-8 transition hover:border-blue-400 dark:border-zinc-700 dark:hover:border-blue-600">
          <svg
            className="mb-2 h-10 w-10 text-zinc-400"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
            />
          </svg>
          <span className="text-sm font-medium text-zinc-600 dark:text-zinc-400">
            Click to upload your resume
          </span>
          <span className="mt-1 text-xs text-zinc-400">
            PDF, DOC, or DOCX (max 5MB)
          </span>
          <input
            type="file"
            accept=".pdf,.doc,.docx"
            onChange={handleFileSelect}
            className="hidden"
          />
        </label>
      ) : (
        <p className="text-sm text-zinc-500 dark:text-zinc-400">
          No resume uploaded yet.
        </p>
      )}
    </section>
  );
}
