import { BrowserRouter as Router, Route, Navigate, Routes, useNavigate } from 'react-router-dom';
import { useEffect } from 'react';
import { useAuth } from './context/AuthContext';
import { Layout } from './components/layout/Layout';
import LoginForm from './components/auth/LoginForm';
import RegisterForm from './components/auth/RegisterForm';
import PatientDashboard from './pages/PatientDashboard';
import NutritionistDashboard from './pages/NutritionistDashboard';
import AdminDashboard from './pages/AdminDashboard';
import PatientAppointments from './pages/PatientAppointments';
import NutritionistProposals from './pages/NutritionistProposals';
import NutritionistAppointments from './pages/NutritionistAppointments';
import NutritionistSchedule from './pages/NutritionistSchedule';
import AdminPending from './pages/AdminPending';
import AdminLogs from './pages/AdminLogs';
import AdminPendingDisable from './pages/AdminPendingDisable';
import MapView from './pages/MapView';
import NutritionalPlans from './pages/NutritionalPlans';
import AppointmentDetail from './pages/AppointmentDetail';
import HealthDataPage from './pages/HealthData';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import AccountStatusGuard from './components/auth/AccountStatusGuard';
import { Toaster } from './components/ui/sonner';

function NavigationSetter() {
  const navigate = useNavigate();
  const { setNavigateCallback } = useAuth();
  
  useEffect(() => {
    if (setNavigateCallback) {
      setNavigateCallback(navigate);
    }
  }, [navigate, setNavigateCallback]);
  
  return null;
}

function AppRoutes() {
  const { user } = useAuth();

  if (!user) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-emerald-50 to-teal-50">
        <div className="container mx-auto py-12">
          <Routes>
            <Route path="/login" element={<LoginForm />} />
            <Route path="/register" element={<RegisterForm />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
            <Route path="/reset-password" element={<ResetPasswordPage />} />
            <Route path="*" element={<Navigate to="/login" />} />
          </Routes>
        </div>
      </div>
    );
  }

  const commonRoutes = (
    <>
      <Route path="/map" element={<MapView />} />
      <Route path="/plans" element={<NutritionalPlans />} />
      <Route path="/health-data" element={<HealthDataPage />} />
    </>
  );

  const patientRoutes = (
    <>
      <Route path="/appointments" element={<PatientAppointments />} />
      <Route path="/appointments/:id" element={<AppointmentDetail />} />
    </>
  );

  const nutritionistRoutes = (
    <>
      <Route path="/appointments" element={<NutritionistAppointments />} />
      <Route path="/proposals" element={<NutritionistProposals />} />
      <Route path="/schedule" element={<NutritionistSchedule />} />
    </>
  );

  const adminRoutes = (
    <>
      <Route path="/pending" element={<AdminPending />} />
      <Route path="/pending-disable" element={<AdminPendingDisable />} />
      <Route path="/nutritionists" element={<AdminPending />} />
      <Route path="/logs" element={<AdminLogs />} />
    </>
  );

  return (
    <AccountStatusGuard>
      <Layout>
        <Routes>
          <Route path="/dashboard" element={
            user.role === 'PATIENT' ? <PatientDashboard /> :
            user.role === 'NUTRITIONIST' ? <NutritionistDashboard /> :
            <AdminDashboard />
          } />
          
          {commonRoutes}
          
          {user.role === 'PATIENT' && patientRoutes}
          {user.role === 'NUTRITIONIST' && nutritionistRoutes}
          {user.role === 'ADMIN' && adminRoutes}
          
          <Route path="/login" element={<Navigate to="/dashboard" />} />
          <Route path="/register" element={<Navigate to="/dashboard" />} />
          <Route path="/forgot-password" element={<Navigate to="/dashboard" />} />
          <Route path="/reset-password" element={<Navigate to="/dashboard" />} />
          <Route path="*" element={<Navigate to="/dashboard" />} />
        </Routes>
      </Layout>
    </AccountStatusGuard>
  );
}

function App() {
  return (
    <Router>
      <NavigationSetter />
      <AppRoutes />
      <Toaster position="top-right" />
    </Router>
  );
}

export default App;