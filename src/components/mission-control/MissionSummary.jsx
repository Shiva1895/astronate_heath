import { ArrowUpRight, CircleCheck, Orbit } from 'lucide-react'
import { Link } from 'react-router-dom'
import MedicalSummaryCard from '../medical/MedicalSummaryCard'
import {
  MISSION_CONTROL_ALERTS,
  MISSION_CONTROL_BASE_PATH,
  MISSION_CONTROL_SUMMARIES,
  MISSION_CONTROL_SUMMARY,
} from '../../data/missionControlMockData'

function MissionSummary({ alerts = MISSION_CONTROL_ALERTS }) {
  const summaries = MISSION_CONTROL_SUMMARIES.map((summary) => (
    summary.id === 'alerts'
      ? { ...summary, value: String(alerts.filter((alert) => alert.status === 'New').length) }
      : summary
  ))

  return (
    <section className="space-y-4" aria-labelledby="mission-summary-title">
      <div className="relative overflow-hidden rounded-3xl border border-cyan-200/10 bg-[radial-gradient(circle_at_78%_0%,rgba(22,200,232,0.12),transparent_35%),linear-gradient(120deg,#0b1d2c,#0b1725_60%,#15152b)] p-5 sm:p-7">
        <div className="relative z-10 max-w-3xl">
          <p className="text-xs font-semibold uppercase tracking-[0.16em] text-cyan-200/80">Mission overview · Sample data</p>
          <div className="mt-3 flex flex-wrap items-center gap-3">
            <h2 id="mission-summary-title" className="text-2xl font-semibold tracking-[-0.035em] text-slate-50 sm:text-3xl">{MISSION_CONTROL_SUMMARY.name}</h2>
            <span className="inline-flex items-center gap-1.5 rounded-full border border-emerald-200/15 bg-emerald-200/[0.06] px-3 py-1 text-xs font-medium text-emerald-100">
              <span className="h-1.5 w-1.5 rounded-full bg-emerald-300" /> {MISSION_CONTROL_SUMMARY.status}
            </span>
          </div>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-300">Mission Day {MISSION_CONTROL_SUMMARY.missionDay} · Illustrative crew and health monitoring status for the ASTRA HEALTH prototype.</p>
          <div className="mt-5 grid grid-cols-2 gap-3 sm:grid-cols-4">
            {[
              ['Crew members', MISSION_CONTROL_SUMMARY.crewMembers],
              ['Medical officers', MISSION_CONTROL_SUMMARY.medicalOfficers],
              ['Monitoring', MISSION_CONTROL_SUMMARY.monitoringStatus],
              ['Last sample update', '09:42 UTC'],
            ].map(([label, value]) => (
              <div key={label} className="rounded-xl border border-white/[0.08] bg-[#07111f]/35 p-3">
                <p className="text-[10px] uppercase tracking-[0.1em] text-slate-500">{label}</p>
                <p className="mt-1 text-sm font-semibold text-slate-100">{value}</p>
              </div>
            ))}
          </div>
          <p className="mt-4 text-[11px] leading-5 text-slate-500">{MISSION_CONTROL_SUMMARY.lastDataUpdate} · Illustrative timestamp</p>
        </div>
        <div className="pointer-events-none absolute -right-10 -top-12 hidden h-64 w-64 items-center justify-center rounded-full border border-cyan-100/[0.06] bg-cyan-200/[0.025] text-cyan-100/20 sm:flex">
          <Orbit size={120} strokeWidth={0.65} />
        </div>
        <Link to={`${MISSION_CONTROL_BASE_PATH}/mission-status`} className="relative z-10 mt-5 inline-flex min-h-10 items-center gap-2 rounded-xl border border-cyan-200/15 bg-cyan-200/[0.06] px-3 text-xs font-medium text-cyan-100 hover:bg-cyan-200/[0.1]">
          Mission details <ArrowUpRight size={15} />
        </Link>
      </div>

      <div className="grid min-w-0 grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-4">
        {summaries.map((summary) => <MedicalSummaryCard key={summary.id} summary={summary} />)}
      </div>
      <p className="inline-flex items-center gap-2 text-[11px] leading-5 text-slate-500"><CircleCheck size={14} className="text-cyan-200/70" /> All mission, personnel, reading, and operational status values are fictional examples.</p>
    </section>
  )
}

export default MissionSummary
