import { useState } from 'react'
import { Battery, ChevronDown, ChevronUp, Radio } from 'lucide-react'
import { MISSION_CONTROL_ASTRONAUTS, MISSION_CONTROL_DEVICES } from '../../data/missionControlMockData'

const DEVICE_TONES = {
  Connected: 'border-emerald-200/15 bg-emerald-200/[0.05] text-emerald-100',
  Synchronizing: 'border-cyan-200/15 bg-cyan-200/[0.05] text-cyan-100',
  Disconnected: 'border-rose-200/15 bg-rose-200/[0.05] text-rose-100',
}

function DeviceMonitoring({ compact = false }) {
  const [expandedId, setExpandedId] = useState(null)
  const devices = compact ? MISSION_CONTROL_DEVICES.slice(0, 3) : MISSION_CONTROL_DEVICES

  return (
    <section className="min-w-0 rounded-2xl border border-white/[0.08] bg-[#0b1a29]/75 p-4 sm:p-5" aria-labelledby="device-monitoring-title">
      <div>
        <p className="text-xs font-semibold uppercase tracking-[0.14em] text-cyan-200/75">Device fleet · Simulated</p>
        <h2 id="device-monitoring-title" className="mt-2 text-lg font-semibold text-slate-50">Device Monitoring</h2>
        <p className="mt-1 text-xs leading-5 text-slate-500">Demo device records do not represent connected hardware.</p>
      </div>
      <div className="mt-4 grid min-w-0 grid-cols-1 gap-3 xl:grid-cols-2">
        {devices.map((device) => {
          const astronaut = MISSION_CONTROL_ASTRONAUTS.find((item) => item.id === device.astronautId)
          const expanded = expandedId === device.id
          return (
            <article key={device.id} className="min-w-0 rounded-xl border border-white/[0.07] bg-white/[0.02] p-4">
              <div className="flex items-start gap-3">
                <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl border border-cyan-200/10 bg-cyan-200/[0.045] text-cyan-100"><Radio size={17} /></span>
                <div className="min-w-0 flex-1">
                  <h3 className="truncate text-sm font-medium text-slate-100">{device.name}</h3>
                  <p className="mt-1 text-xs text-slate-500">{device.id} · {astronaut?.name || device.astronautId}</p>
                </div>
                <span className={`shrink-0 rounded-full border px-2 py-1 text-[10px] ${DEVICE_TONES[device.status]}`}>{device.status}</span>
              </div>
              <div className="mt-3 flex flex-wrap items-center justify-between gap-2 text-xs text-slate-400">
                <span>Last sample: {device.lastMeasurement}</span>
                <span className="text-slate-500">{device.updated}</span>
              </div>
              {expanded ? (
                <div className="mt-3 grid grid-cols-2 gap-2 border-t border-white/[0.06] pt-3">
                  <p className="text-[11px] text-slate-500">Assigned astronaut <span className="mt-1 block text-slate-300">{astronaut?.name} · {device.astronautId}</span></p>
                  <p className="text-[11px] text-slate-500"><span className="inline-flex items-center gap-1"><Battery size={12} /> Sample battery</span><span className="mt-1 block text-slate-300">{device.battery}%</span></p>
                </div>
              ) : null}
              <button type="button" onClick={() => setExpandedId(expanded ? null : device.id)} className="mt-3 inline-flex min-h-9 items-center gap-1 rounded-lg px-2 text-xs font-medium text-cyan-100 hover:bg-cyan-200/[0.05]" aria-expanded={expanded}>
                {expanded ? 'Hide device details' : 'View device details'} {expanded ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
              </button>
            </article>
          )
        })}
      </div>
    </section>
  )
}

export default DeviceMonitoring
