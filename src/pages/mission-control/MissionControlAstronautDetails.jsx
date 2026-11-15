import { ArrowLeft, CircleAlert } from 'lucide-react'
import { Link, useOutletContext, useParams } from 'react-router-dom'
import HealthChart from '../../components/dashboard/HealthChart'
import MetricCard from '../../components/dashboard/MetricCard'
import PageHeader from '../../components/dashboard/PageHeader'
import {
  MEDICAL_HEALTH_METRICS,
  MEDICAL_HEALTH_TRENDS,
} from '../../data/medicalMockData'
import {
  MISSION_CONTROL_ASTRONAUTS,
  MISSION_CONTROL_BASE_PATH,
  MISSION_CONTROL_DEVICES,
} from '../../data/missionControlMockData'

function MissionControlAstronautDetails() {
  const { astronautId } = useParams()
  const { alerts } = useOutletContext()
  const astronaut = MISSION_CONTROL_ASTRONAUTS.find((item) => item.id === astronautId)

  if (!astronaut) {
    return (
      <section className="rounded-2xl border border-white/[0.08] bg-[#0b1a29]/75 p-6">
        <h2 className="text-lg font-semibold text-slate-100">Sample astronaut not found</h2>
        <p className="mt-2 text-sm text-slate-400">This demo roster does not contain the requested astronaut profile.</p>
        <Link to={`${MISSION_CONTROL_BASE_PATH}/astronaut-monitoring`} className="mt-4 inline-flex min-h-10 items-center gap-2 text-sm text-cyan-100">
          <ArrowLeft size={16} /> Return to astronaut monitoring
        </Link>
      </section>
    )
  }

  const metrics = MEDICAL_HEALTH_METRICS[astronaut.id]
  const trends = MEDICAL_HEALTH_TRENDS[astronaut.id]['24h']
  const astronautAlerts = alerts.filter((alert) => alert.astronautId === astronaut.id)
  const devices = MISSION_CONTROL_DEVICES.filter((device) => device.astronautId === astronaut.id)

  return (
    <div className="space-y-5">
      <PageHeader
        eyebrow="Crew profile · Fictional sample"
        title={astronaut.name}
        description={`${astronaut.id} · ${astronaut.mission} · All profile, health, alert, and device details are illustrative only.`}
        action={
          <Link to={`${MISSION_CONTROL_BASE_PATH}/astronaut-monitoring`} className="inline-flex min-h-10 items-center gap-2 rounded-xl border border-white/10 px-3 text-xs font-medium text-slate-300 hover:bg-white/[0.04]">
            <ArrowLeft size={14} /> All astronauts
          </Link>
        }
      />

      <section className="grid min-w-0 grid-cols-1 gap-3 sm:grid-cols-3">
        {[
          ['Mission assignment', astronaut.mission],
          ['Monitoring state', astronaut.monitoringStatus],
          ['Latest sample update', `${astronaut.latestUpdate} · illustrative`],
        ].map(([label, value]) => (
          <article key={label} className="rounded-xl border border-white/[0.08] bg-[#0b1a29]/75 p-4">
            <p className="text-xs text-slate-500">{label}</p>
            <p className="mt-2 text-sm font-medium text-slate-100">{value}</p>
          </article>
        ))}
      </section>

      <section>
        <div className="mb-3">
          <h2 className="text-base font-semibold text-slate-100">Health measurement summary · Sample</h2>
          <p className="mt-1 text-xs text-slate-500">Values are simulated and are not live sensor readings.</p>
        </div>
        <div className="grid min-w-0 grid-cols-1 gap-3 sm:grid-cols-2 2xl:grid-cols-4">
          {metrics.map((metric) => <MetricCard key={`${astronaut.id}-${metric.id}`} metric={metric} />)}
        </div>
      </section>

      <section>
        <div className="mb-3">
          <h2 className="text-base font-semibold text-slate-100">Recent health trends · Illustrative</h2>
          <p className="mt-1 text-xs text-slate-500">{astronaut.name} · Last 24 hours · Example data</p>
        </div>
        <div className="grid min-w-0 grid-cols-1 gap-3 xl:grid-cols-2">
          <HealthChart title="Heart rate" dataKey="heartRate" unit="BPM" color="#16c8e8" data={trends} />
          <HealthChart title="SpO₂" dataKey="spo2" unit="%" color="#8b83ff" data={trends} />
          <HealthChart title="Body temperature" dataKey="temperature" unit="°C" color="#4ea8ff" data={trends} />
        </div>
      </section>

      <section className="grid min-w-0 grid-cols-1 gap-4 xl:grid-cols-2">
        <div className="rounded-2xl border border-white/[0.08] bg-[#0b1a29]/75 p-4 sm:p-5">
          <h2 className="text-base font-semibold text-slate-100">Health alert history · Sample</h2>
          <ul className="mt-3 space-y-3">
            {astronautAlerts.map((alert) => (
              <li key={alert.id} className="rounded-xl border border-white/[0.06] bg-white/[0.02] p-3">
                <div className="flex items-start gap-2">
                  <CircleAlert size={15} className="mt-0.5 shrink-0 text-amber-100" />
                  <div className="min-w-0">
                    <p className="text-xs font-medium text-slate-200">{alert.title}</p>
                    <p className="mt-1 text-[11px] leading-5 text-slate-500">{alert.id} · {alert.severity} · {alert.status}</p>
                  </div>
                </div>
              </li>
            ))}
            {astronautAlerts.length === 0 ? <li className="text-xs text-slate-500">No sample alerts are associated with this profile.</li> : null}
          </ul>
        </div>
        <div className="rounded-2xl border border-white/[0.08] bg-[#0b1a29]/75 p-4 sm:p-5">
          <h2 className="text-base font-semibold text-slate-100">Device information · Simulated</h2>
          <ul className="mt-3 space-y-2">
            {devices.map((device) => (
              <li key={device.id} className="flex flex-wrap items-center justify-between gap-2 rounded-xl border border-white/[0.06] bg-white/[0.02] p-3">
                <div>
                  <p className="text-xs font-medium text-slate-200">{device.name}</p>
                  <p className="mt-1 text-[10px] text-slate-500">{device.id} · Sample update {device.updated}</p>
                </div>
                <span className="text-[11px] text-cyan-100">{device.status}</span>
              </li>
            ))}
            {devices.length === 0 ? <li className="text-xs text-slate-500">No sample device is assigned to this profile.</li> : null}
          </ul>
        </div>
      </section>
      <p className="text-[11px] leading-5 text-slate-600">This is a fictional demonstration profile, not an authenticated or authorized medical record. Medical review and access control require a trusted backend.</p>
    </div>
  )
}

export default MissionControlAstronautDetails
