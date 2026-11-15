import { Bell, Check, CircleAlert } from 'lucide-react'
import { useState } from 'react'

const FILTERS = ['All', 'Unread', 'Alert', 'Device', 'Check-in']

function MedicalNotificationPanel({ notifications, onToggleRead, onMarkAllRead }) {
  const [filter, setFilter] = useState('All')
  const [expandedId, setExpandedId] = useState(null)
  const visible = notifications.filter((notification) => {
    if (filter === 'Unread') return !notification.read
    if (filter === 'All') return true
    return notification.type === filter || (filter === 'Alert' && notification.type === 'Critical')
  })

  return (
    <section className="min-w-0 rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85">
      <div className="flex flex-col justify-between gap-3 border-b border-white/[0.07] p-4 sm:flex-row sm:items-center sm:p-5">
        <div className="flex items-center gap-3">
          <span className="flex h-10 w-10 items-center justify-center rounded-xl border border-cyan-200/10 bg-cyan-200/[0.06] text-cyan-100"><Bell size={18} /></span>
          <div>
            <h3 className="text-sm font-semibold text-slate-100">Medical notifications</h3>
            <p className="mt-1 text-xs text-slate-500">Account-scoped backend notifications</p>
          </div>
        </div>
        <button
          type="button"
          onClick={onMarkAllRead}
          disabled={!notifications.some((item) => !item.read)}
          className="inline-flex min-h-9 items-center justify-center gap-1.5 self-start rounded-lg px-3 text-xs font-medium text-cyan-200 hover:bg-cyan-200/[0.06] disabled:text-slate-600"
        >
          <Check size={14} /> Mark all read
        </button>
      </div>
      <div className="flex gap-2 overflow-x-auto border-b border-white/[0.06] px-4 py-3 sm:px-5">
        {FILTERS.map((item) => (
          <button
            type="button"
            key={item}
            onClick={() => setFilter(item)}
            aria-pressed={filter === item}
            className={`min-h-9 shrink-0 rounded-lg px-3 text-xs font-medium ${filter === item ? 'bg-cyan-200/[0.09] text-cyan-100' : 'text-slate-400 hover:bg-white/[0.04] hover:text-slate-200'}`}
          >
            {item}
          </button>
        ))}
      </div>
      <ul className="divide-y divide-white/[0.06]">
        {visible.map((notification) => {
          const expanded = expandedId === notification.id
          return (
            <li key={notification.id} className={`px-4 py-4 sm:px-5 ${notification.read ? '' : 'bg-cyan-200/[0.018]'}`}>
              <div className="flex items-start gap-3">
                <span className={`mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-xl ${notification.type === 'Critical' ? 'bg-rose-200/[0.08] text-rose-200' : 'bg-cyan-200/[0.06] text-cyan-100'}`}>
                  <CircleAlert size={16} />
                </span>
                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <h4 className="text-sm font-medium text-slate-100">{notification.title}</h4>
                    {!notification.read ? <span className="rounded-full border border-cyan-200/10 bg-cyan-200/[0.05] px-2 py-0.5 text-[10px] text-cyan-100">Unread</span> : null}
                  </div>
                  <p className="mt-1 text-xs text-slate-500">{notification.time} · {notification.astronautId}</p>
                  {expanded ? <p className="mt-3 rounded-lg border border-white/[0.06] bg-white/[0.02] p-3 text-xs leading-5 text-slate-400">{notification.detail}</p> : null}
                </div>
                <div className="flex shrink-0 flex-col items-end gap-1 sm:flex-row">
                  <button type="button" onClick={() => setExpandedId(expanded ? null : notification.id)} className="inline-flex min-h-9 items-center rounded-lg px-2 text-xs text-cyan-200 hover:bg-cyan-200/[0.06]">
                    {expanded ? 'Hide' : 'Details'}
                  </button>
                  <button type="button" onClick={() => onToggleRead(notification.id)} disabled={notification.read} className="inline-flex min-h-9 items-center rounded-lg px-2 text-xs text-slate-400 hover:bg-white/[0.05] hover:text-white disabled:cursor-default disabled:text-slate-600">
                    {notification.read ? 'Read' : 'Mark read'}
                  </button>
                </div>
              </div>
            </li>
          )
        })}
      </ul>
      {visible.length === 0 ? <p className="px-5 py-8 text-center text-sm text-slate-500">No notifications in this filter.</p> : null}
      <p className="px-4 pb-4 text-[11px] leading-5 text-slate-600 sm:px-5">Notifications are stored per account. The application polls the backend; no real-time push channel is configured.</p>
    </section>
  )
}

export default MedicalNotificationPanel
