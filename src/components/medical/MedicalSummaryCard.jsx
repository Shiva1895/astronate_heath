function MedicalSummaryCard({ summary }) {
  const Icon = summary.icon
  const tones = {
    cyan: 'border-cyan-200/10 bg-cyan-200/[0.06] text-cyan-100',
    mint: 'border-emerald-200/10 bg-emerald-200/[0.06] text-emerald-100',
    amber: 'border-amber-200/10 bg-amber-200/[0.06] text-amber-100',
    violet: 'border-violet-200/10 bg-violet-200/[0.06] text-violet-100',
  }

  return (
    <article className="min-w-0 rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4 sm:p-5">
      <div className="flex items-start justify-between gap-3">
        <div className={`flex h-10 w-10 items-center justify-center rounded-xl border ${tones[summary.tone]}`}>
          <Icon size={19} />
        </div>
        <span className="rounded-full border border-white/[0.08] px-2 py-1 text-[10px] font-medium uppercase tracking-[0.08em] text-slate-500">Sample</span>
      </div>
      <p className="mt-4 text-sm font-medium text-slate-400">{summary.label}</p>
      <p className="mt-1 text-3xl font-semibold tracking-[-0.04em] text-slate-50">{summary.value}</p>
      <p className="mt-2 text-xs text-slate-500">{summary.detail}</p>
    </article>
  )
}

export default MedicalSummaryCard
