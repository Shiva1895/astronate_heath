import { ArrowRight, Bell, HeartPulse, Smartphone, Waves } from 'lucide-react'
import { Link, useOutletContext } from 'react-router-dom'
import PageHeader from '../../components/dashboard/PageHeader'

function DashboardOverview() {
  const { user, astronautId, latestReadings, alerts, devices, reports } = useOutletContext()
  const activeAlerts = alerts.filter((alert) => !['Resolved', 'Closed'].includes(alert.status))
  const summaries = [
    { label: 'Latest reading types', value: latestReadings.length, icon: HeartPulse },
    { label: 'Open alerts', value: activeAlerts.length, icon: Bell },
    { label: 'Assigned devices', value: devices.length, icon: Smartphone },
    { label: 'Well-being reports', value: reports.length, icon: Waves },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Personal health overview"
        title={`Welcome, ${user.displayName || user.username || 'Astronaut'}`}
        description="This dashboard displays readings, alerts, devices, and reports returned for your linked astronaut profile."
      />
      <section className="grid min-w-0 gap-3 sm:grid-cols-2 xl:grid-cols-4" aria-label="Account health summary">
        {summaries.map(({ label, value, icon: Icon }) => (
          <article key={label} className="rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4">
            <Icon size={18} className="text-cyan-100" />
            <p className="mt-4 text-2xl font-semibold text-slate-50">{value}</p>
            <p className="mt-1 text-xs text-slate-400">{label}</p>
          </article>
        ))}
      </section>
      <section className="rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4 sm:p-5">
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 className="text-sm font-semibold text-slate-100">Recent readings</h2>
            <p className="mt-1 text-xs text-slate-500">Astronaut profile #{astronautId} · values as returned by the API</p>
          </div>
          <Link to="/astronaut/dashboard/health" className="inline-flex min-h-9 items-center gap-1 text-xs font-medium text-cyan-200">
            View readings <ArrowRight size={14} />
          </Link>
        </div>
        {latestReadings.length ? (
          <ul className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
            {latestReadings.map((reading) => (
              <li key={reading.id} className="rounded-xl border border-white/[0.07] bg-white/[0.02] p-4">
                <p className="text-xs text-slate-500">{reading.readingType}</p>
                <p className="mt-1 text-xl font-semibold text-slate-100">{reading.value} <span className="text-xs font-normal text-slate-400">{reading.unit}</span></p>
                <p className="mt-2 text-[11px] text-slate-500">{new Date(reading.recordedAt).toLocaleString()}</p>
              </li>
            ))}
          </ul>
        ) : <p className="rounded-xl border border-white/[0.07] p-5 text-sm text-slate-400">No readings are available yet.</p>}
      </section>
      <section className="rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4 sm:p-5">
        <div className="mb-4 flex items-center justify-between gap-3">
          <div>
            <h2 className="text-sm font-semibold text-slate-100">Alerts requiring attention</h2>
            <p className="mt-1 text-xs text-slate-500">Backend workflow alerts, not diagnoses</p>
          </div>
          <Link to="/astronaut/dashboard/alerts" className="inline-flex min-h-9 items-center gap-1 text-xs font-medium text-cyan-200">
            View alerts <ArrowRight size={14} />
          </Link>
        </div>
        {activeAlerts.length ? (
          <ul className="divide-y divide-white/[0.06]">
            {activeAlerts.slice(0, 5).map((alert) => (
              <li key={alert.id} className="flex flex-wrap justify-between gap-2 py-3">
                <div>
                  <p className="text-sm font-medium text-slate-200">{alert.title}</p>
                  <p className="mt-1 text-xs text-slate-500">{alert.severity} · {alert.status} · {new Date(alert.timestamp).toLocaleString()}</p>
                </div>
              </li>
            ))}
          </ul>
        ) : <p className="rounded-xl border border-white/[0.07] p-5 text-sm text-slate-400">There are no active alerts.</p>}
      </section>
    </div>
  )
}

export default DashboardOverview
