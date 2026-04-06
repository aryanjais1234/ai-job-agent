import { useAuth } from '../context/AuthContext';

export default function DashboardPage() {
  const { user } = useAuth();

  const cards = [
    {
      title: 'Job Matches',
      description: 'AI-powered job recommendations tailored to your profile.',
      icon: '🎯',
      count: 0,
      color: 'bg-blue-50 text-blue-700',
    },
    {
      title: 'Applications',
      description: 'Track and manage your job applications.',
      icon: '📋',
      count: 0,
      color: 'bg-green-50 text-green-700',
    },
    {
      title: 'Resumes',
      description: 'Create and manage your AI-optimized resumes.',
      icon: '📄',
      count: 0,
      color: 'bg-purple-50 text-purple-700',
    },
  ];

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">
          Welcome, {user?.fullName || 'User'}!
        </h1>
        <p className="text-gray-500 mt-1">Here&apos;s an overview of your job search progress.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {cards.map((card) => (
          <div
            key={card.title}
            className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow"
          >
            <div className="flex items-center justify-between mb-4">
              <span className="text-3xl">{card.icon}</span>
              <span className={`text-2xl font-bold px-3 py-1 rounded-lg ${card.color}`}>
                {card.count}
              </span>
            </div>
            <h2 className="text-lg font-semibold text-gray-800">{card.title}</h2>
            <p className="text-sm text-gray-500 mt-1">{card.description}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
