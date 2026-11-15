import { useOutletContext } from 'react-router-dom'
import ApiState from '../../components/dashboard/ApiState'
import PageHeader from '../../components/dashboard/PageHeader'

function HealthMonitoring() {
  const { readings } = useOutletContext()

  return (
    <div className="space-y-5">
      <PageHeader
        eyebrow="Personal health monitoring"
        title="Measurements & readings"
        description="A paginated snapshot of the health readings authorized for your linked astronaut profile. Values are informational workflow data, not a diagnosis."
      />
      <ApiState empty={readings.length === 0 ? 'No health readings have been recorded for your profile.' : null}>
        <div className="overflow-hidden rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85">
          <div className="overflow-x-auto">
            <table className="w-full min-w-[640px] text-left text-sm">
              <thead className="bg-white/[0.025] text-[10px] uppercase tracking-[0.12em] text-slate-500">
                <tr>
                  <th className="px-4 py-3 font-medium">Recorded</th>
                  <th className="px-4 py-3 font-medium">Reading type</th>
                  <th className="px-4 py-3 font-medium">Value</th>
                  <th className="px-4 py-3 font-medium">Source</th>
                  <th className="px-4 py-3 font-medium">Device</th>
                </tr>
              </thead>
              <tbody>
                {readings.map((reading) => (
                  <tr key={reading.id} className="border-t border-white/[0.06] text-xs text-slate-300">
                    <td className="whitespace-nowrap px-4 py-3">{new Date(reading.recordedAt).toLocaleString()}</td>
                    <td className="px-4 py-3">{reading.readingType}</td>
                    <td className="px-4 py-3 font-medium">{reading.value} {reading.unit}</td>
                    <td className="px-4 py-3">{reading.dataSource}</td>
                    <td className="px-4 py-3">{reading.deviceId ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </ApiState>
    </div>
  )
}

export default HealthMonitoring
