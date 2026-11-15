import { MoonStar } from 'lucide-react'
import DemoBanner from '../../components/dashboard/DemoBanner'
import MissionCard from '../../components/dashboard/MissionCard'
import PageHeader from '../../components/dashboard/PageHeader'
import SleepActivityCards from '../../components/dashboard/SleepActivityCards'
import { SLEEP_ACTIVITY } from '../../data/astronautDashboard'

function SleepActivity() {
  return (
    <div className="space-y-6">
      <DemoBanner />
      <PageHeader
        eyebrow="Personal wellbeing · Illustrative"
        title="Sleep & activity"
        description="Example cards show how daily sleep and movement summaries may appear. No real activity or sleep data is collected in this demo."
      />
      <SleepActivityCards items={SLEEP_ACTIVITY} />
      <div className="grid min-w-0 grid-cols-1 gap-4 xl:grid-cols-2">
        <section className="rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-5">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl border border-violet-200/10 bg-violet-200/[0.06] text-violet-100">
            <MoonStar size={18} />
          </div>
          <h3 className="mt-4 text-base font-semibold text-slate-100">Daily summary · Sample</h3>
          <p className="mt-2 text-sm leading-6 text-slate-400">
            The sample values are designed to demonstrate progress summaries and are not intended to assess fitness, sleep disorders, or medical status.
          </p>
          <p className="mt-4 border-t border-white/[0.07] pt-4 text-xs leading-5 text-slate-500">
            Future versions require secure backend storage, validated device integration, and appropriate consent for personal health data.
          </p>
        </section>
        <MissionCard />
      </div>
    </div>
  )
}

export default SleepActivity
