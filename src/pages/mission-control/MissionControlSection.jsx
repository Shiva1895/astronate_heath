import { useEffect, useState } from 'react'
import { Download, Smartphone } from 'lucide-react'
import { Navigate, useOutletContext, useParams } from 'react-router-dom'
import { api } from '../../api/client'
import ApiState from '../../components/dashboard/ApiState'
import PageHeader from '../../components/dashboard/PageHeader'

function MissionControlSection() {
  const { section } = useParams()
  const { missions, notifications, markNotificationRead, markAllNotificationsRead } = useOutletContext()
  const [devices, setDevices] = useState([])
  const [deviceState, setDeviceState] = useState({ loading: true, error: '' })
  const [exportingId, setExportingId] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    if (section !== 'device-monitoring') return undefined
    let active = true
    api.devicesList()
      .then((page) => {
        if (active) {
          setDevices(page.content || [])
          setDeviceState({ loading: false, error: '' })
        }
      })
      .catch((requestError) => {
        if (active) setDeviceState({ loading: false, error: requestError.message || 'Device records could not be loaded.' })
      })
    return () => { active = false }
  }, [section])

  if (!['device-monitoring', 'notifications', 'reports'].includes(section)) {
    return <Navigate to="/mission-control/dashboard" replace />
  }

  async function exportMission(mission) {
    setExportingId(String(mission.missionId))
    setError('')
    try {
      const file = await api.exportMissionReport(mission.missionId, 'PDF')
      const url = URL.createObjectURL(file)
      const link = document.createElement('a')
      link.href = url
      link.download = `mission-${mission.missionCode}.pdf`
      link.click()
      URL.revokeObjectURL(url)
    } catch (requestError) {
      setError(requestError.message || 'Mission report export failed.')
    } finally {
      setExportingId('')
    }
  }

  if (section === 'device-monitoring') {
    return (
      <div className="space-y-5">
        <PageHeader eyebrow="Operational devices" title="Device Monitoring" description="Device inventory and status only. Mission Control does not receive raw readings or private medical information." />
        <ApiState loading={deviceState.loading} error={deviceState.error} empty={devices.length === 0 ? 'No devices are available.' : null}>
          <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
            {devices.map((device) => (
              <article key={device.id} className="rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4">
                <div className="flex items-start justify-between gap-3">
                  <div><h2 className="text-sm font-semibold text-slate-100">{device.deviceName}</h2><p className="mt-1 text-xs text-slate-500">{device.deviceType}</p></div>
                  <span className="rounded-full border border-white/10 px-2.5 py-1 text-[10px] text-slate-300">{device.status}</span>
                </div>
                <p className="mt-3 break-all text-xs text-slate-400">Serial · {device.serialNumber}</p>
                <p className="mt-2 text-xs text-slate-500">Astronaut code · {device.astronautCode || 'Unassigned'}</p>
                <p className="mt-1 text-xs text-slate-500">Last communication · {device.lastCommunicationAt ? new Date(device.lastCommunicationAt).toLocaleString() : 'Not recorded'}</p>
              </article>
            ))}
          </div>
        </ApiState>
        <div className="flex items-start gap-2 text-xs leading-5 text-slate-500"><Smartphone size={15} className="mt-0.5 shrink-0" /> A stored device record does not confirm a live, authenticated hardware connection.</div>
      </div>
    )
  }

  if (section === 'notifications') {
    return (
      <div className="space-y-5">
        <PageHeader eyebrow="Account notifications" title="Notifications" description="Read and update notifications associated with your account." action={<button type="button" onClick={markAllNotificationsRead} disabled={!notifications.some((item) => !item.read)} className="min-h-10 rounded-lg border border-white/10 px-3 text-xs text-cyan-100 disabled:opacity-50">Mark all read</button>} />
        <ul className="divide-y divide-white/[0.06] overflow-hidden rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85">
          {notifications.map((item) => (
            <li key={item.id} className="flex flex-wrap items-start gap-3 p-4">
              <div className="min-w-0 flex-1"><h2 className="text-sm font-medium text-slate-100">{item.title}</h2><p className="mt-1 text-xs text-slate-500">{item.time} · {item.type}</p><p className="mt-2 text-sm leading-5 text-slate-300">{item.detail}</p></div>
              <button type="button" onClick={() => markNotificationRead(item.id)} disabled={item.read} className="min-h-9 rounded-lg px-3 text-xs text-cyan-100 hover:bg-white/[0.05] disabled:text-slate-600">{item.read ? 'Read' : 'Mark read'}</button>
            </li>
          ))}
          {!notifications.length ? <li className="p-5 text-sm text-slate-400">No notifications are available.</li> : null}
        </ul>
      </div>
    )
  }

  return (
    <div className="space-y-5">
      <PageHeader eyebrow="Operational reports" title="Mission Reports" description="Export mission-level summaries. This role cannot export raw health histories or private well-being reports." />
      {error ? <p role="alert" className="rounded-xl border border-rose-300/15 p-3 text-sm text-rose-100">{error}</p> : null}
      <ApiState empty={missions.length === 0 ? 'No mission summaries are available to export.' : null}>
        <div className="space-y-3">
          {missions.map((mission) => (
            <article key={mission.missionId} className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-white/[0.08] bg-[#0b1a29]/85 p-4">
              <div><h2 className="text-sm font-semibold text-slate-100">{mission.missionName}</h2><p className="mt-1 text-xs text-slate-500">{mission.missionCode} · {mission.status}</p></div>
              <button type="button" onClick={() => exportMission(mission)} disabled={Boolean(exportingId)} className="inline-flex min-h-10 items-center gap-2 rounded-lg border border-cyan-200/15 px-3 text-xs text-cyan-100 disabled:opacity-50"><Download size={14} />{exportingId === String(mission.missionId) ? 'Preparing…' : 'Export PDF'}</button>
            </article>
          ))}
        </div>
      </ApiState>
    </div>
  )
}

export default MissionControlSection
