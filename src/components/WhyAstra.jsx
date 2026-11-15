import { motion } from 'framer-motion'
import { ArrowUpRight, Gauge, ShieldCheck, Sparkles, WifiOff } from 'lucide-react'

const principles = [
  {
    icon: Gauge,
    title: 'Mission-Aware Monitoring',
    description: 'Prioritise health insights around mission phase, crew workload and environmental risk.',
  },
  {
    icon: Sparkles,
    title: 'Intelligent Health Insights',
    description: 'Combine sensor trends, history and thresholds to reveal significant changes earlier.',
  },
  {
    icon: WifiOff,
    title: 'Resilient Offline Operation',
    description: 'Continue local monitoring during communication outages, then synchronize stored data when connectivity returns.',
  },
  {
    icon: ShieldCheck,
    title: 'Privacy and Data Security',
    description: 'Protect sensitive medical data with controlled access and mission-specific policy safeguards.',
  },
]

function WhyAstra() {
  return (
    <section id="about" className="section-shell py-16 sm:py-20">
      <div className="grid items-center gap-8 lg:grid-cols-[0.95fr_1.05fr]">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true, amount: 0.2 }}
          transition={{ duration: 0.6, ease: 'easeOut' }}
          className="relative overflow-hidden rounded-[28px] border border-white/10 bg-slate-900/60 p-3 shadow-[0_30px_80px_rgba(3,7,18,0.8)]"
        >
          <div className="why-image overflow-hidden rounded-[24px] border border-white/10 bg-slate-950/80">
            <img
              src="https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?auto=format&fit=crop&w=1200&q=80"
              alt="Earth viewed from orbit"
              className="h-full w-full object-cover"
              onError={(event) => {
                event.currentTarget.hidden = true
              }}
            />
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true, amount: 0.2 }}
          transition={{ duration: 0.6, ease: 'easeOut' }}
          className=""
        >
          <p className="eyebrow">Why ASTRA HEALTH?</p>
          <h2 className="mt-5 text-3xl font-bold tracking-[-0.05em] text-slate-50 sm:text-4xl lg:text-5xl">
            Built for the Challenges Beyond Earth.
          </h2>

          <div className="mt-8 space-y-4">
            {principles.map((item) => {
              const Icon = item.icon

              return (
                <div
                  key={item.title}
                  className="glass-panel flex items-start gap-4 p-4 sm:p-5"
                >
                  <div className="mt-1 flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl border border-cyan-400/20 bg-cyan-500/10 text-cyan-300">
                    <Icon size={22} />
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <h3 className="text-lg font-semibold text-slate-50">{item.title}</h3>
                      <ArrowUpRight size={16} className="text-slate-400" />
                    </div>
                    <p className="mt-2 text-sm leading-7 text-slate-300">{item.description}</p>
                  </div>
                </div>
              )
            })}
          </div>
        </motion.div>
      </div>
    </section>
  )
}

export default WhyAstra
