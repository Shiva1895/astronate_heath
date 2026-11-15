import { ArrowRight, Check, Clock3, ShieldAlert } from 'lucide-react'
import { Link } from 'react-router-dom'

const SEVERITY_STYLES = {
  Critical: 'border-rose-300/15 bg-rose-300/[0.07] text-rose-200',
  Warning: 'border-amber-200/15 bg-amber-200/[0.06] text-amber-100',
  Information: 'border-sky-200/15 bg-sky-200/[0.06] text-sky-100',
}

const STATUS_STYLES = {
  New: 'text-cyan-200',
  Acknowledged: 'text-amber-200',
  Resolved: 'text-emerald-200',
}

function AlertPanel({ alerts, onStatusChange, compact = false }) {
  return (
    <section className="min-w-0 rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85">
      <div className="flex items-center justify-between gap-3 border-b border-white/[0.07] px-4 py-4 sm:px-5">
        <div className="flex min-w-0 items-center gap-3">
          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl border border-amber-200/10 bg-amber-200/[0.06] text-amber-100">
            <ShieldAlert size={17} />
          </div>
          <div className="min-w-0">
            <h3 className="truncate text-sm font-semibold text-slate-100">Alert center</h3>
            <p className="mt-0.5 text-xs text-slate-500">Illustrative, non-diagnostic notices</p>
          </div>
        </div>
        <Link
          to="/astronaut/dashboard/alerts"
          className="inline-flex min-h-9 shrink-0 items-center gap-1 rounded-lg px-2 text-xs font-medium text-cyan-200 hover:bg-cyan-200/[0.06] hover:text-cyan-100"
        >
          View all <ArrowRight size={14} />
        </Link>
      </div>

      <ul className="divide-y divide-white/[0.06]">
        {(compact ? alerts.slice(0, 2) : alerts).map((alert) => (
          <li key={alert.id} className="px-4 py-4 sm:px-5">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
              <div className="min-w-0">
                <div className="flex flex-wrap items-center gap-2">
                  <span className={`rounded-full border px-2 py-1 text-[10px] font-medium ${SEVERITY_STYLES[alert.severity]}`}>
                    {alert.severity}
                  </span>
                  <span className={`text-[11px] font-medium ${STATUS_STYLES[alert.status]}`}>{alert.status}</span>
                </div>
                <h4 className="mt-2 text-sm font-medium leading-5 text-slate-100">{alert.title}</h4>
                <p className="mt-1 max-w-2xl text-xs leading-5 text-slate-400">{alert.description}</p>
                <p className="mt-2 flex items-center gap-1.5 text-[11px] text-slate-500">
                  <Clock3 size={12} /> {alert.time}
                </p>
              </div>
              {onStatusChange && alert.status !== 'Resolved' ? (
                <button
                  type="button"
                  onClick={() => onStatusChange(alert.id, alert.status === 'New' ? 'Acknowledged' : 'Resolved')}
                  className="inline-flex min-h-9 shrink-0 items-center justify-center gap-1.5 self-start rounded-lg border border-white/10 px-3 text-xs font-medium text-slate-300 hover:border-cyan-200/20 hover:bg-cyan-200/[0.05] hover:text-cyan-100"
                  aria-label={`${alert.status === 'New' ? 'Acknowledge' : 'Resolve'} ${alert.title}`}
                >
                  <Check size={14} />
                  {alert.status === 'New' ? 'Acknowledge' : 'Resolve'}
                </button>
              ) : null}
            </div>
          </li>
        ))}
      </ul>
      {alerts.length === 0 ? <p className="px-5 py-8 text-center text-sm text-slate-500">No illustrative alerts to show.</p> : null}
    </section>
  )
}

export default AlertPanel
