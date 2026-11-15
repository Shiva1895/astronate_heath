import { useOutletContext } from 'react-router-dom'
import ApiState from '../../components/dashboard/ApiState'
import PageHeader from '../../components/dashboard/PageHeader'

function Devices() {
  const { devices } = useOutletContext()

  return (
    <div className="space-y-5">
      <PageHeader
        eyebrow="Assigned devices"
        title="Device status"
        description="Review devices assigned to your astronaut profile. Device status and communication timestamps are provided by the backend."
      />
      <ApiState empty={devices.length === 0 ? 'No devices are assigned to your profile.' : null}>
        <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
          {devices.map((device) => (
            <article key={device.id} className="rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <h2 className="text-sm font-semibold text-slate-100">{device.deviceName}</h2>
                  <p className="mt-1 text-xs text-slate-500">{device.deviceType}</p>
                </div>
                <span className="rounded-full border border-white/10 px-2.5 py-1 text-[10px] text-slate-300">{device.status}</span>
              </div>
              <dl className="mt-4 space-y-2 border-t border-white/[0.06] pt-3 text-xs">
                <div className="flex justify-between gap-3"><dt className="text-slate-500">Serial number</dt><dd className="break-all text-right text-slate-300">{device.serialNumber}</dd></div>
                <div className="flex justify-between gap-3"><dt className="text-slate-500">Registered</dt><dd className="text-right text-slate-300">{new Date(device.registeredAt).toLocaleString()}</dd></div>
                <div className="flex justify-between gap-3"><dt className="text-slate-500">Last communication</dt><dd className="text-right text-slate-300">{device.lastCommunicationAt ? new Date(device.lastCommunicationAt).toLocaleString() : 'No communication recorded'}</dd></div>
              </dl>
            </article>
          ))}
        </div>
      </ApiState>
      <p className="text-xs leading-5 text-slate-500">A device record does not imply a verified physical connection or authenticated sensor data source.</p>
    </div>
  )
}

export default Devices
