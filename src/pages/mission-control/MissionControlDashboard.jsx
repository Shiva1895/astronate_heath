import { useEffect, useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { api } from '../../api/client'
import { useAuth } from '../../auth/AuthProvider'
import AccountSession from '../../components/auth/AccountSession'
import ApiState from '../../components/dashboard/ApiState'
import MissionSidebar from '../../components/mission-control/MissionSidebar'
import MissionTopbar from '../../components/mission-control/MissionTopbar'

function contentOf(page) {
  return Array.isArray(page) ? page : page?.content || []
}

function MissionControlDashboard() {
  const { user } = useAuth()
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false)
  const [mobileSidebar, setMobileSidebar] = useState({ pathname: '', open: false })
  const [state, setState] = useState({ loading: true, error: '', missions: [], notifications: [], devices: [] })
  const { pathname } = useLocation()
  const mobileSidebarOpen = mobileSidebar.pathname === pathname && mobileSidebar.open
  const unreadCount = state.notifications.filter((notification) => !notification.read).length
  const closeMobileSidebar = () => setMobileSidebar({ pathname, open: false })

  useEffect(() => {
    let active = true
    async function load() {
      try {
        const [missionPage, notificationPage] = await Promise.all([api.missions(), api.notifications()])
        if (active) {
          setState({
            loading: false,
            error: '',
            missions: contentOf(missionPage),
            notifications: contentOf(notificationPage).map((item) => ({
              id: String(item.id),
              title: item.title,
              detail: item.message,
              type: item.type,
              time: new Date(item.createdAt).toLocaleString(),
              read: Boolean(item.readAt),
            })),
            devices: [],
          })
        }
      } catch (error) {
        if (active) setState((current) => ({ ...current, loading: false, error: error.message || 'Mission operations data could not be loaded.' }))
      }
    }
    load()
    return () => { active = false }
  }, [user?.id])

  async function loadDevices() {
    const page = await api.devicesList()
    const devices = contentOf(page)
    setState((current) => ({ ...current, devices }))
    return devices
  }

  async function markNotificationRead(notificationId) {
    try {
      const updated = await api.markNotificationRead(notificationId)
      setState((current) => ({
        ...current,
        notifications: current.notifications.map((item) => item.id === String(updated.id) ? { ...item, read: Boolean(updated.readAt) } : item),
      }))
    } catch (error) {
      setState((current) => ({ ...current, error: error.message || 'Notification could not be marked as read.' }))
    }
  }

  async function markAllNotificationsRead() {
    try {
      await api.markAllNotificationsRead()
      setState((current) => ({ ...current, notifications: current.notifications.map((item) => ({ ...item, read: true })) }))
    } catch (error) {
      setState((current) => ({ ...current, error: error.message || 'Notifications could not be marked as read.' }))
    }
  }

  return (
    <div className="flex min-h-screen w-full bg-[#07111f] text-slate-100">
      {mobileSidebarOpen ? <button type="button" className="fixed inset-0 z-40 bg-black/60 lg:hidden" onClick={closeMobileSidebar} aria-label="Close Mission Control navigation overlay" /> : null}
      <MissionSidebar
        collapsed={sidebarCollapsed}
        mobileOpen={mobileSidebarOpen}
        onToggleCollapse={() => setSidebarCollapsed((collapsed) => !collapsed)}
        onMobileClose={closeMobileSidebar}
      />
      <div className="flex min-h-screen min-w-0 flex-1 flex-col">
        <MissionTopbar unreadCount={unreadCount} onOpenMobile={() => setMobileSidebar({ pathname, open: true })} />
        <AccountSession />
        <main className="w-full min-w-0 flex-1 px-4 py-5 sm:px-6 sm:py-7 xl:px-8">
          <div className="mx-auto w-full max-w-[1600px] space-y-4">
            <ApiState loading={state.loading} error={state.error}>
              <Outlet context={{
                missions: state.missions,
                notifications: state.notifications,
                devices: state.devices,
                loadDevices,
                markNotificationRead,
                markAllNotificationsRead,
              }} />
            </ApiState>
          </div>
        </main>
        <footer className="border-t border-white/[0.06] px-4 py-4 text-center text-[11px] leading-5 text-slate-500 sm:px-6">
          Mission Control displays mission-level operational summaries only. Private health records and medical observations are not exposed.
        </footer>
      </div>
    </div>
  )
}

export default MissionControlDashboard
