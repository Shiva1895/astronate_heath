import { motion } from 'framer-motion'
import { Activity, AlertTriangle, Cpu, HeartPulse, ShieldCheck, Thermometer } from 'lucide-react'

const astronauts = [
  { id: 'AST001', name: 'Alex', status: 'Stable', pulse: '68 bpm', oxygen: '98%' },
  { id: 'AST002', name: 'John', status: 'Watch', pulse: '74 bpm', oxygen: '95%' },
  { id: 'AST003', name: 'David', status: 'Stable', pulse: '66 bpm', oxygen: '99%' },
]

const bars = [64, 86, 72, 96, 78, 88, 61, 74, 92]

function DashboardPreview() {
  return (
    <section id="platform-preview" className="section-shell py-16 sm:py-20">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, amount: 0.15 }}
        transition={{ duration: 0.6, ease: 'easeOut' }}
        className="mx-auto max-w-3xl text-center"
      >
        <p className="eyebrow justify-center">Platform preview</p>
        <h2 className="mt-5 text-3xl font-bold tracking-[-0.05em] text-slate-50 sm:text-4xl lg:text-5xl">
          One Mission. One Intelligent Health View.
        </h2>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 24 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, amount: 0.15 }}
        transition={{ duration: 0.6, delay: 0.1 }}
        className="glass-panel relative mt-12 overflow-hidden p-4 sm:p-6"
      >
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top,_rgba(34,211,238,0.08),_transparent_35%)]" />

        <div className="relative grid gap-5 xl:grid-cols-[0.9fr_1.4fr]">
          <p className="demo-data-note xl:col-span-2">
            Illustrative demonstration data only — these are fictional profiles and sample readings, not real astronaut records or live sensor measurements.
          </p>
          <div className="space-y-4">
            {astronauts.map((astronaut) => (
              <div key={astronaut.id} className="rounded-2xl border border-white/10 bg-slate-950/60 p-4 shadow-inner shadow-slate-950/50">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-[10px] uppercase tracking-[0.22em] text-slate-400">{astronaut.id}</p>
                    <h3 className="mt-1 text-xl font-semibold text-slate-50">{astronaut.name}</h3>
                  </div>
                  <span
                    className={`rounded-full px-2.5 py-1 text-[10px] font-medium uppercase tracking-[0.18em] ${
                      astronaut.status === 'Watch'
                        ? 'border border-amber-400/35 bg-amber-500/10 text-amber-200'
                        : 'border border-emerald-400/35 bg-emerald-500/10 text-emerald-200'
                    }`}
                  >
                    {astronaut.status}
                  </span>
                </div>

                <div className="mt-4 grid grid-cols-2 gap-3 text-xs text-slate-300">
                  <div className="rounded-xl border border-white/10 bg-white/5 p-2.5">
                    <div className="flex items-center gap-1.5 text-slate-400">
                      <HeartPulse size={12} />
                      Pulse
                    </div>
                    <p className="mt-2 text-base font-semibold text-slate-50">{astronaut.pulse}</p>
                  </div>
                  <div className="rounded-xl border border-white/10 bg-white/5 p-2.5">
                    <div className="flex items-center gap-1.5 text-slate-400">
                      <Activity size={12} />
                      Oxygen
                    </div>
                    <p className="mt-2 text-base font-semibold text-slate-50">{astronaut.oxygen}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>

          <div className="space-y-4">
            <div className="grid gap-4 md:grid-cols-2">
              <div className="rounded-2xl border border-white/10 bg-slate-950/60 p-4">
                <div className="flex items-center justify-between">
                  <p className="text-[10px] uppercase tracking-[0.22em] text-slate-400">Heart rate</p>
                  <HeartPulse size={16} className="text-cyan-300" />
                </div>
                <div className="mt-6 flex h-24 items-end gap-2">
                  {bars.map((bar, index) => (
                    <div
                      key={`${bar}-${index}`}
                      className="w-full rounded-t-xl bg-gradient-to-t from-cyan-400 to-violet-500"
                      style={{ height: `${bar}%` }}
                    />
                  ))}
                </div>
              </div>

              <div className="rounded-2xl border border-white/10 bg-slate-950/60 p-4">
                <div className="flex items-center justify-between">
                  <p className="text-[10px] uppercase tracking-[0.22em] text-slate-400">Oxygen</p>
                  <Activity size={16} className="text-violet-300" />
                </div>
                <div className="mt-6 flex h-24 items-end gap-2">
                  {[44, 56, 68, 52, 76, 88, 82, 70, 94].map((bar, index) => (
                    <div
                      key={`${bar}-${index}`}
                      className="w-full rounded-t-xl bg-gradient-to-t from-violet-500 to-cyan-400"
                      style={{ height: `${bar}%` }}
                    />
                  ))}
                </div>
              </div>
            </div>

            <div className="grid gap-4 lg:grid-cols-[1.1fr_0.9fr]">
              <div className="rounded-2xl border border-white/10 bg-slate-950/60 p-4">
                <div className="flex items-center justify-between">
                  <p className="text-[10px] uppercase tracking-[0.22em] text-slate-400">Temperature</p>
                  <Thermometer size={16} className="text-cyan-300" />
                </div>
                <div className="mt-4 space-y-3">
                  <div className="flex items-center justify-between rounded-xl border border-white/10 bg-white/5 px-3 py-2 text-sm">
                    <span className="text-slate-300">Core body</span>
                    <span className="font-semibold text-slate-50">36.9°C</span>
                  </div>
                  <div className="flex items-center justify-between rounded-xl border border-white/10 bg-white/5 px-3 py-2 text-sm">
                    <span className="text-slate-300">Suit temp</span>
                    <span className="font-semibold text-slate-50">24.4°C</span>
                  </div>
                  <div className="flex items-center justify-between rounded-xl border border-white/10 bg-white/5 px-3 py-2 text-sm">
                    <span className="text-slate-300">Variance</span>
                    <span className="font-semibold text-emerald-300">+0.2°C</span>
                  </div>
                </div>
              </div>

              <div className="rounded-2xl border border-white/10 bg-slate-950/60 p-4">
                <div className="flex items-center justify-between">
                  <p className="text-[10px] uppercase tracking-[0.22em] text-slate-400">Alerts</p>
                  <AlertTriangle size={16} className="text-amber-300" />
                </div>
                <div className="mt-4 space-y-3">
                  <div className="rounded-xl border border-amber-400/25 bg-amber-500/8 p-3 text-sm">
                    <p className="font-medium text-amber-200">Example hydration alert</p>
                    <p className="mt-1 text-slate-300">Sample priority: review</p>
                  </div>
                  <div className="rounded-xl border border-cyan-400/25 bg-cyan-500/8 p-3 text-sm">
                    <p className="font-medium text-cyan-200">Example data sync</p>
                    <p className="mt-1 text-slate-300">Illustrative status</p>
                  </div>
                </div>
              </div>
            </div>

            <div className="rounded-2xl border border-white/10 bg-slate-950/60 p-4">
              <div className="flex items-center justify-between">
                <p className="text-[10px] uppercase tracking-[0.22em] text-slate-400">Mission health overview</p>
                <ShieldCheck size={16} className="text-emerald-300" />
              </div>
              <div className="mt-4 grid gap-3 sm:grid-cols-3">
                <div className="rounded-xl border border-white/10 bg-white/5 p-3">
                  <p className="text-[10px] uppercase tracking-[0.2em] text-slate-400">Crew status</p>
                  <p className="mt-2 text-2xl font-bold text-slate-50">98%</p>
                </div>
                <div className="rounded-xl border border-white/10 bg-white/5 p-3">
                  <p className="text-[10px] uppercase tracking-[0.2em] text-slate-400">Link status (sample)</p>
                  <p className="mt-2 text-2xl font-bold text-slate-50">Available</p>
                </div>
                <div className="rounded-xl border border-white/10 bg-white/5 p-3">
                  <p className="text-[10px] uppercase tracking-[0.2em] text-slate-400">Devices listed</p>
                  <div className="mt-3 flex items-center gap-2 text-slate-50">
                    <Cpu size={16} className="text-cyan-300" />
                    12 (sample)
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </motion.div>
    </section>
  )
}

export default DashboardPreview
