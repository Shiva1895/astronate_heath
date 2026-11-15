import { PanelLeftClose, PanelLeftOpen, X } from 'lucide-react'
import { Link, NavLink } from 'react-router-dom'
import { useAuth } from '../../auth/AuthProvider'
import { DASHBOARD_NAVIGATION } from '../../data/astronautDashboard'

function DashboardSidebar({ collapsed, mobileOpen, onToggleCollapse, onMobileClose }) {
  const { user } = useAuth()
  const name = user?.displayName || user?.username || user?.email || 'Astronaut'
  const initials = name.split(/[\s@]+/).filter(Boolean).map((part) => part[0]).join('').slice(0, 2).toUpperCase()
  return (
    <aside
      className={`fixed inset-y-0 left-0 z-50 flex w-72 shrink-0 flex-col border-r border-white/[0.08] bg-[#081522] transition-[width,transform] duration-300 lg:sticky lg:top-0 lg:h-screen lg:translate-x-0 ${
        mobileOpen ? 'translate-x-0' : '-translate-x-full'
      } ${collapsed ? 'lg:w-[84px]' : 'lg:w-72'}`}
      aria-label="Astronaut dashboard sidebar"
    >
      <div className={`flex h-[76px] items-center border-b border-white/[0.08] ${collapsed ? 'justify-center px-3' : 'justify-between px-5'}`}>
        <Link to="/astronaut/dashboard" className="flex min-w-0 items-center gap-3" aria-label="ASTRA HEALTH dashboard">
          <div className="orbit-mark small shrink-0">
            <span className="orbit-ring" />
          </div>
          {!collapsed ? (
            <span className="min-w-0">
              <span className="block truncate text-xs font-bold tracking-[0.2em] text-slate-100">ASTRA HEALTH</span>
              <span className="mt-1 block truncate text-[10px] text-slate-500">Astronaut Health Monitoring System</span>
            </span>
          ) : null}
        </Link>
        <button
          type="button"
          onClick={onToggleCollapse}
          className="hidden h-9 w-9 items-center justify-center rounded-lg text-slate-400 hover:bg-white/5 hover:text-slate-100 lg:inline-flex"
          aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          title={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          {collapsed ? <PanelLeftOpen size={18} /> : <PanelLeftClose size={18} />}
        </button>
        <button
          type="button"
          onClick={onMobileClose}
          className="inline-flex h-9 w-9 items-center justify-center rounded-lg text-slate-400 hover:bg-white/5 hover:text-slate-100 lg:hidden"
          aria-label="Close dashboard menu"
        >
          <X size={18} />
        </button>
      </div>

      <div className={`px-4 pt-5 ${collapsed ? 'lg:px-3' : ''}`}>
        {!collapsed ? (
          <p className="px-2 pb-3 text-[10px] font-semibold uppercase tracking-[0.18em] text-slate-500">Workspace</p>
        ) : null}
        <nav className="space-y-1" aria-label="Dashboard sections">
          {DASHBOARD_NAVIGATION.map((item) => {
            const Icon = item.icon

            return (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                onClick={onMobileClose}
                title={collapsed ? item.label : undefined}
                aria-label={collapsed ? item.label : undefined}
                className={({ isActive }) =>
                  `group flex min-h-11 items-center gap-3 rounded-xl px-3 text-sm transition ${
                    isActive
                      ? 'border border-cyan-300/15 bg-cyan-300/[0.09] font-medium text-cyan-100'
                      : 'border border-transparent text-slate-400 hover:bg-white/[0.04] hover:text-slate-100'
                  } ${collapsed ? 'justify-center lg:px-0' : ''}`
                }
              >
                <Icon size={18} className="shrink-0" />
                {!collapsed ? <span className="truncate">{item.label}</span> : null}
              </NavLink>
            )
          })}
        </nav>
      </div>

      <div className={`mt-auto border-t border-white/[0.08] p-4 ${collapsed ? 'lg:px-3' : ''}`}>
        {!collapsed ? (
          <div className="mb-4 flex min-w-0 items-center gap-3 px-1">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full border border-cyan-200/20 bg-cyan-200/10 text-xs font-semibold text-cyan-100">
              {initials}
            </div>
            <div className="min-w-0">
              <p className="truncate text-sm font-medium text-slate-100">{name}</p>
              <p className="truncate text-xs text-slate-500">Authenticated account</p>
            </div>
          </div>
        ) : (
          <div className="mb-4 flex justify-center">
            <div className="flex h-10 w-10 items-center justify-center rounded-full border border-cyan-200/20 bg-cyan-200/10 text-xs font-semibold text-cyan-100">
              {initials}
            </div>
          </div>
        )}
      </div>
    </aside>
  )
}

export default DashboardSidebar
