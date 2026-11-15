import { Bell, ChevronDown, Menu } from 'lucide-react'
import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '../../auth/AuthProvider'
import {
  MISSION_CONTROL_BASE_PATH,
  MISSION_CONTROL_NAVIGATION,
} from '../../data/missionControlMockData'

function MissionTopbar({ unreadCount, onOpenMobile }) {
  const { user } = useAuth()
  const [profileOpen, setProfileOpen] = useState(false)
  const { pathname } = useLocation()
  const page = MISSION_CONTROL_NAVIGATION.find((item) => item.path === pathname)
  const title = page?.label || (pathname.includes('/astronauts/') ? 'Astronaut Details' : 'Mission Control')

  return (
    <header className="sticky top-0 z-30 border-b border-white/[0.08] bg-[#07111f]/90 backdrop-blur-xl">
      <div className="flex min-h-[76px] flex-wrap items-center justify-between gap-3 px-4 py-2 sm:px-6 xl:flex-nowrap xl:px-8">
        <div className="flex min-w-0 items-center gap-3">
          <button
            type="button"
            onClick={onOpenMobile}
            className="inline-flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border border-white/10 bg-white/[0.03] text-slate-300 hover:bg-white/[0.07] lg:hidden"
            aria-label="Open Mission Control navigation"
          >
            <Menu size={19} />
          </button>
          <div className="min-w-0">
            <p className="hidden text-[10px] font-medium uppercase tracking-[0.15em] text-slate-500 sm:block">
              ASTRA HEALTH <span className="px-1 text-slate-700">/</span> Mission Operations
            </p>
            <h1 className="truncate text-lg font-semibold text-slate-100 sm:mt-1 sm:text-xl">{title}</h1>
          </div>
        </div>

        <div className="flex min-w-0 items-center gap-2 sm:gap-3">
          <div className="hidden rounded-xl border border-white/10 bg-white/[0.03] px-3 py-2 sm:block">
            <p className="text-[10px] uppercase tracking-[0.12em] text-slate-500">Mission Control</p>
            <p className="mt-0.5 text-xs font-medium text-slate-200">Mission-level data access</p>
          </div>
          <Link
            to={`${MISSION_CONTROL_BASE_PATH}/notifications`}
            className="relative inline-flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border border-white/10 bg-white/[0.03] text-slate-300 hover:bg-white/[0.07] hover:text-white"
            aria-label={`Notifications${unreadCount ? `, ${unreadCount} unread` : ''}`}
          >
            <Bell size={18} />
            {unreadCount > 0 ? <span className="absolute right-2 top-2 h-1.5 w-1.5 rounded-full bg-cyan-300" /> : null}
          </Link>
          <div className="relative">
            <button
              type="button"
              onClick={() => setProfileOpen((open) => !open)}
              className="flex min-h-11 items-center gap-2 rounded-xl border border-white/10 bg-white/[0.03] p-1.5 pr-2.5 text-left hover:bg-white/[0.07] sm:gap-3 sm:pr-3"
              aria-label="Mission Control operator profile menu"
              aria-expanded={profileOpen}
            >
              <span className="flex h-8 w-8 items-center justify-center rounded-full bg-gradient-to-br from-cyan-300/25 to-violet-300/20 text-[10px] font-semibold text-cyan-100">
                {(user?.displayName || user?.username || user?.email || 'MC').slice(0, 2).toUpperCase()}
              </span>
              <span className="hidden min-w-0 sm:block">
                <span className="block max-w-32 truncate text-xs font-medium text-slate-100">{user?.displayName || user?.username || user?.email}</span>
                <span className="block text-[10px] text-slate-500">{user?.roles?.join(', ')}</span>
              </span>
              <ChevronDown size={14} className="hidden text-slate-500 sm:block" />
            </button>
            {profileOpen ? (
              <div className="absolute right-0 top-14 z-50 w-56 rounded-2xl border border-white/10 bg-[#0b1b2b] p-2 shadow-2xl shadow-black/40">
                <div className="px-3 py-2">
                  <p className="truncate text-sm font-medium text-slate-100">{user?.displayName || user?.username || user?.email}</p>
                  <p className="mt-1 text-xs text-slate-500">{user?.roles?.join(', ')}</p>
                </div>
              </div>
            ) : null}
          </div>
        </div>
      </div>
    </header>
  )
}

export default MissionTopbar
