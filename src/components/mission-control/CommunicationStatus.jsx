import { Activity, Clock3, Radio } from 'lucide-react'
import { MISSION_CONTROL_COMMUNICATIONS } from '../../data/missionControlMockData'

const STATUS_TONES = {
  Connected: 'border-emerald-200/15 bg-emerald-200/[0.05] text-emerald-100',
  Delayed: 'border-amber-200/15 bg-amber-200/[0.05] text-amber-100',
  Disconnected: 'border-rose-200/15 bg-rose-200/[0.05] text-rose-100',
}

function CommunicationStatus({ compact = false }) {
  const channels = compact ? MISSION_CONTROL_COMMUNICATIONS.slice(0, 3) : MISSION_CONTROL_COMMUNICATIONS

  return (
    <section className="min-w-0 rounded-2xl border border-white/[0.08] bg-[#0b1a29]/75 p-4 sm:p-5" aria-labelledby="communication-status-title">
      <div className="flex items-start gap-3">
        <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl border border-cyan-200/10 bg-cyan-200/[0.05] text-cyan-100"><Radio size={18} /></span>
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.14em] text-cyan-200/75">Communications · Sample</p>
          <h2 id="communication-status-title" className="mt-1 text-base font-semibold text-slate-50">Communication Status</h2>
        </div>
      </div>
      <div className="mt-4 space-y-2">
        {channels.map((channel) => {
          const Icon = channel.icon
          return (
            <article key={channel.id} className="flex min-w-0 items-center gap-3 rounded-xl border border-white/[0.06] bg-white/[0.02] p-3">
              <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-white/[0.035] text-slate-300"><Icon size={16} /></span>
              <div className="min-w-0 flex-1">
                <h3 className="truncate text-xs font-medium text-slate-200">{channel.name}</h3>
                <p className="mt-1 flex items-center gap-1 text-[10px] text-slate-500"><Clock3 size={11} /> {channel.detail}</p>
              </div>
              <span className={`shrink-0 rounded-full border px-2 py-1 text-[10px] ${STATUS_TONES[channel.status]}`}>{channel.status}</span>
            </article>
          )
        })}
      </div>
      <p className="mt-3 flex items-start gap-2 text-[10px] leading-5 text-slate-600"><Activity size={13} className="mt-0.5 shrink-0" /> Demonstration statuses only; no spacecraft or external communication systems are connected.</p>
    </section>
  )
}

export default CommunicationStatus
