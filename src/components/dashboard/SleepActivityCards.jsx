function SleepActivityCards({ items }) {
  return (
    <section className="grid min-w-0 grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-4">
      {items.map((item) => {
        const Icon = item.icon

        return (
          <article key={item.id} className="min-w-0 rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4">
            <div className="flex items-center justify-between gap-3">
              <div className="flex h-9 w-9 items-center justify-center rounded-xl border border-white/[0.07] bg-white/[0.03] text-cyan-100">
                <Icon size={17} />
              </div>
              <span className="rounded-full border border-white/[0.08] px-2 py-1 text-[10px] text-slate-500">Sample</span>
            </div>
            <p className="mt-4 text-xs text-slate-400">{item.title}</p>
            <p className="mt-1 text-xl font-semibold text-slate-100">{item.value}</p>
            <p className="mt-1 text-xs text-slate-500">{item.detail}</p>
            <div className="mt-4 h-1.5 overflow-hidden rounded-full bg-white/[0.06]" aria-label={`${item.title} sample progress ${item.progress}%`}>
              <div className="h-full rounded-full bg-gradient-to-r from-cyan-300/80 to-violet-300/70" style={{ width: `${item.progress}%` }} />
            </div>
          </article>
        )
      })}
    </section>
  )
}

export default SleepActivityCards
