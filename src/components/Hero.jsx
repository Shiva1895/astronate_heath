import { motion } from 'framer-motion'
import { Activity, ArrowRight, HeartPulse, Play, ShieldCheck, Sparkles, Thermometer } from 'lucide-react'

const statCards = [
  { label: 'Heart rate', value: '68 bpm', icon: HeartPulse, tone: 'cyan' },
  { label: 'Oxygen saturation', value: '98%', icon: Activity, tone: 'violet' },
  { label: 'Body temperature', value: '36.9°C', icon: Thermometer, tone: 'cyan' },
]

function Hero() {
  return (
    <section id="home" className="section-shell relative overflow-hidden pt-8 pb-16 sm:pt-10 lg:pt-14 lg:pb-24">
      <div className="absolute inset-x-0 top-10 -z-10 mx-auto h-[500px] w-[500px] rounded-full bg-cyan-500/10 blur-3xl" />
      <div className="grid min-w-0 items-center gap-10 lg:grid-cols-[1.05fr_0.95fr]">
        <motion.div
          initial={{ opacity: 0, y: 28 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.7, ease: 'easeOut' }}
          className="max-w-xl"
        >
          <div className="eyebrow mb-6">
            <Sparkles size={12} />
            Astronaut Health Monitoring &amp; Early Warning System
          </div>

          <h1 className="text-[clamp(2.25rem,5.5vw,4.5rem)] font-black leading-[0.96] tracking-[-0.06em] text-slate-50">
            Monitoring Life
            <span className="block bg-gradient-to-r from-cyan-300 via-sky-400 to-violet-400 bg-clip-text text-transparent">
              Beyond Earth&apos;s Boundaries.
            </span>
          </h1>

          <p className="mt-6 max-w-lg text-base leading-8 text-slate-300 sm:text-lg">
            An intelligent astronaut health monitoring platform designed to help space missions track vital signs,
            identify unusual health patterns and support medical teams with actionable insights.
          </p>

          <div className="mt-8 flex flex-col gap-3 sm:flex-row">
            <a
              href="#platform-preview"
              className="inline-flex items-center justify-center gap-2 rounded-full bg-gradient-to-r from-cyan-400 to-violet-500 px-5 py-3.5 text-sm font-semibold text-slate-950 shadow-[0_0_30px_rgba(22,200,232,0.35)] transition hover:scale-[1.02]"
            >
              Explore Platform
              <ArrowRight size={16} />
            </a>
            <a
              href="#features"
              className="inline-flex items-center justify-center gap-2 rounded-full border border-white/15 bg-white/5 px-5 py-3.5 text-sm font-semibold text-slate-100 transition hover:border-cyan-400/40 hover:bg-cyan-500/5"
            >
              <Play size={14} className="fill-current" />
              Discover Features
            </a>
          </div>

          <div className="mt-8 flex flex-wrap items-center gap-5 text-sm text-slate-300">
            <div className="inline-flex items-center gap-2 rounded-full border border-emerald-400/30 bg-emerald-500/5 px-3 py-1.5">
              <ShieldCheck size={14} className="text-emerald-300" />
              Mission-aware monitoring.
            </div>
            <div className="inline-flex items-center gap-2 rounded-full border border-violet-400/30 bg-violet-500/5 px-3 py-1.5">
              <Activity size={14} className="text-violet-300" />
              Health data monitoring
            </div>
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, scale: 0.96 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.8, ease: 'easeOut', delay: 0.12 }}
          className="relative"
        >
          <div className="hero-visual glass-panel">
            <img
              src="https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?auto=format&fit=crop&w=1400&q=85"
              alt="Earth viewed from space"
              className="hero-earth-image"
              onError={(event) => {
                event.currentTarget.hidden = true
              }}
            />
            <div className="hero-visual-shade" />
            <div className="hero-visual-content">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <span className="rounded-full border border-cyan-300/30 bg-slate-950/60 px-3 py-1.5 text-[10px] font-medium uppercase tracking-[0.2em] text-cyan-100 backdrop-blur">
                  Illustrative mission view
                </span>
                <span className="flex items-center gap-2 rounded-full border border-emerald-300/30 bg-slate-950/60 px-3 py-1.5 text-[10px] font-medium uppercase tracking-[0.14em] text-emerald-100 backdrop-blur">
                  <span className="h-2 w-2 rounded-full bg-emerald-300" />
                  Sample monitoring status
                </span>
              </div>

              <div className="hero-monitor-panel">
                <div className="mb-3 flex items-center justify-between">
                  <div>
                    <p className="text-[10px] font-semibold uppercase tracking-[0.2em] text-cyan-100">Crew health snapshot</p>
                    <p className="mt-1 text-xs text-slate-300">Example readings · not live sensor data</p>
                  </div>
                  <div className="flex h-9 w-9 items-center justify-center rounded-xl border border-cyan-300/25 bg-cyan-300/10 text-cyan-100">
                    <Activity size={18} />
                  </div>
                </div>
                <div className="grid gap-2 sm:grid-cols-3 lg:grid-cols-2 xl:grid-cols-3">
                  {statCards.map((item) => {
                    const Icon = item.icon

                    return (
                      <div
                        key={item.label}
                        className={`metric-card ${item.tone === 'violet' ? 'from-violet-500/20 to-indigo-500/10' : 'from-cyan-500/20 to-sky-500/10'}`}
                      >
                        <div className="flex items-center gap-2 text-[10px] uppercase tracking-[0.12em] text-slate-300">
                          <Icon size={13} className="shrink-0 text-cyan-200" />
                          <span className="min-w-0 leading-snug">{item.label}</span>
                        </div>
                        <p className="mt-2 text-lg font-semibold text-slate-50 sm:text-xl">{item.value}</p>
                      </div>
                    )
                  })}
                </div>
              </div>
            </div>
          </div>
        </motion.div>
      </div>
    </section>
  )
}

export default Hero
