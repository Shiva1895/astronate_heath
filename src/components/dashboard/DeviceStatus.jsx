import { Battery, ChevronDown, ChevronUp, Radio, Watch } from 'lucide-react'
import { useState } from 'react'

const STATUS_STYLES = {
  Connected: 'bg-emerald-200/[0.08] text-emerald-200',
  Disconnected: 'bg-rose-200/[0.08] text-rose-200',
  Synchronizing: 'bg-cyan-200/[0.08] text-cyan-100',
}

function DeviceStatus({ devices, compact = false }) {
  const [expandedDevice, setExpandedDevice] = useState(null)
  const visibleDevices = compact ? devices.slice(0, 3) : devices

  return (
    <section className="min-w-0 rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85">
      <div className="flex items-center justify-between gap-3 border-b border-white/[0.07] px-4 py-4 sm:px-5">
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-xl border border-cyan-200/10 bg-cyan-200/[0.06] text-cyan-100">
            <Radio size={17} />
          </div>
          <div>
            <h3 className="text-sm font-semibold text-slate-100">Device status</h3>
            <p className="mt-0.5 text-xs text-slate-500">All connection states are simulated</p>
          </div>
        </div>
        <Watch size={17} className="text-slate-500" />
      </div>
      <div className="divide-y divide-white/[0.06]">
        {visibleDevices.map((device) => {
          const Icon = device.icon
          const expanded = expandedDevice === device.id

          return (
            <div key={device.id} className="px-4 py-3.5 sm:px-5">
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl border border-white/[0.07] bg-white/[0.025] text-slate-300">
                  <Icon size={17} />
                </div>
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-medium text-slate-200">{device.name}</p>
                  <p className="mt-0.5 truncate text-xs text-slate-500">{device.id} · Last sync {device.lastSync}</p>
                </div>
                <span className={`hidden shrink-0 rounded-full px-2.5 py-1 text-[10px] font-medium sm:inline-flex ${STATUS_STYLES[device.status]}`}>
                  {device.status}
                </span>
                <button
                  type="button"
                  onClick={() => setExpandedDevice(expanded ? null : device.id)}
                  aria-label={`${expanded ? 'Hide' : 'Show'} details for ${device.name}`}
                  aria-expanded={expanded}
                  className="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-lg text-slate-400 hover:bg-white/[0.05] hover:text-white"
                >
                  {expanded ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
                </button>
              </div>
              <div className="mt-2 flex items-center justify-between pl-[52px] sm:hidden">
                <span className={`rounded-full px-2.5 py-1 text-[10px] font-medium ${STATUS_STYLES[device.status]}`}>{device.status}</span>
                <span className="inline-flex items-center gap-1 text-[10px] text-slate-500">
                  <Battery size={12} /> {device.battery}%
                </span>
              </div>
              {expanded ? (
                <div className="ml-[52px] mt-3 rounded-xl border border-white/[0.06] bg-white/[0.02] p-3">
                  <div className="flex flex-wrap gap-x-5 gap-y-2 text-xs text-slate-400">
                    <span>Type: {device.category}</span>
                    <span>Sample ID: {device.serial}</span>
                    <span className="inline-flex items-center gap-1"><Battery size={13} /> {device.battery}% sample battery</span>
                  </div>
                  <p className="mt-2 text-xs leading-5 text-slate-500">{device.detail}</p>
                </div>
              ) : null}
            </div>
          )
        })}
      </div>
    </section>
  )
}

export default DeviceStatus
