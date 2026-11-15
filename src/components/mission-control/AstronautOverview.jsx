import { useMemo, useState } from 'react'
import { ArrowUpRight, CircleAlert, Search, UserRound } from 'lucide-react'
import { Link } from 'react-router-dom'
import {
  MISSION_CONTROL_ALERTS,
  MISSION_CONTROL_ASTRONAUTS,
  MISSION_CONTROL_BASE_PATH,
} from '../../data/missionControlMockData'

const HEALTH_TONES = {
  Normal: 'border-emerald-200/15 bg-emerald-200/[0.05] text-emerald-100',
  Review: 'border-amber-200/15 bg-amber-200/[0.05] text-amber-100',
  Standby: 'border-slate-200/10 bg-white/[0.03] text-slate-300',
}

function AstronautOverview({ astronauts = MISSION_CONTROL_ASTRONAUTS, alerts = MISSION_CONTROL_ALERTS, compact = false }) {
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState('All')
  const filteredAstronauts = useMemo(() => astronauts.filter((astronaut) => {
    const matchesText = `${astronaut.name} ${astronaut.id} ${astronaut.mission}`.toLowerCase().includes(query.trim().toLowerCase())
    return matchesText && (status === 'All' || astronaut.monitoringStatus === status)
  }), [astronauts, query, status])
  const visibleAstronauts = (compact ? filteredAstronauts.slice(0, 4) : filteredAstronauts).map((astronaut) => ({
    ...astronaut,
    unresolvedAlerts: alerts.filter((alert) => alert.astronautId === astronaut.id && alert.status !== 'Resolved').length,
  }))

  return (
    <section className="min-w-0 rounded-2xl border border-white/[0.08] bg-[#0b1a29]/75 p-4 sm:p-5" aria-labelledby="astronaut-overview-title">
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.14em] text-cyan-200/75">Crew status · Illustrative</p>
          <h2 id="astronaut-overview-title" className="mt-2 text-lg font-semibold text-slate-50">Astronaut Monitoring</h2>
          <p className="mt-1 text-xs leading-5 text-slate-500">Fictional astronaut profiles and simulated health summaries.</p>
        </div>
        <div className="grid grid-cols-1 gap-2 sm:grid-cols-[minmax(12rem,1fr)_auto]">
          <label className="relative min-w-0">
            <span className="sr-only">Search astronauts</span>
            <Search size={15} className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
            <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search name or ID" className="min-h-10 w-full rounded-xl border border-white/10 bg-[#071522] pl-9 pr-3 text-xs text-slate-100 outline-none placeholder:text-slate-600 focus:border-cyan-200/35" />
          </label>
          <label>
            <span className="sr-only">Filter by monitoring status</span>
            <select value={status} onChange={(event) => setStatus(event.target.value)} className="min-h-10 w-full rounded-xl border border-white/10 bg-[#071522] px-3 text-xs text-slate-100 outline-none focus:border-cyan-200/35">
              <option value="All">All monitoring</option>
              <option value="Active">Active</option>
              <option value="Standby">Standby</option>
            </select>
          </label>
        </div>
      </div>

      {visibleAstronauts.length ? (
        <div className="mt-4 grid min-w-0 grid-cols-1 gap-3 md:grid-cols-2 2xl:grid-cols-4">
          {visibleAstronauts.map((astronaut) => (
            <article key={astronaut.id} className="min-w-0 rounded-xl border border-white/[0.07] bg-white/[0.02] p-4">
              <div className="flex min-w-0 items-center gap-3">
                <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full border border-cyan-200/15 bg-cyan-200/[0.06] text-xs font-semibold text-cyan-100">{astronaut.avatar || <UserRound size={17} />}</span>
                <div className="min-w-0 flex-1">
                  <h3 className="truncate text-sm font-semibold text-slate-100">{astronaut.name}</h3>
                  <p className="mt-0.5 text-xs text-slate-500">{astronaut.id} · {astronaut.mission}</p>
                </div>
                <span className={`shrink-0 rounded-full border px-2 py-1 text-[10px] ${HEALTH_TONES[astronaut.healthStatus] || HEALTH_TONES.Standby}`}>{astronaut.healthStatus}</span>
              </div>
              <div className="mt-4 grid grid-cols-2 gap-2">
                <div className="rounded-lg bg-white/[0.025] p-2.5">
                  <p className="text-[10px] text-slate-500">Heart rate · sample</p>
                  <p className="mt-1 text-sm font-semibold text-slate-100">{astronaut.heartRate} <span className="text-[10px] font-normal text-slate-500">BPM</span></p>
                </div>
                <div className="rounded-lg bg-white/[0.025] p-2.5">
                  <p className="text-[10px] text-slate-500">SpO₂ · sample</p>
                  <p className="mt-1 text-sm font-semibold text-slate-100">{astronaut.spo2}<span className="text-[10px] font-normal text-slate-500">%</span></p>
                </div>
              </div>
              <div className="mt-3 flex flex-wrap items-center justify-between gap-2 text-[11px] text-slate-500">
                <span>{astronaut.monitoringStatus} monitoring · {astronaut.latestUpdate}</span>
                <span className="inline-flex items-center gap-1.5"><CircleAlert size={13} /> {astronaut.unresolvedAlerts} unresolved</span>
              </div>
              <Link to={`${MISSION_CONTROL_BASE_PATH}/astronauts/${astronaut.id}`} className="mt-3 inline-flex min-h-10 w-full items-center justify-center gap-2 rounded-lg border border-white/[0.08] text-xs font-medium text-cyan-100 hover:border-cyan-200/20 hover:bg-cyan-200/[0.04]">
                View details <ArrowUpRight size={14} />
              </Link>
            </article>
          ))}
        </div>
      ) : (
        <p className="mt-4 rounded-xl border border-white/[0.06] px-4 py-8 text-center text-sm text-slate-500">No sample astronauts match the current filters.</p>
      )}
    </section>
  )
}

export default AstronautOverview
