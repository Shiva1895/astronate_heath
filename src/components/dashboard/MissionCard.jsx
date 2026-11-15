import { CalendarClock, Orbit, UsersRound } from 'lucide-react'
import { SAMPLE_PROFILE } from '../../data/astronautDashboard'

function MissionCard() {
  return (
    <section className="relative min-w-0 overflow-hidden rounded-2xl border border-violet-200/10 bg-gradient-to-br from-[#171b36] via-[#111c31] to-[#0b1a29] p-5">
      <div className="pointer-events-none absolute -right-10 -top-12 h-40 w-40 rounded-full bg-violet-300/[0.06] blur-2xl" />
      <div className="relative flex items-start justify-between gap-4">
        <div>
          <p className="text-xs font-medium uppercase tracking-[0.14em] text-violet-200/75">Mission information · Sample</p>
          <h3 className="mt-2 text-xl font-semibold text-slate-50">{SAMPLE_PROFILE.mission}</h3>
          <p className="mt-1 text-sm text-slate-400">Day {SAMPLE_PROFILE.missionDay} · {SAMPLE_PROFILE.missionStatus}</p>
        </div>
        <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border border-violet-200/15 bg-violet-200/[0.07] text-violet-100">
          <Orbit size={20} />
        </div>
      </div>
      <div className="relative mt-5 grid min-w-0 grid-cols-1 gap-3 border-t border-white/[0.08] pt-4 sm:grid-cols-2">
        <div className="flex min-w-0 items-center gap-2.5 text-xs text-slate-300">
          <UsersRound size={15} className="shrink-0 text-violet-200/80" />
          <span className="min-w-0">{SAMPLE_PROFILE.crew}</span>
        </div>
        <div className="flex min-w-0 items-center gap-2.5 text-xs text-slate-300">
          <CalendarClock size={15} className="shrink-0 text-violet-200/80" />
          <span className="min-w-0 break-words">Next check-in {SAMPLE_PROFILE.nextCheckIn}</span>
        </div>
      </div>
      <p className="relative mt-4 text-[11px] leading-5 text-slate-500">
        Mission details are illustrative only and are not connected to a live mission or crew roster.
      </p>
    </section>
  )
}

export default MissionCard
