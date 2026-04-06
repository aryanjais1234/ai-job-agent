import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';

/**
 * Root application component.
 * Sets up client-side routing for all major screens.
 */
function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<PlaceholderPage title="Login" />} />
        <Route path="/register" element={<PlaceholderPage title="Register" />} />
        <Route path="/onboarding" element={<PlaceholderPage title="Onboarding" />} />
        <Route path="/dashboard" element={<PlaceholderPage title="Dashboard" />} />
        <Route path="/resume/:id" element={<PlaceholderPage title="Resume Preview" />} />
        <Route path="/applications" element={<PlaceholderPage title="Application Tracker" />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

function PlaceholderPage({ title }) {
  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="text-center">
        <h1 className="text-4xl font-bold text-gray-800 mb-2">🧠 AI Job Agent</h1>
        <p className="text-xl text-gray-500">{title} — coming soon</p>
      </div>
    </div>
  );
}

export default App;
