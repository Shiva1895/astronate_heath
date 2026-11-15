import { motion } from 'framer-motion'
import { Activity, MoonStar, Satellite } from 'lucide-react'

const challenges = [
  {
    icon: Satellite,
    title: 'Distance from Earth',
    description: 'Medical support is difficult to access during deep-space missions, so health systems must support autonomy and early awareness.',
  },
  {
    icon: Activity,
    title: 'Microgravity',
    description: 'Spaceflight can affect cardiovascular health, muscles and bones, making continuous monitoring essential to mission readiness.',
  },
  {
    icon: MoonStar,
    title: 'Isolation',
    description: 'Long missions can influence sleep, mood and psychological stability, requiring sustained wellbeing support and assessment.',
  },
]

function MissionSection() {
  return (
    <section id="mission" className="section-shell py-16 sm:py-20">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, amount: 0.2 }}
        transition={{ duration: 0.6, ease: 'easeOut' }}
        className="mx-auto max-w-3xl text-center"
      >
        <p className="eyebrow justify-center">Mission introduction</p>
        <h2 className="mt-5 text-3xl font-bold tracking-[-0.05em] text-slate-50 sm:text-4xl lg:text-5xl">
          Human Exploration Deserves Intelligent Protection.
        </h2>
      </motion.div>

      <div className="mt-12 grid gap-6 md:grid-cols-3">
        {challenges.map((challenge, index) => {
          const Icon = challenge.icon

          return (
            <motion.article
              key={challenge.title}
              initial={{ opacity: 0, y: 18 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, amount: 0.2 }}
              transition={{ duration: 0.5, delay: index * 0.12 }}
              className="glass-panel relative overflow-hidden p-6 sm:p-7"
            >
              <div className="absolute inset-0 bg-gradient-to-br from-cyan-500/5 via-transparent to-violet-500/5" />
              <div className="relative">
                <div className="mb-5 flex h-14 w-14 items-center justify-center rounded-2xl border border-cyan-400/25 bg-cyan-500/10 text-cyan-300 shadow-[0_0_25px_rgba(22,200,232,0.2)]">
                  <Icon size={28} />
                </div>
                <h3 className="text-xl font-semibold text-slate-50">{challenge.title}</h3>
                <p className="mt-3 text-sm leading-7 text-slate-300">{challenge.description}</p>
              </div>
            </motion.article>
          )
        })}
      </div>
    </section>
  )
}

export default MissionSection
