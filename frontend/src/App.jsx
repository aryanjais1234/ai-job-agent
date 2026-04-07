import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';

function PlaceholderPage({ title }) {
  return (
    <div className="text-center py-16">
      <h1 className="text-3xl font-bold text-gray-800 mb-2">{title}</h1>
      <p className="text-gray-500">Coming soon</p>
    </div>
  );
}

function DefaultRedirect() {
  const { isAuthenticated } = useAuth();
  return <Navigate to={isAuthenticated ? '/dashboard' : '/login'} replace />;
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public routes */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Protected routes */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Layout><DashboardPage /></Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/applications"
            element={
              <ProtectedRoute>
                <Layout><PlaceholderPage title="Application Tracker" /></Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/resume/:id"
            element={
              <ProtectedRoute>
                <Layout><PlaceholderPage title="Resume Preview" /></Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/onboarding"
            element={
              <ProtectedRoute>
                <Layout><PlaceholderPage title="Onboarding" /></Layout>
              </ProtectedRoute>
            }
          />

          {/* Default redirect */}
          <Route path="*" element={<DefaultRedirect />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
