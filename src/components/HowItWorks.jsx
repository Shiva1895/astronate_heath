import { motion } from 'framer-motion'
import { ArrowDown, BellRing, BrainCircuit, Database, HeartPulse, ShieldCheck } from 'lucide-react'

const steps = [
  {
    icon: Database,
    title: 'Data Collection',
    description: 'Designed to receive health measurements from wearable sensors and medical devices.',
  },
  {
    icon: BrainCircuit,
    title: 'Data Processing',
    description: 'Incoming measurements are validated and organised for review.',
  },
  {
    icon: ActivityIcon,
    title: 'Intelligent Analysis',
    description: 'Health rules and analytical models flag unusual patterns for medical review.',
  },
  {
    icon: BellRing,
    title: 'Alert Generation',
    description: 'The system creates appropriately prioritised alerts.',
  },
  {
    icon: ShieldCheck,
    title: 'Medical Review',
    description: 'Qualified medical professionals review the information and decide what action is needed.',
  },
]

function ActivityIcon() {
  return <HeartPulse size={22} />
}

function HowItWorks() {
  return (
    <section id="how-it-works" className="section-shell py-16 sm:py-20">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, amount: 0.2 }}
        transition={{ duration: 0.6, ease: 'easeOut' }}
        className="mx-auto max-w-3xl text-center"
      >
        <p className="eyebrow justify-center">How it works</p>
        <h2 className="mt-5 text-3xl font-bold tracking-[-0.05em] text-slate-50 sm:text-4xl lg:text-5xl">
          From Body Signals to Informed Decisions.
        </h2>
      </motion.div>

      <div className="relative mt-14">
        <div className="absolute left-0 right-0 top-8 hidden h-px bg-gradient-to-r from-transparent via-cyan-400/60 to-transparent md:block" />

        <div className="grid gap-6 xl:grid-cols-5">
          {steps.map((step, index) => {
            const Icon = step.icon

            return (
              <motion.div
                key={step.title}
                initial={{ opacity: 0, y: 24 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true, amount: 0.15 }}
                transition={{ duration: 0.45, delay: index * 0.08 }}
                className="process-step glass-panel relative p-5"
              >
                <div className="mb-5 flex items-center justify-between">
                  <div className="flex h-10 w-10 items-center justify-center rounded-xl border border-cyan-400/25 bg-cyan-500/10 text-cyan-300">
                    <Icon size={20} />
                  </div>
                  <span className="text-xs font-medium uppercase tracking-[0.25em] text-slate-400">0{index + 1}</span>
                </div>
                <h3 className="text-base font-semibold uppercase tracking-[0.14em] text-slate-50">{step.title}</h3>
                <p className="mt-3 text-sm leading-7 text-slate-300">{step.description}</p>
                {index < steps.length - 1 && (
                  <div className="mt-4 flex justify-end xl:hidden">
                    <ArrowDown size={16} className="text-cyan-300" />
                  </div>
                )}
              </motion.div>
            )
          })}
        </div>
      </div>
    </section>
  )
}

export default HowItWorks
