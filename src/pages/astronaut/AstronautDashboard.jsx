import { useEffect, useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { api } from '../../api/client'
import { useAuth } from '../../auth/AuthProvider'
import AccountSession from '../../components/auth/AccountSession'
import ApiState from '../../components/dashboard/ApiState'
import DashboardSidebar from '../../components/dashboard/DashboardSidebar'
import DashboardTopbar from '../../components/dashboard/DashboardTopbar'
import { PAGE_TITLES } from '../../data/astronautDashboard'

function contentOf(page) {
  return Array.isArray(page) ? page : page?.content || []
}

function formatAlert(alert) {
  const priority = alert.priority
  return {
    ...alert,
    id: String(alert.id),
    astronautId: String(alert.astronautId),
    severity: priority === 'CRITICAL' || priority === 'HIGH' ? 'Critical' : priority === 'INFORMATIONAL' ? 'Information' : 'Warning',
    status: ({ OPEN: 'New', ACKNOWLEDGED: 'Acknowledged', ESCALATED: 'Escalated', RESOLVED: 'Resolved', CLOSED: 'Closed' })[alert.status] || alert.status,
    timestamp: alert.createdAt,
    explanation: [alert.description, alert.reason].filter(Boolean).join(' — '),
  }
}

function AstronautDashboard() {
  const { user } = useAuth()
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false)
  const [mobileSidebar, setMobileSidebar] = useState({ pathname: '', open: false })
  const [resource, setResource] = useState({ loading: true, error: '', readings: [], latestReadings: [], alerts: [], devices: [], reports: [], notifications: [] })
  const { pathname } = useLocation()
  const title = PAGE_TITLES[pathname] || 'Astronaut Dashboard'
  const unreadCount = resource.notifications.filter((item) => !item.readAt).length

  const mobileSidebarOpen = mobileSidebar.pathname === pathname && mobileSidebar.open
  const closeMobileSidebar = () => setMobileSidebar({ pathname, open: false })

  useEffect(() => {
    let active = true
    async function load() {
      const astronautId = user?.astronautProfileId
      if (!astronautId) {
        setResource({ loading: false, error: 'Your account is not linked to an astronaut profile. Ask an administrator to link your profile before accessing health data.', readings: [], latestReadings: [], alerts: [], devices: [], reports: [], notifications: [] })
        return
      }
      setResource((current) => ({ ...current, loading: true, error: '' }))
      try {
        const [readingPage, latestReadings, alertPage, devicePage, reportPage, notificationPage] = await Promise.all([
          api.readings(astronautId),
          api.latestReadings(astronautId),
          api.astronautAlerts(astronautId),
          api.devices(astronautId),
          api.myWellbeingReports(),
          api.notifications(),
        ])
        if (active) {
          setResource({
            loading: false,
            error: '',
            readings: contentOf(readingPage),
            latestReadings: contentOf(latestReadings),
            alerts: contentOf(alertPage).map(formatAlert),
            devices: contentOf(devicePage),
            reports: contentOf(reportPage),
            notifications: contentOf(notificationPage),
          })
        }
      } catch (error) {
        if (active) setResource((current) => ({ ...current, loading: false, error: error.message || 'Authorized astronaut data could not be loaded.' }))
      }
    }
    load()
    return () => { active = false }
  }, [user?.astronautProfileId])

  async function markNotificationRead(notificationId) {
    const updated = await api.markNotificationRead(notificationId)
    setResource((current) => ({
      ...current,
      notifications: current.notifications.map((item) => item.id === updated.id ? updated : item),
    }))
  }

  return (
    <div className="flex min-h-screen w-full bg-[#07111f] text-slate-100">
      {mobileSidebarOpen ? (
        <button
          type="button"
          className="fixed inset-0 z-40 bg-black/60 lg:hidden"
          onClick={closeMobileSidebar}
          aria-label="Close dashboard navigation overlay"
        />
      ) : null}
      <DashboardSidebar
        collapsed={sidebarCollapsed}
        mobileOpen={mobileSidebarOpen}
        onToggleCollapse={() => setSidebarCollapsed((collapsed) => !collapsed)}
        onMobileClose={closeMobileSidebar}
      />
      <div className="flex min-h-screen min-w-0 flex-1 flex-col">
        <DashboardTopbar title={title} unreadCount={unreadCount} onOpenMobile={() => setMobileSidebar({ pathname, open: true })} />
        <AccountSession />
        <main className="w-full flex-1 px-4 py-5 sm:px-6 sm:py-7 xl:px-8">
          <div className="mx-auto w-full max-w-[1600px]">
            <ApiState loading={resource.loading} error={resource.error}>
              <Outlet context={{ ...resource, user, astronautId: user?.astronautProfileId, markNotificationRead }} />
            </ApiState>
          </div>
        </main>
        <footer className="border-t border-white/[0.06] px-4 py-4 text-center text-[11px] leading-5 text-slate-500 sm:px-6">
          ASTRA HEALTH astronaut dashboard · Health information is private and is not a diagnosis.
        </footer>
      </div>
    </div>
  )
}

export default AstronautDashboard
