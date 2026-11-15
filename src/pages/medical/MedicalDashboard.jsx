import { useEffect, useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { api } from '../../api/client'
import { useAuth } from '../../auth/AuthProvider'
import AccountSession from '../../components/auth/AccountSession'
import ApiState from '../../components/dashboard/ApiState'
import MedicalSidebar from '../../components/medical/MedicalSidebar'
import MedicalTopbar from '../../components/medical/MedicalTopbar'
import { MEDICAL_PAGE_TITLES } from '../../data/medicalMockData'

function contentOf(page) {
  return Array.isArray(page) ? page : page?.content || []
}

function formatAlert(alert, astronautName) {
  const priority = alert.priority
  const severity = priority === 'CRITICAL' || priority === 'HIGH'
    ? 'Critical'
    : priority === 'INFORMATIONAL'
      ? 'Information'
      : 'Warning'
  const status = {
    OPEN: 'New',
    ACKNOWLEDGED: 'Acknowledged',
    ESCALATED: 'Escalated',
    RESOLVED: 'Resolved',
    CLOSED: 'Closed',
  }[alert.status] || alert.status
  return {
    ...alert,
    id: String(alert.id),
    astronautId: String(alert.astronautId),
    astronautName,
    severity,
    status,
    timestamp: alert.createdAt,
    explanation: [alert.description, alert.reason].filter(Boolean).join(' — '),
  }
}

function MedicalDashboard() {
  const { user } = useAuth()
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false)
  const [mobileSidebar, setMobileSidebar] = useState({ pathname: '', open: false })
  const [resource, setResource] = useState({ loading: true, error: '', astronauts: [], alerts: [], notifications: [], reports: [], followUps: [] })
  const [actionError, setActionError] = useState('')
  const { pathname } = useLocation()
  const title = pathname.startsWith('/medical/dashboard/astronauts/')
    ? 'Astronaut Details'
    : MEDICAL_PAGE_TITLES[pathname] || 'Medical Operations'
  const mobileSidebarOpen = mobileSidebar.pathname === pathname && mobileSidebar.open
  const scopedAstronauts = resource.astronauts
  const unreadCount = resource.notifications.filter((notification) => !notification.read).length
  const closeMobileSidebar = () => setMobileSidebar({ pathname, open: false })

  useEffect(() => {
    let active = true
    async function load() {
      setResource((current) => ({ ...current, loading: true, error: '' }))
      try {
        const [rosterPage, notificationPage, reportPage, followUpPage] = await Promise.all([
          api.medicalAstronauts('?page=0&size=20'),
          api.notifications(),
          api.wellbeingInbox(),
          api.observationFollowUps(),
        ])
        const roster = contentOf(rosterPage).map((item) => ({
          id: String(item.astronautId),
          name: item.fullName,
          avatar: item.fullName.split(/\s+/).map((part) => part[0]).join('').slice(0, 2).toUpperCase(),
          mission: 'Assigned astronaut',
          monitoring: item.status,
          latestUpdate: item.latestReadingAt ? new Date(item.latestReadingAt).toLocaleString() : 'No readings reported',
          alertSummary: `${item.activeAlertCount} active alert${item.activeAlertCount === 1 ? '' : 's'}`,
          alertTone: item.activeAlertCount ? 'warning' : 'neutral',
          latestReadings: item.latestReadings,
        }))
        const alertPages = await Promise.all(roster.map((astronaut) => api.astronautAlerts(astronaut.id)))
        const alerts = alertPages.flatMap((page, index) => contentOf(page).map((alert) => formatAlert(alert, roster[index].name)))
        if (active) {
          setResource({
            loading: false,
            error: '',
            astronauts: roster,
            alerts,
            notifications: contentOf(notificationPage).map((item) => ({
              id: String(item.id),
              title: item.title,
              detail: item.message,
              type: item.type === 'ALERT' ? 'Alert' : item.type === 'DEVICE' ? 'Device' : 'Check-in',
              time: new Date(item.createdAt).toLocaleString(),
              read: Boolean(item.readAt),
              astronautId: item.resourceType === 'ASTRONAUT' ? item.resourceId : '',
            })),
            reports: contentOf(reportPage),
            followUps: contentOf(followUpPage),
          })
        }
      } catch (error) {
        if (active) setResource((current) => ({ ...current, loading: false, error: error.message || 'Unable to load authorized medical data.' }))
      }
    }
    load()
    return () => { active = false }
  }, [user?.id])

  async function updateAlertStatus(alertId, nextStatus) {
    setActionError('')
    const action = nextStatus === 'Acknowledged' ? 'acknowledge' : 'resolve'
    try {
      const updated = await api.alertAction(alertId, action)
      setResource((current) => ({
        ...current,
        alerts: current.alerts.map((alert) => alert.id === String(updated.id) ? formatAlert(updated, alert.astronautName) : alert),
      }))
    } catch (error) {
      setActionError(error.message || 'The alert status could not be updated.')
    }
  }

  async function addObservation(observation) {
    setActionError('')
    try {
      return await api.createObservation(observation)
    } catch (error) {
      setActionError(error.message || 'The medical observation could not be saved.')
      throw error
    }
  }

  async function reviewReport(reportId, body) {
    setActionError('')
    try {
      const updated = await api.reviewWellbeingReport(reportId, body)
      setResource((current) => ({
        ...current,
        reports: current.reports.map((report) => report.id === updated.id ? updated : report),
      }))
      return updated
    } catch (error) {
      setActionError(error.message || 'The well-being report review could not be saved.')
      throw error
    }
  }

  async function toggleNotificationRead(notificationId) {
    setActionError('')
    try {
      const updated = await api.markNotificationRead(notificationId)
      setResource((current) => ({
        ...current,
        notifications: current.notifications.map((item) => item.id === String(updated.id) ? { ...item, read: Boolean(updated.readAt) } : item),
      }))
    } catch (error) {
      setActionError(error.message || 'The notification could not be marked as read.')
    }
  }

  async function markAllNotificationsRead() {
    setActionError('')
    try {
      await api.markAllNotificationsRead()
      setResource((current) => ({ ...current, notifications: current.notifications.map((item) => ({ ...item, read: true })) }))
    } catch (error) {
      setActionError(error.message || 'Notifications could not be marked as read.')
    }
  }

  return (
    <div className="flex min-h-screen w-full bg-[#07111f] text-slate-100">
      {mobileSidebarOpen ? (
        <button
          type="button"
          className="fixed inset-0 z-40 bg-black/60 lg:hidden"
          onClick={closeMobileSidebar}
          aria-label="Close medical navigation overlay"
        />
      ) : null}
      <MedicalSidebar
        collapsed={sidebarCollapsed}
        mobileOpen={mobileSidebarOpen}
        onToggleCollapse={() => setSidebarCollapsed((collapsed) => !collapsed)}
        onMobileClose={closeMobileSidebar}
      />
      <div className="flex min-h-screen min-w-0 flex-1 flex-col">
        <MedicalTopbar
          title={title}
          onOpenMobile={() => setMobileSidebar({ pathname, open: true })}
          unreadCount={unreadCount}
        />
        <AccountSession />
        <main className="w-full flex-1 px-4 py-5 sm:px-6 sm:py-7 xl:px-8">
          <div className="mx-auto w-full max-w-[1600px] space-y-4">
            {actionError ? <p role="alert" className="rounded-xl border border-rose-300/15 bg-rose-300/[0.05] p-3 text-sm text-rose-100">{actionError}</p> : null}
            <ApiState loading={resource.loading} error={resource.error}>
              <Outlet context={{
                astronauts: scopedAstronauts,
                allAstronauts: scopedAstronauts,
                alerts: resource.alerts,
                allAlerts: resource.alerts,
                audit: [],
                updateAlertStatus,
                observations: [],
                addObservation,
                notifications: resource.notifications,
                toggleNotificationRead,
                markAllNotificationsRead,
                reports: resource.reports,
                followUps: resource.followUps,
                reviewReport,
              }} />
            </ApiState>
          </div>
        </main>
        <footer className="border-t border-white/[0.06] px-4 py-4 text-center text-[11px] leading-5 text-slate-500 sm:px-6">
          ASTRA HEALTH medical dashboard · Access is enforced by the backend; operational flags are not diagnoses.
        </footer>
      </div>
    </div>
  )
}

export default MedicalDashboard
