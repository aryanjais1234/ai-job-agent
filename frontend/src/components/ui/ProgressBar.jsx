const SIZE_CLASSES = {
  sm: 'h-1.5',
  md: 'h-2.5',
  lg: 'h-4',
};

const THRESHOLDS = { LOW: 50, MEDIUM: 70, HIGH: 85 };

function getColor(value) {
  if (value < THRESHOLDS.LOW) return 'bg-red-500';
  if (value < THRESHOLDS.MEDIUM) return 'bg-amber-500';
  if (value < THRESHOLDS.HIGH) return 'bg-green-500';
  return 'bg-brand-600';
}

export function ProgressBar({ value = 0, size = 'md', color, showLabel = false, className = '' }) {
  const clamped = Math.max(0, Math.min(100, value));
  const barColor = color || getColor(clamped);

  return (
    <div className={`w-full ${className}`}>
      <div className={`w-full rounded-full bg-gray-200 overflow-hidden ${SIZE_CLASSES[size]}`}>
        <div
          role="progressbar"
          aria-valuenow={clamped}
          aria-valuemin={0}
          aria-valuemax={100}
          className={`${barColor} ${SIZE_CLASSES[size]} rounded-full transition-all duration-500 ease-out`}
          style={{ width: `${clamped}%` }}
        />
      </div>
      {showLabel && (
        <p className="mt-1 text-xs font-medium text-gray-600 text-right">{Math.round(clamped)}%</p>
      )}
    </div>
  );
}
