import { useState } from 'react'
import { Download, Radio, Smartphone, Bell } from 'lucide-react'
import { Link, useOutletContext } from 'react-router-dom'
import { api } from '../../api/client'
import PageHeader from '../../components/dashboard/PageHeader'

function MissionControlOverview() {
  const { missions, notifications } = useOutletContext()
  const [busyMission, setBusyMission] = useState('')
  const [error, setError] = useState('')

  async function exportMission(mission) {
    setBusyMission(String(mission.missionId))
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
      setBusyMission('')
    }
  }

  return (
    <div className="space-y-5">
      <PageHeader
        eyebrow="Mission operations"
        title="Mission Control Overview"
        description="Review backend mission-level operational summaries. This role cannot access raw health readings, private observations, or well-being reports."
      />
      {error ? <p role="alert" className="rounded-xl border border-rose-300/15 p-3 text-sm text-rose-100">{error}</p> : null}
      <div className="grid gap-3 sm:grid-cols-3">
        {[
          { title: 'Missions', value: missions.length, icon: Radio },
          { title: 'Unread notifications', value: notifications.filter((item) => !item.read).length, icon: Bell },
          { title: 'Devices', value: 'Open device view', icon: Smartphone, link: '/mission-control/dashboard/device-monitoring' },
        ].map((item) => (
          <article key={item.title} className="rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4">
            <item.icon size={18} className="text-cyan-100" />
            {item.link ? <Link to={item.link} className="mt-4 block text-sm font-medium text-cyan-100">{item.value}</Link> : <p className="mt-4 text-2xl font-semibold text-slate-50">{item.value}</p>}
            <p className="mt-1 text-xs text-slate-400">{item.title}</p>
          </article>
        ))}
      </div>
      <section className="space-y-3">
        <h2 className="text-sm font-semibold text-slate-100">Mission summaries</h2>
        {missions.map((mission) => (
          <article key={mission.missionId} className="rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4 sm:p-5">
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div>
                <p className="text-[10px] uppercase tracking-[0.14em] text-cyan-200/80">{mission.missionCode}</p>
                <h3 className="mt-1 text-lg font-semibold text-slate-50">{mission.missionName}</h3>
                <p className="mt-1 text-xs text-slate-400">Status · {mission.status}</p>
              </div>
              <button type="button" onClick={() => exportMission(mission)} disabled={Boolean(busyMission)} className="inline-flex min-h-10 items-center gap-2 rounded-lg border border-cyan-200/15 px-3 text-xs text-cyan-100 disabled:opacity-50">
                <Download size={14} /> {busyMission === String(mission.missionId) ? 'Preparing…' : 'Export report'}
              </button>
            </div>
            <dl className="mt-4 grid grid-cols-2 gap-3 border-t border-white/[0.06] pt-4 sm:grid-cols-4">
              {[
                ['Crew count', mission.astronautCount],
                ['Active alerts', mission.activeAlertCount],
                ['Offline devices', mission.offlineDeviceCount],
                ['Latest reading time', mission.latestReadingAt ? new Date(mission.latestReadingAt).toLocaleString() : 'No readings'],
              ].map(([label, value]) => <div key={label}><dt className="text-[10px] uppercase tracking-[0.1em] text-slate-500">{label}</dt><dd className="mt-1 text-sm font-medium text-slate-200">{value}</dd></div>)}
            </dl>
          </article>
        ))}
        {!missions.length ? <p className="rounded-xl border border-white/[0.08] p-5 text-sm text-slate-400">No mission summaries are available.</p> : null}
      </section>
    </div>
  )
}

export default MissionControlOverview
