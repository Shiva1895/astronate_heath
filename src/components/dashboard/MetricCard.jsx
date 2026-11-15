import { ArrowDownRight, ArrowUpRight, Minus } from 'lucide-react'

const TONES = {
  cyan: 'border-cyan-200/10 bg-cyan-200/[0.06] text-cyan-100',
  violet: 'border-violet-200/10 bg-violet-200/[0.06] text-violet-100',
  blue: 'border-sky-200/10 bg-sky-200/[0.06] text-sky-100',
  mint: 'border-emerald-200/10 bg-emerald-200/[0.06] text-emerald-100',
}

function MetricCard({ metric }) {
  const Icon = metric.icon
  const TrendIcon = metric.direction === 'up' ? ArrowUpRight : metric.direction === 'down' ? ArrowDownRight : Minus

  return (
    <article className="min-w-0 rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4 shadow-[0_12px_36px_rgba(0,0,0,0.12)] sm:p-5">
      <div className="flex items-start justify-between gap-3">
        <div className={`flex h-10 w-10 items-center justify-center rounded-xl border ${TONES[metric.tone]}`}>
          <Icon size={19} />
        </div>
        <span className="rounded-full border border-white/[0.08] px-2 py-1 text-[10px] font-medium uppercase tracking-[0.08em] text-slate-500">
          Sample
        </span>
      </div>
      <p className="mt-4 text-sm font-medium text-slate-400">{metric.title}</p>
      <div className="mt-1 flex items-baseline gap-2">
        <span className="text-3xl font-semibold tracking-[-0.04em] text-slate-50">{metric.value}</span>
        <span className="text-xs text-slate-500">{metric.unit}</span>
      </div>
      <div className="mt-3 flex min-h-5 items-center gap-1.5 text-xs text-slate-400">
        <TrendIcon size={14} className="shrink-0 text-cyan-200/80" />
        <span className="truncate">{metric.trend}</span>
      </div>
      <p className="mt-3 border-t border-white/[0.06] pt-3 text-[11px] text-slate-500">
        Updated {metric.updated} · simulated
      </p>
    </article>
  )
}

export default MetricCard
