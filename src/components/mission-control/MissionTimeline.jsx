import { Clock3 } from 'lucide-react'
import { MISSION_CONTROL_TIMELINE } from '../../data/missionControlMockData'

const CATEGORY_TONES = {
  Mission: 'border-cyan-200/15 bg-cyan-200/[0.05] text-cyan-100',
  'Medical review': 'border-violet-200/15 bg-violet-200/[0.05] text-violet-100',
  Device: 'border-amber-200/15 bg-amber-200/[0.05] text-amber-100',
  Alert: 'border-rose-200/15 bg-rose-200/[0.05] text-rose-100',
  Synchronization: 'border-emerald-200/15 bg-emerald-200/[0.05] text-emerald-100',
}

function MissionTimeline({ compact = false }) {
  const events = compact ? MISSION_CONTROL_TIMELINE.slice(-4).reverse() : [...MISSION_CONTROL_TIMELINE].reverse()

  return (
    <section className="min-w-0 rounded-2xl border border-white/[0.08] bg-[#0b1a29]/75 p-4 sm:p-5" aria-labelledby="mission-timeline-title">
      <div>
        <p className="text-xs font-semibold uppercase tracking-[0.14em] text-cyan-200/75">Mission events · Sample</p>
        <h2 id="mission-timeline-title" className="mt-2 text-lg font-semibold text-slate-50">Mission Timeline</h2>
      </div>
      <ol className="mt-4 space-y-0">
        {events.map((event, index) => (
          <li key={event.id} className="relative flex gap-3 pb-5 last:pb-0">
            {index < events.length - 1 ? <span className="absolute bottom-0 left-[15px] top-8 w-px bg-white/[0.08]" aria-hidden="true" /> : null}
            <span className="relative z-10 mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-full border border-cyan-200/10 bg-[#0b1a29] text-cyan-100"><Clock3 size={14} /></span>
            <div className="min-w-0 flex-1 rounded-xl border border-white/[0.06] bg-white/[0.02] p-3">
              <div className="flex flex-wrap items-start justify-between gap-2">
                <h3 className="text-xs font-semibold leading-5 text-slate-200">{event.title}</h3>
                <span className={`rounded-full border px-2 py-0.5 text-[10px] ${CATEGORY_TONES[event.category]}`}>{event.category}</span>
              </div>
              <p className="mt-1 text-[10px] text-slate-500">{event.time}</p>
              <p className="mt-2 text-xs leading-5 text-slate-400">{event.description}</p>
            </div>
          </li>
        ))}
      </ol>
    </section>
  )
}

export default MissionTimeline
