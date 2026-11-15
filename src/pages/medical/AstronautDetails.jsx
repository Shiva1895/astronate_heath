import { useEffect, useState } from 'react'
import { ArrowLeft } from 'lucide-react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../../api/client'
import ApiState from '../../components/dashboard/ApiState'
import PageHeader from '../../components/dashboard/PageHeader'

function contentOf(page) {
  return Array.isArray(page) ? page : page?.content || []
}

function AstronautDetails() {
  const { astronautId } = useParams()
  const [state, setState] = useState({ loading: true, error: '', astronaut: null, readings: [], alerts: [], devices: [], observations: [] })

  useEffect(() => {
    let active = true
    async function load() {
      try {
        const [astronaut, readings, alerts, devices, observations] = await Promise.all([
          api.astronaut(astronautId),
          api.readings(astronautId),
          api.astronautAlerts(astronautId),
          api.devices(astronautId),
          api.observations(astronautId),
        ])
        if (active) setState({
          loading: false,
          error: '',
          astronaut,
          readings: contentOf(readings),
          alerts: contentOf(alerts),
          devices: contentOf(devices),
          observations: contentOf(observations),
        })
      } catch (error) {
        if (active) setState({ loading: false, error: error.message || 'The assigned astronaut record could not be loaded.', astronaut: null, readings: [], alerts: [], devices: [], observations: [] })
      }
    }
    load()
    return () => { active = false }
  }, [astronautId])

  return (
    <div className="space-y-5">
      <PageHeader
        eyebrow="Assigned astronaut record"
        title={state.astronaut?.fullName || `Astronaut ${astronautId}`}
        description="Personal and health data is requested from role- and assignment-checked backend endpoints."
        action={<Link to="/medical/dashboard/astronauts" className="inline-flex min-h-10 items-center gap-2 rounded-lg px-3 text-xs text-cyan-100"><ArrowLeft size={14} /> Assigned astronauts</Link>}
      />
      <ApiState loading={state.loading} error={state.error}>
        {state.astronaut ? (
          <div className="space-y-5">
            <section className="grid gap-3 rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4 sm:grid-cols-2 xl:grid-cols-4">
              {[['Astronaut code', state.astronaut.astronautCode], ['Profile ID', state.astronaut.id], ['Email', state.astronaut.email], ['Status', state.astronaut.status]].map(([label, value]) => <div key={label}><p className="text-xs text-slate-500">{label}</p><p className="mt-1 break-all text-sm text-slate-200">{value || '—'}</p></div>)}
            </section>
            <section className="grid gap-4 xl:grid-cols-2">
              <article className="rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4">
                <h2 className="text-sm font-semibold text-slate-100">Recent readings</h2>
                <ul className="mt-3 divide-y divide-white/[0.06]">
                  {state.readings.slice(0, 10).map((reading) => <li key={reading.id} className="flex flex-wrap justify-between gap-2 py-3 text-xs"><span className="text-slate-300">{reading.readingType} · {reading.value} {reading.unit}</span><span className="text-slate-500">{new Date(reading.recordedAt).toLocaleString()}</span></li>)}
                  {state.readings.length === 0 ? <li className="py-3 text-xs text-slate-500">No readings are available.</li> : null}
                </ul>
              </article>
              <article className="rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4">
                <h2 className="text-sm font-semibold text-slate-100">Alerts</h2>
                <ul className="mt-3 divide-y divide-white/[0.06]">
                  {state.alerts.map((alert) => <li key={alert.id} className="py-3"><p className="text-xs font-medium text-slate-200">{alert.title} · {alert.status}</p><p className="mt-1 text-[11px] text-slate-500">{alert.priority} · {new Date(alert.createdAt).toLocaleString()}</p></li>)}
                  {state.alerts.length === 0 ? <li className="py-3 text-xs text-slate-500">No alerts are available.</li> : null}
                </ul>
              </article>
              <article className="rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4">
                <h2 className="text-sm font-semibold text-slate-100">Assigned devices</h2>
                <ul className="mt-3 divide-y divide-white/[0.06]">
                  {state.devices.map((device) => <li key={device.id} className="py-3 text-xs"><p className="font-medium text-slate-200">{device.deviceName} · {device.status}</p><p className="mt-1 text-slate-500">{device.deviceType} · {device.serialNumber}</p></li>)}
                  {state.devices.length === 0 ? <li className="py-3 text-xs text-slate-500">No devices are assigned.</li> : null}
                </ul>
              </article>
              <article className="rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4">
                <h2 className="text-sm font-semibold text-slate-100">Private medical observations</h2>
                <ul className="mt-3 divide-y divide-white/[0.06]">
                  {state.observations.map((observation) => <li key={observation.id} className="py-3"><p className="whitespace-pre-wrap text-xs leading-5 text-slate-300">{observation.observationText}</p><p className="mt-1 text-[11px] text-slate-500">{new Date(observation.createdAt).toLocaleString()}</p></li>)}
                  {state.observations.length === 0 ? <li className="py-3 text-xs text-slate-500">No observations are recorded.</li> : null}
                </ul>
              </article>
            </section>
          </div>
        ) : null}
      </ApiState>
    </div>
  )
}

export default AstronautDetails
