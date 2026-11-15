import { useState } from 'react'
import { useOutletContext } from 'react-router-dom'
import PageHeader from '../../components/dashboard/PageHeader'

function AstronautNotifications() {
  const { notifications, markNotificationRead } = useOutletContext()
  const [busyId, setBusyId] = useState('')
  const [error, setError] = useState('')

  async function markRead(notification) {
    setBusyId(String(notification.id))
    setError('')
    try {
      await markNotificationRead(notification.id)
    } catch (requestError) {
      setError(requestError.message || 'The notification could not be marked as read.')
    } finally {
      setBusyId('')
    }
  }

  return (
    <div className="space-y-5">
      <PageHeader eyebrow="Account notifications" title="Notifications" description="Notifications for your account are loaded and updated through the backend." />
      {error ? <p role="alert" className="rounded-xl border border-rose-300/15 bg-rose-300/[0.05] p-3 text-sm text-rose-100">{error}</p> : null}
      <ul className="divide-y divide-white/[0.06] overflow-hidden rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85">
        {notifications.map((item) => (
          <li key={item.id} className="flex flex-wrap items-start gap-3 p-4">
            <div className="min-w-0 flex-1">
              <h2 className="text-sm font-medium text-slate-100">{item.title}</h2>
              <p className="mt-1 text-xs text-slate-500">{item.createdAt ? new Date(item.createdAt).toLocaleString() : 'Time unavailable'}{item.type ? ` · ${item.type}` : ''}</p>
              {item.message ? <p className="mt-2 whitespace-pre-wrap text-sm leading-5 text-slate-300">{item.message}</p> : null}
            </div>
            <button type="button" onClick={() => markRead(item)} disabled={Boolean(item.readAt) || busyId === String(item.id)} className="min-h-9 rounded-lg px-3 text-xs text-cyan-100 hover:bg-white/[0.05] disabled:text-slate-600">
              {item.readAt ? 'Read' : busyId === String(item.id) ? 'Saving…' : 'Mark read'}
            </button>
          </li>
        ))}
        {!notifications.length ? <li className="p-5 text-sm text-slate-400">No notifications are available.</li> : null}
      </ul>
    </div>
  )
}

export default AstronautNotifications
