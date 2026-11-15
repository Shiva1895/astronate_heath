import { useMemo, useState } from 'react'
import { Bell, Check, CircleAlert } from 'lucide-react'

function MissionNotifications({ notifications, onToggleRead, onMarkAllRead, compact = false }) {
  const [filter, setFilter] = useState('All')
  const visible = useMemo(() => notifications.filter((notification) => {
    if (filter === 'Unread') return !notification.read
    return filter === 'All' || notification.type === filter
  }), [notifications, filter])
  const entries = compact ? visible.slice(0, 3) : visible

  return (
    <section className="min-w-0 rounded-2xl border border-white/[0.08] bg-[#0b1a29]/75" aria-labelledby="mission-notifications-title">
      <div className="flex flex-col justify-between gap-3 border-b border-white/[0.07] p-4 sm:flex-row sm:items-center sm:p-5">
        <div className="flex items-center gap-3">
          <span className="flex h-10 w-10 items-center justify-center rounded-xl border border-cyan-200/10 bg-cyan-200/[0.05] text-cyan-100"><Bell size={18} /></span>
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.14em] text-cyan-200/75">Mission updates · Sample</p>
            <h2 id="mission-notifications-title" className="mt-1 text-base font-semibold text-slate-50">Notifications</h2>
          </div>
        </div>
        {!compact ? (
          <button type="button" onClick={onMarkAllRead} disabled={!notifications.some((item) => !item.read)} className="inline-flex min-h-9 items-center justify-center gap-1.5 self-start rounded-lg px-3 text-xs font-medium text-cyan-200 hover:bg-cyan-200/[0.06] disabled:text-slate-600">
            <Check size={14} /> Mark all read
          </button>
        ) : null}
      </div>
      {!compact ? (
        <div className="flex gap-2 overflow-x-auto border-b border-white/[0.06] px-4 py-3 sm:px-5">
          {['All', 'Unread', 'Health alert', 'Medical team', 'Device', 'Measurement', 'Mission'].map((item) => (
            <button type="button" key={item} onClick={() => setFilter(item)} aria-pressed={filter === item} className={`min-h-9 shrink-0 rounded-lg px-3 text-xs font-medium ${filter === item ? 'bg-cyan-200/[0.09] text-cyan-100' : 'text-slate-400 hover:bg-white/[0.04] hover:text-slate-200'}`}>
              {item}
            </button>
          ))}
        </div>
      ) : null}
      <ul className="divide-y divide-white/[0.06]">
        {entries.map((notification) => (
          <li key={notification.id} className={`flex min-w-0 items-start gap-3 px-4 py-4 sm:px-5 ${notification.read ? '' : 'bg-cyan-200/[0.018]'}`}>
            <span className={`mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-xl ${notification.read ? 'bg-white/[0.035] text-slate-500' : 'bg-cyan-200/[0.06] text-cyan-100'}`}><CircleAlert size={16} /></span>
            <div className="min-w-0 flex-1">
              <div className="flex flex-wrap items-center gap-2">
                <h3 className="text-xs font-medium text-slate-100">{notification.title}</h3>
                {!notification.read ? <span className="rounded-full border border-cyan-200/10 bg-cyan-200/[0.05] px-2 py-0.5 text-[10px] text-cyan-100">Unread</span> : null}
              </div>
              <p className="mt-1 text-[10px] text-slate-500">{notification.type} · {notification.time}</p>
              {!compact ? <p className="mt-2 text-xs leading-5 text-slate-400">{notification.detail}</p> : null}
            </div>
            <button type="button" onClick={() => onToggleRead(notification.id)} className="inline-flex min-h-9 shrink-0 items-center rounded-lg px-2 text-[11px] text-slate-400 hover:bg-white/[0.05] hover:text-white">
              {notification.read ? 'Mark unread' : 'Mark read'}
            </button>
          </li>
        ))}
      </ul>
      {entries.length === 0 ? <p className="px-5 py-8 text-center text-sm text-slate-500">No notifications match this filter.</p> : null}
      <p className="px-4 pb-4 text-[10px] leading-5 text-slate-600 sm:px-5">Read status is held in demo session memory only. Notifications are not sent or persisted.</p>
    </section>
  )
}

export default MissionNotifications
