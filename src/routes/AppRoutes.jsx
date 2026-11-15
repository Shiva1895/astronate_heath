import { lazy, Suspense } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import LandingPage from '../pages/LandingPage'
import Login from '../pages/auth/Login'
import Register from '../pages/auth/Register'
import ForgotPassword from '../pages/auth/ForgotPassword'
import RequireAuth from '../components/auth/RequireAuth'

const AstronautDashboard = lazy(() => import('../pages/astronaut/AstronautDashboard'))
const DashboardOverview = lazy(() => import('../pages/astronaut/DashboardOverview'))
const HealthMonitoring = lazy(() => import('../pages/astronaut/HealthMonitoring'))
const HealthAnalytics = lazy(() => import('../pages/astronaut/HealthAnalytics'))
const Alerts = lazy(() => import('../pages/astronaut/Alerts'))
const AstronautNotifications = lazy(() => import('../pages/astronaut/AstronautNotifications'))
const Devices = lazy(() => import('../pages/astronaut/Devices'))
const SleepActivity = lazy(() => import('../pages/astronaut/SleepActivity'))
const WellbeingReports = lazy(() => import('../pages/astronaut/WellbeingReports'))
const Profile = lazy(() => import('../pages/astronaut/Profile'))
const Settings = lazy(() => import('../pages/astronaut/Settings'))
const MedicalDashboard = lazy(() => import('../pages/medical/MedicalDashboard'))
const MedicalOverview = lazy(() => import('../pages/medical/MedicalOverview'))
const MyAstronauts = lazy(() => import('../pages/medical/MyAstronauts'))
const MedicalHealthMonitoring = lazy(() => import('../pages/medical/MedicalHealthMonitoring'))
const MedicalAlertManagement = lazy(() => import('../pages/medical/MedicalAlertManagement'))
const MedicalHistory = lazy(() => import('../pages/medical/MedicalHistory'))
const MedicalObservations = lazy(() => import('../pages/medical/MedicalObservations'))
const MedicalReports = lazy(() => import('../pages/medical/MedicalReports'))
const MedicalNotifications = lazy(() => import('../pages/medical/MedicalNotifications'))
const MedicalProfile = lazy(() => import('../pages/medical/MedicalProfile'))
const MedicalSettings = lazy(() => import('../pages/medical/MedicalSettings'))
const AuditHistory = lazy(() => import('../pages/medical/AuditHistory'))
const AstronautDetails = lazy(() => import('../pages/medical/AstronautDetails'))
const MissionControlDashboard = lazy(() => import('../pages/mission-control/MissionControlDashboard'))
const MissionControlOverview = lazy(() => import('../pages/mission-control/MissionControlOverview'))
const MissionControlSection = lazy(() => import('../pages/mission-control/MissionControlSection'))

function AppRoutes() {
  return (
    <Suspense fallback={<div className="min-h-screen px-6 py-10 text-sm text-slate-400">Loading ASTRA HEALTH…</div>}>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/astronaut/dashboard" element={<RequireAuth roles={['ASTRONAUT']}><AstronautDashboard /></RequireAuth>}>
          <Route index element={<DashboardOverview />} />
          <Route path="health" element={<HealthMonitoring />} />
          <Route path="analytics" element={<HealthAnalytics />} />
          <Route path="alerts" element={<Alerts />} />
          <Route path="notifications" element={<AstronautNotifications />} />
          <Route path="devices" element={<Devices />} />
          <Route path="sleep-activity" element={<SleepActivity />} />
          <Route path="wellbeing" element={<WellbeingReports />} />
          <Route path="profile" element={<Profile />} />
          <Route path="settings" element={<Settings />} />
        </Route>
        <Route path="/medical/dashboard" element={<RequireAuth roles={['MEDICAL_OFFICER', 'ADMINISTRATOR']}><MedicalDashboard /></RequireAuth>}>
          <Route index element={<MedicalOverview />} />
          <Route path="astronauts" element={<MyAstronauts />} />
          <Route path="astronauts/:astronautId" element={<AstronautDetails />} />
          <Route path="health" element={<MedicalHealthMonitoring />} />
          <Route path="alerts" element={<MedicalAlertManagement />} />
          <Route path="history" element={<MedicalHistory />} />
          <Route path="observations" element={<MedicalObservations />} />
          <Route path="reports" element={<MedicalReports />} />
          <Route path="notifications" element={<MedicalNotifications />} />
          <Route path="profile" element={<MedicalProfile />} />
          <Route path="settings" element={<MedicalSettings />} />
          <Route path="audit" element={<RequireAuth roles={['ADMINISTRATOR']}><AuditHistory /></RequireAuth>} />
        </Route>
        <Route path="/mission-control/dashboard" element={<RequireAuth roles={['MISSION_CONTROL', 'ADMINISTRATOR']}><MissionControlDashboard /></RequireAuth>}>
          <Route index element={<MissionControlOverview />} />
          <Route path="astronauts/:astronautId" element={<Navigate to="/mission-control/dashboard" replace />} />
          <Route path=":section" element={<MissionControlSection />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  )
}

export default AppRoutes
