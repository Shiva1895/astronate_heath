import { useMemo, useState } from 'react'
import { ArrowUpRight, CircleAlert, X } from 'lucide-react'
import { Link } from 'react-router-dom'
import { MISSION_CONTROL_ASTRONAUTS, MISSION_CONTROL_BASE_PATH } from '../../data/missionControlMockData'

const SEVERITY_TONES = {
  Critical: 'border-rose-200/15 bg-rose-200/[0.06] text-rose-100',
  Warning: 'border-amber-200/15 bg-amber-200/[0.06] text-amber-100',
  Information: 'border-sky-200/15 bg-sky-200/[0.06] text-sky-100',
}

const STATUS_TONES = {
  New: 'text-cyan-100',
  Acknowledged: 'text-amber-100',
  'Under Review': 'text-violet-100',
  Resolved: 'text-emerald-100',
}

const NEXT_ACTION = {
  New: { label: 'Acknowledge', status: 'Acknowledged' },
  Acknowledged: { label: 'Start review', status: 'Under Review' },
  'Under Review': { label: 'Mark resolved', status: 'Resolved' },
}

function formatTimestamp(timestamp) {
  return new Date(timestamp).toLocaleString('en-GB', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
    timeZone: 'UTC',
  })
}

function MissionAlertPanel({ alerts, onUpdateStatus, audit = [], compact = false }) {
  const [severity, setSeverity] = useState('All')
  const [status, setStatus] = useState('All')
  const [selectedId, setSelectedId] = useState(null)
  const selectedAlert = alerts.find((alert) => alert.id === selectedId)
  const visibleAlerts = useMemo(() => alerts.filter((alert) => (
    (severity === 'All' || alert.severity === severity)
    && (status === 'All' || alert.status === status)
  )), [alerts, severity, status])
  const list = compact ? visibleAlerts.slice(0, 3) : visibleAlerts

  function renderAction(alert) {
    const action = NEXT_ACTION[alert.status]
    return action ? (
      <button type="button" onClick={() => onUpdateStatus(alert.id, action.status)} className="inline-flex min-h-9 items-center justify-center rounded-lg border border-cyan-200/15 bg-cyan-200/[0.045] px-3 text-[11px] font-medium text-cyan-100 hover:bg-cyan-200/[0.09]">
        {action.label}
      </button>
    ) : <span className="text-[11px] text-slate-500">No action needed</span>
  }

  return (
    <section className="min-w-0 rounded-2xl border border-white/[0.08] bg-[#0b1a29]/75 p-4 sm:p-5" aria-labelledby="mission-alerts-title">
      <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-end">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.14em] text-cyan-200/75">Alert coordination · Demo actions</p>
          <h2 id="mission-alerts-title" className="mt-2 text-lg font-semibold text-slate-50">Active Health Alerts</h2>
          <p className="mt-1 text-xs leading-5 text-slate-500">Mission Control may coordinate review; medical decisions remain with authorized medical personnel.</p>
        </div>
        {!compact ? (
          <div className="grid grid-cols-2 gap-2">
            <label>
              <span className="sr-only">Filter alerts by severity</span>
              <select value={severity} onChange={(event) => setSeverity(event.target.value)} className="min-h-10 w-full rounded-lg border border-white/10 bg-[#071522] px-3 text-xs text-slate-100 outline-none">
                <option>All</option><option>Critical</option><option>Warning</option><option>Information</option>
              </select>
            </label>
            <label>
              <span className="sr-only">Filter alerts by status</span>
              <select value={status} onChange={(event) => setStatus(event.target.value)} className="min-h-10 w-full rounded-lg border border-white/10 bg-[#071522] px-3 text-xs text-slate-100 outline-none">
                <option>All</option><option>New</option><option>Acknowledged</option><option>Under Review</option><option>Resolved</option>
              </select>
            </label>
          </div>
        ) : null}
      </div>
      <p className="mt-3 text-[11px] text-slate-500">Showing {list.length} of {visibleAlerts.length} illustrative alert{visibleAlerts.length === 1 ? '' : 's'}.</p>

      {list.length ? (
        <div className="mt-3 grid min-w-0 grid-cols-1 gap-3 2xl:grid-cols-2">
          {list.map((alert) => {
            const astronaut = MISSION_CONTROL_ASTRONAUTS.find((item) => item.id === alert.astronautId)
            return (
              <article key={alert.id} className="min-w-0 rounded-xl border border-white/[0.07] bg-white/[0.02] p-4">
                <div className="flex items-start gap-3">
                  <span className={`mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-xl border ${SEVERITY_TONES[alert.severity]}`}><CircleAlert size={16} /></span>
                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <p className="text-[10px] font-medium text-slate-500">{alert.id} · {alert.category}</p>
                      <span className={`rounded-full border px-2 py-0.5 text-[10px] ${SEVERITY_TONES[alert.severity]}`}>{alert.severity}</span>
                    </div>
                    <h3 className="mt-1 text-sm font-semibold leading-5 text-slate-100">{alert.title}</h3>
                    <p className="mt-1 text-xs text-slate-500">{astronaut?.name || 'Sample astronaut'} · {alert.astronautId} · {formatTimestamp(alert.timestamp)} UTC</p>
                  </div>
                </div>
                <p className="mt-3 text-xs leading-5 text-slate-400">{alert.explanation}</p>
                <div className="mt-3 flex flex-wrap items-center justify-between gap-2 border-t border-white/[0.06] pt-3">
                  <span className={`text-xs font-medium ${STATUS_TONES[alert.status]}`}>{alert.status}</span>
                  <div className="flex flex-wrap items-center justify-end gap-2">
                    <button type="button" onClick={() => setSelectedId(alert.id)} className="inline-flex min-h-9 items-center gap-1 rounded-lg px-2 text-[11px] font-medium text-cyan-100 hover:bg-cyan-200/[0.05]">
                      View alert <ArrowUpRight size={13} />
                    </button>
                    <Link to={`${MISSION_CONTROL_BASE_PATH}/astronauts/${alert.astronautId}`} className="inline-flex min-h-9 items-center rounded-lg px-2 text-[11px] font-medium text-slate-300 hover:bg-white/[0.05]">
                      View astronaut
                    </Link>
                    {renderAction(alert)}
                  </div>
                </div>
              </article>
            )
          })}
        </div>
      ) : (
        <p className="mt-3 rounded-xl border border-white/[0.06] px-4 py-8 text-center text-sm text-slate-500">No illustrative alerts match these filters.</p>
      )}

      {!compact ? (
        <section className="mt-5 border-t border-white/[0.07] pt-4" aria-label="Mission Control demo audit activity">
          <h3 className="text-xs font-semibold text-slate-300">Recent demo actions</h3>
          {audit.length ? (
            <ul className="mt-2 space-y-2">
              {audit.slice(0, 5).map((entry) => <li key={entry.id} className="text-[11px] leading-5 text-slate-500">{entry.time} · {entry.actor} {entry.action} {entry.alertId}.</li>)}
            </ul>
          ) : <p className="mt-2 text-[11px] text-slate-600">No alert actions in this browser session.</p>}
        </section>
      ) : null}

      {selectedAlert ? (
        <div className="fixed inset-0 z-[70] flex items-end justify-center bg-black/65 p-0 backdrop-blur-sm sm:items-center sm:p-5" role="presentation" onMouseDown={(event) => {
          if (event.target === event.currentTarget) setSelectedId(null)
        }}>
          <section role="dialog" aria-modal="true" aria-labelledby="mission-alert-detail-title" className="max-h-[92vh] w-full max-w-2xl overflow-y-auto rounded-t-2xl border border-white/10 bg-[#0b1a29] p-5 shadow-2xl shadow-black/50 sm:rounded-2xl sm:p-6">
            <div className="flex items-start justify-between gap-4">
              <div>
                <p className="text-xs font-medium uppercase tracking-[0.14em] text-cyan-200/80">{selectedAlert.id} · Illustrative alert</p>
                <h2 id="mission-alert-detail-title" className="mt-2 text-xl font-semibold text-slate-50">{selectedAlert.title}</h2>
              </div>
              <button type="button" onClick={() => setSelectedId(null)} className="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-xl text-slate-400 hover:bg-white/[0.06] hover:text-white" aria-label="Close alert details"><X size={18} /></button>
            </div>
            <div className="mt-5 grid grid-cols-1 gap-4 rounded-xl border border-white/[0.07] bg-white/[0.02] p-4 sm:grid-cols-2">
              <div><p className="text-xs text-slate-500">Astronaut</p><p className="mt-1 text-sm text-slate-200">{MISSION_CONTROL_ASTRONAUTS.find((item) => item.id === selectedAlert.astronautId)?.name} · {selectedAlert.astronautId}</p></div>
              <div><p className="text-xs text-slate-500">Observed at</p><p className="mt-1 text-sm text-slate-200">{formatTimestamp(selectedAlert.timestamp)} UTC</p></div>
              <div><p className="text-xs text-slate-500">Severity</p><p className="mt-1 text-sm text-slate-200">{selectedAlert.severity} · sample classification</p></div>
              <div><p className="text-xs text-slate-500">Current status</p><p className={`mt-1 text-sm font-medium ${STATUS_TONES[selectedAlert.status]}`}>{selectedAlert.status}</p></div>
            </div>
            <p className="mt-4 rounded-xl border border-amber-200/10 bg-amber-200/[0.035] p-4 text-sm leading-6 text-slate-300">{selectedAlert.explanation} This is not a diagnosis or treatment recommendation.</p>
            <div className="mt-5 flex flex-wrap gap-2">
              {renderAction(selectedAlert)}
              <Link to={`${MISSION_CONTROL_BASE_PATH}/astronauts/${selectedAlert.astronautId}`} onClick={() => setSelectedId(null)} className="inline-flex min-h-9 items-center rounded-lg px-3 text-xs text-slate-300 hover:bg-white/[0.05]">View astronaut</Link>
            </div>
          </section>
        </div>
      ) : null}
    </section>
  )
}

export default MissionAlertPanel
