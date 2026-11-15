import { ArrowUpRight, CircleAlert } from 'lucide-react'
import { Link } from 'react-router-dom'

const ALERT_TONES = {
  critical: 'text-rose-200',
  warning: 'text-amber-100',
  info: 'text-cyan-100',
  neutral: 'text-slate-400',
}

function AstronautCard({ astronaut }) {
  return (
    <article className="min-w-0 rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4 transition hover:border-cyan-200/15 sm:p-5">
      <div className="flex items-start justify-between gap-3">
        <div className="flex min-w-0 items-center gap-3">
          <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border border-cyan-200/10 bg-cyan-200/[0.06] text-xs font-semibold text-cyan-100">
            {astronaut.avatar}
          </span>
          <div className="min-w-0">
            <h3 className="truncate text-sm font-semibold text-slate-100">{astronaut.name}</h3>
            <p className="mt-1 text-xs text-slate-500">Profile #{astronaut.id}</p>
          </div>
        </div>
        <span className={`inline-flex shrink-0 items-center gap-1.5 rounded-full border border-emerald-200/10 bg-emerald-200/[0.05] px-2.5 py-1 text-[10px] font-medium ${astronaut.monitoring === 'Active' ? 'text-emerald-200' : 'text-slate-300'}`}>
          <span className={`h-1.5 w-1.5 rounded-full ${astronaut.monitoring === 'Active' ? 'bg-emerald-300' : 'bg-slate-400'}`} />
          {astronaut.monitoring}
        </span>
      </div>
      <div className="mt-4 grid grid-cols-2 gap-3 border-t border-white/[0.06] pt-4">
        <div className="min-w-0">
          <p className="text-[10px] uppercase tracking-[0.12em] text-slate-500">Mission</p>
          <p className="mt-1 truncate text-xs font-medium text-slate-200">{astronaut.mission}</p>
        </div>
        <div className="min-w-0">
          <p className="text-[10px] uppercase tracking-[0.12em] text-slate-500">Latest sample</p>
          <p className="mt-1 truncate text-xs font-medium text-slate-200">{astronaut.latestUpdate}</p>
        </div>
      </div>
      <div className="mt-3 flex items-center justify-between gap-2">
        <p className={`flex min-w-0 items-center gap-1.5 truncate text-xs ${ALERT_TONES[astronaut.alertTone]}`}>
          <CircleAlert size={14} className="shrink-0" />
          {astronaut.alertSummary}
        </p>
        <Link
          to={`/medical/dashboard/astronauts/${astronaut.id}`}
          className="inline-flex min-h-9 shrink-0 items-center gap-1 rounded-lg px-2 text-xs font-medium text-cyan-200 hover:bg-cyan-200/[0.06] hover:text-cyan-100"
        >
          Details <ArrowUpRight size={14} />
        </Link>
      </div>
      <p className="mt-1 text-[10px] text-slate-600">Assigned profile summary returned by the backend.</p>
    </article>
  )
}

export default AstronautCard
