import { motion } from 'framer-motion'
import {
  Activity,
  BellRing,
  BrainCircuit,
  LayoutDashboard,
  ShieldCheck,
  TrendingUp,
  Users,
  WifiOff,
} from 'lucide-react'

const features = [
  {
    icon: Activity,
    title: 'Health Data Monitoring',
    description: 'Monitor important health measurements such as heart rate, oxygen saturation, body temperature and breathing rate.',
  },
  {
    icon: BrainCircuit,
    title: 'AI-Powered Pattern Analysis',
    description: 'Analyse historical measurements and identify unusual health patterns for medical review.',
  },
  {
    icon: BellRing,
    title: 'Smart Medical Alerts',
    description: 'Generate configurable alerts when health readings require attention.',
  },
  {
    icon: LayoutDashboard,
    title: 'Medical Command Dashboard',
    description: 'Provide doctors with a centralised view of astronaut health information.',
  },
  {
    icon: TrendingUp,
    title: 'Historical Health Analytics',
    description: 'Compare current readings with personal baselines and previous measurements.',
  },
  {
    icon: WifiOff,
    title: 'Offline-First Monitoring',
    description: 'Designed to continue local monitoring during communication outages, then synchronize stored data when connectivity returns.',
  },
  {
    icon: Users,
    title: 'Mission and Crew Management',
    description: 'Organise astronaut profiles, missions, assigned medical officers and sensor devices.',
  },
  {
    icon: ShieldCheck,
    title: 'Secure Medical Records',
    description: 'Protect sensitive astronaut information with controlled access and clear audit awareness.',
  },
]

function FeatureSection() {
  return (
    <section id="features" className="section-shell py-16 sm:py-20">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, amount: 0.15 }}
        transition={{ duration: 0.6, ease: 'easeOut' }}
        className="mx-auto max-w-3xl text-center"
      >
        <p className="eyebrow justify-center">Core features</p>
        <h2 className="mt-5 text-3xl font-bold tracking-[-0.05em] text-slate-50 sm:text-4xl lg:text-5xl">
          Intelligence That Watches Over Every Mission.
        </h2>
      </motion.div>

      <div className="mt-12 grid gap-5 min-[600px]:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {features.map((feature, index) => {
          const Icon = feature.icon

          return (
            <motion.article
              key={feature.title}
              initial={{ opacity: 0, y: 24 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, amount: 0.15 }}
              transition={{ duration: 0.45, delay: index * 0.06 }}
              whileHover={{ y: -4 }}
              className="feature-card glass-panel flex h-full flex-col p-6"
            >
              <div className="mb-5 flex h-12 w-12 items-center justify-center rounded-2xl border border-cyan-400/25 bg-cyan-500/8 text-cyan-300 shadow-[0_0_25px_rgba(22,200,232,0.18)]">
                <Icon size={22} />
              </div>
              <h3 className="text-lg font-semibold leading-7 text-slate-50">{feature.title}</h3>
              <p className="mt-3 text-sm leading-7 text-slate-300">{feature.description}</p>
            </motion.article>
          )
        })}
      </div>
    </section>
  )
}

export default FeatureSection
