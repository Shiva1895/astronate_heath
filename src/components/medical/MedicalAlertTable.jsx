import { ArrowUpRight, CircleAlert } from 'lucide-react'
import { getAstronautName } from './medicalHelpers'

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

function AlertRow({ alert, onSelect }) {
  const name = getAstronautName(alert.astronautId, alert.astronautName)
  const timestamp = new Date(alert.timestamp).toLocaleString('en-GB', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
    timeZone: 'UTC',
  })

  return (
    <tr className="border-t border-white/[0.06] align-top">
      <td className="whitespace-nowrap px-4 py-4 text-xs font-medium text-cyan-100">{alert.id}</td>
      <td className="min-w-36 px-4 py-4">
        <p className="text-sm font-medium text-slate-200">{name}</p>
        <p className="mt-1 text-xs text-slate-500">{alert.astronautId}</p>
      </td>
      <td className="min-w-56 px-4 py-4">
        <p className="text-sm font-medium leading-5 text-slate-200">{alert.title}</p>
        <p className="mt-1 max-w-sm text-xs leading-5 text-slate-500">{alert.explanation}</p>
      </td>
      <td className="whitespace-nowrap px-4 py-4 text-xs text-slate-400">{timestamp} UTC</td>
      <td className="px-4 py-4">
        <span className={`inline-flex rounded-full border px-2.5 py-1 text-[10px] font-medium ${SEVERITY_STYLES[alert.severity]}`}>{alert.severity}</span>
      </td>
      <td className={`whitespace-nowrap px-4 py-4 text-xs font-medium ${STATUS_STYLES[alert.status]}`}>{alert.status}</td>
      <td className="px-4 py-3">
        <button
          type="button"
          onClick={() => onSelect(alert)}
          className="inline-flex min-h-9 items-center gap-1 rounded-lg px-2 text-xs font-medium text-cyan-200 hover:bg-cyan-200/[0.06]"
          aria-label={`View details for ${alert.id}`}
        >
          Details <ArrowUpRight size={14} />
        </button>
      </td>
    </tr>
  )
}

function AlertCard({ alert, onSelect }) {
  const name = getAstronautName(alert.astronautId, alert.astronautName)
  const timestamp = new Date(alert.timestamp).toLocaleString('en-GB', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
    timeZone: 'UTC',
  })

  return (
    <article className="rounded-xl border border-white/[0.07] bg-white/[0.02] p-4">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="text-[11px] font-medium text-cyan-100">{alert.id} · {alert.astronautId}</p>
          <h3 className="mt-1 text-sm font-semibold leading-5 text-slate-100">{alert.title}</h3>
        </div>
        <CircleAlert size={17} className={alert.severity === 'Critical' ? 'shrink-0 text-rose-200' : 'shrink-0 text-amber-100'} />
      </div>
      <p className="mt-2 text-xs text-slate-400">{name} · {timestamp} UTC</p>
      <p className="mt-2 text-xs leading-5 text-slate-500">{alert.explanation}</p>
      <div className="mt-3 flex flex-wrap items-center gap-2">
        <span className={`rounded-full border px-2.5 py-1 text-[10px] font-medium ${SEVERITY_STYLES[alert.severity]}`}>{alert.severity}</span>
        <span className={`text-xs font-medium ${STATUS_STYLES[alert.status]}`}>{alert.status}</span>
        <button type="button" onClick={() => onSelect(alert)} className="ml-auto inline-flex min-h-9 items-center gap-1 rounded-lg px-2 text-xs font-medium text-cyan-200 hover:bg-cyan-200/[0.06]">
          Details <ArrowUpRight size={14} />
        </button>
      </div>
    </article>
  )
}

function MedicalAlertTable({ alerts, onSelect }) {
  return (
    <section className="min-w-0 overflow-hidden rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85">
      <div className="hidden overflow-x-auto lg:block">
        <table className="w-full min-w-[980px] text-left">
          <thead className="bg-white/[0.025]">
            <tr className="text-[10px] uppercase tracking-[0.12em] text-slate-500">
              <th className="px-4 py-3 font-medium">Alert ID</th>
              <th className="px-4 py-3 font-medium">Astronaut</th>
              <th className="px-4 py-3 font-medium">Alert details</th>
              <th className="px-4 py-3 font-medium">Timestamp</th>
              <th className="px-4 py-3 font-medium">Severity</th>
              <th className="px-4 py-3 font-medium">Status</th>
              <th className="px-4 py-3 font-medium">Action</th>
            </tr>
          </thead>
          <tbody>{alerts.map((alert) => <AlertRow key={alert.id} alert={alert} onSelect={onSelect} />)}</tbody>
        </table>
      </div>
      <div className="space-y-3 p-3 lg:hidden">
        {alerts.map((alert) => <AlertCard key={alert.id} alert={alert} onSelect={onSelect} />)}
      </div>
      {alerts.length === 0 ? <p className="px-5 py-10 text-center text-sm text-slate-500">No alerts match these filters.</p> : null}
    </section>
  )
}

export default MedicalAlertTable
