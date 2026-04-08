import { TrendingDown, TrendingUp } from 'lucide-react';

const COLOR_MAP = {
  brand: {
    iconBg: 'bg-brand-100 text-brand-600',
    trend: 'text-brand-600',
  },
  green: {
    iconBg: 'bg-green-100 text-green-600',
    trend: 'text-green-600',
  },
  blue: {
    iconBg: 'bg-blue-100 text-blue-600',
    trend: 'text-blue-600',
  },
  amber: {
    iconBg: 'bg-amber-100 text-amber-600',
    trend: 'text-amber-600',
  },
  red: {
    iconBg: 'bg-red-100 text-red-600',
    trend: 'text-red-600',
  },
};

export function StatCard({
  title,
  value,
  subtitle,
  icon: Icon,
  trend,
  trendValue,
  color = 'brand',
  className = '',
}) {
  const palette = COLOR_MAP[color] || COLOR_MAP.brand;

  return (
    <div
      className={`rounded-xl border border-gray-200 bg-white p-6 shadow-sm hover:shadow-md transition-shadow ${className}`}
    >
      <div className="flex items-start justify-between">
        <div className="flex-1 min-w-0">
          <p className="text-sm font-medium text-gray-500 truncate">{title}</p>
          <p className="mt-1 text-2xl font-bold text-gray-900">{value}</p>
        </div>
        {Icon && (
          <div className={`rounded-lg p-2.5 ${palette.iconBg}`}>
            <Icon className="h-5 w-5" />
          </div>
        )}
      </div>

      {(subtitle || trendValue) && (
        <div className="mt-3 flex items-center gap-2 text-sm">
          {trend && trendValue && (
            <span
              className={`inline-flex items-center gap-0.5 font-medium ${
                trend === 'up'
                  ? 'text-green-600'
                  : trend === 'down'
                    ? 'text-red-600'
                    : 'text-gray-500'
              }`}
            >
              {trend === 'up' && <TrendingUp className="h-3.5 w-3.5" />}
              {trend === 'down' && <TrendingDown className="h-3.5 w-3.5" />}
              {trendValue}
            </span>
          )}
          {subtitle && <span className="text-gray-500 truncate">{subtitle}</span>}
        </div>
      )}
    </div>
  );
}
