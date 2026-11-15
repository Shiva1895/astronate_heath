import { ArrowRight, ShieldCheck } from 'lucide-react'
import { Link, useNavigate, useOutletContext } from 'react-router-dom'
import AstronautCard from '../../components/medical/AstronautCard'
import MedicalAlertTable from '../../components/medical/MedicalAlertTable'
import MedicalSummaryCard from '../../components/medical/MedicalSummaryCard'
import PageHeader from '../../components/dashboard/PageHeader'
import { MEDICAL_OVERVIEW_SUMMARIES } from '../../data/medicalMockData'

function MedicalOverview() {
  const { astronauts, alerts } = useOutletContext()
  const navigate = useNavigate()
  const summaries = MEDICAL_OVERVIEW_SUMMARIES.map((summary) => {
    if (summary.id === 'assigned') return { ...summary, value: String(astronauts.length).padStart(2, '0'), detail: 'Assigned backend profiles' }
    if (summary.id === 'monitoring') return { ...summary, value: String(astronauts.filter((astronaut) => astronaut.monitoring === 'ACTIVE').length).padStart(2, '0'), detail: 'Active profile status' }
    if (summary.id === 'review') return { ...summary, value: String(alerts.filter((alert) => alert.status === 'New').length).padStart(2, '0'), detail: 'Open backend alerts' }
    return { ...summary, value: String(alerts.filter((alert) => alert.status === 'Acknowledged').length).padStart(2, '0'), detail: 'Acknowledged backend alerts' }
  })

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Medical Officer workspace"
        title="Medical Operations Overview"
        description="Monitor assigned astronauts and review health workflow information authorized by the backend."
        action={
          <span className="inline-flex min-h-10 items-center gap-2 rounded-xl border border-violet-200/10 bg-violet-200/[0.045] px-3 text-xs font-medium text-violet-100">
            <ShieldCheck size={15} /> Backend-authorized session
          </span>
        }
      />

      <section className="grid min-w-0 grid-cols-1 gap-3 sm:grid-cols-2 2xl:grid-cols-4" aria-label="Medical operations summaries">
        {summaries.map((summary) => <MedicalSummaryCard key={summary.id} summary={summary} />)}
      </section>

      <section aria-labelledby="assigned-astronauts-heading">
        <div className="mb-3 flex flex-wrap items-end justify-between gap-3">
          <div>
            <h2 id="assigned-astronauts-heading" className="text-lg font-semibold text-slate-100">Assigned Astronauts</h2>
            <p className="mt-1 text-xs text-slate-500">Assigned roster · {astronauts.length} shown</p>
          </div>
          <Link to="/medical/dashboard/astronauts" className="inline-flex min-h-9 items-center gap-1 rounded-lg px-2 text-xs font-medium text-cyan-200 hover:bg-cyan-200/[0.06]">
            View roster <ArrowRight size={14} />
          </Link>
        </div>
        {astronauts.length ? (
          <div className="grid min-w-0 grid-cols-1 gap-3 md:grid-cols-2 2xl:grid-cols-4">
            {astronauts.map((astronaut) => <AstronautCard key={astronaut.id} astronaut={astronaut} />)}
          </div>
        ) : (
          <p className="rounded-2xl border border-white/[0.08] bg-[#0b1a29]/70 p-6 text-sm text-slate-400">No astronauts are assigned to your account.</p>
        )}
      </section>

      <section aria-labelledby="priority-alerts-heading">
        <div className="mb-3 flex flex-wrap items-end justify-between gap-3">
          <div>
            <h2 id="priority-alerts-heading" className="text-lg font-semibold text-slate-100">Alerts requiring attention</h2>
            <p className="mt-1 text-xs text-slate-500">Operational flags for review; not diagnoses</p>
          </div>
          <Link to="/medical/dashboard/alerts" className="inline-flex min-h-9 items-center gap-1 rounded-lg px-2 text-xs font-medium text-cyan-200 hover:bg-cyan-200/[0.06]">
            Manage alerts <ArrowRight size={14} />
          </Link>
        </div>
        <MedicalAlertTable
          alerts={alerts.filter((alert) => !['Resolved', 'Closed'].includes(alert.status)).slice(0, 4)}
          onSelect={(alert) => navigate(`/medical/dashboard/alerts?focus=${alert.id}`)}
        />
      </section>
    </div>
  )
}

export default MedicalOverview
