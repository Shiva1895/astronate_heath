import { useEffect, useState } from 'react'
import { Download } from 'lucide-react'
import { useOutletContext } from 'react-router-dom'
import { api } from '../../api/client'
import ApiState from '../../components/dashboard/ApiState'
import PageHeader from '../../components/dashboard/PageHeader'

function dateOffset(days) {
  const date = new Date(Date.now() - days * 24 * 60 * 60 * 1000)
  return date.toISOString().slice(0, 10)
}

function MedicalHistory() {
  const { astronauts, alerts } = useOutletContext()
  const [astronautId, setAstronautId] = useState('')
  const [startDate, setStartDate] = useState(dateOffset(7))
  const [endDate, setEndDate] = useState(dateOffset(0))
  const [state, setState] = useState({ loading: false, error: '', readings: [] })
  const [exportError, setExportError] = useState('')
  const [exporting, setExporting] = useState(false)

  useEffect(() => {
    setAstronautId((current) => current || astronauts[0]?.id || '')
  }, [astronauts])

  useEffect(() => {
    if (!astronautId) return
    let active = true
    const query = new URLSearchParams({
      from: `${startDate}T00:00:00Z`,
      to: `${endDate}T23:59:59.999Z`,
      page: '0',
      size: '100',
    })
    setState({ loading: true, error: '', readings: [] })
    api.readings(astronautId, `?${query}`)
      .then((page) => {
        if (active) setState({ loading: false, error: '', readings: page.content || [] })
      })
      .catch((error) => {
        if (active) setState({ loading: false, error: error.message || 'Health history could not be loaded.', readings: [] })
      })
    return () => { active = false }
  }, [astronautId, startDate, endDate])

  async function exportHistory() {
    if (!astronautId) return
    setExporting(true)
    setExportError('')
    try {
      const file = await api.exportHealthReport(astronautId, `${startDate}T00:00:00Z`, `${endDate}T23:59:59.999Z`, 'CSV')
      const url = URL.createObjectURL(file)
      const link = document.createElement('a')
      link.href = url
      link.download = `health-readings-${astronautId}.csv`
      link.click()
      URL.revokeObjectURL(url)
    } catch (error) {
      setExportError(error.message || 'The health report export failed.')
    } finally {
      setExporting(false)
    }
  }

  const historyAlerts = alerts.filter((alert) => alert.astronautId === astronautId)
  const astronaut = astronauts.find((item) => item.id === astronautId)

  return (
    <div className="space-y-5">
      <PageHeader
        eyebrow="Authorized health history"
        title="Health History"
        description="Filter persisted readings and workflow alerts for an assigned astronaut. Access is checked for each request."
        action={<button type="button" onClick={exportHistory} disabled={!astronautId || exporting} className="inline-flex min-h-10 items-center gap-2 rounded-lg border border-cyan-200/15 px-3 text-xs text-cyan-100 disabled:opacity-50"><Download size={14} />{exporting ? 'Exporting…' : 'Export CSV'}</button>}
      />
      {exportError ? <p role="alert" className="rounded-xl border border-rose-300/15 p-3 text-sm text-rose-100">{exportError}</p> : null}
      <section className="grid gap-3 rounded-2xl border border-white/[0.08] bg-[#0b1a29]/65 p-4 sm:grid-cols-2 xl:grid-cols-4" aria-label="Health history filters">
        <label>
          <span className="mb-1.5 block text-xs text-slate-400">Assigned astronaut</span>
          <select value={astronautId} onChange={(event) => setAstronautId(event.target.value)} className="min-h-11 w-full rounded-xl border border-white/10 bg-[#071522] px-3 text-sm text-slate-100">
            {astronauts.map((item) => <option key={item.id} value={item.id}>{item.name} · {item.id}</option>)}
          </select>
        </label>
        <label>
          <span className="mb-1.5 block text-xs text-slate-400">From date (UTC)</span>
          <input type="date" value={startDate} max={endDate} onChange={(event) => setStartDate(event.target.value)} className="min-h-11 w-full rounded-xl border border-white/10 bg-[#071522] px-3 text-sm text-slate-100 [color-scheme:dark]" />
        </label>
        <label>
          <span className="mb-1.5 block text-xs text-slate-400">To date (UTC)</span>
          <input type="date" value={endDate} min={startDate} onChange={(event) => setEndDate(event.target.value)} className="min-h-11 w-full rounded-xl border border-white/10 bg-[#071522] px-3 text-sm text-slate-100 [color-scheme:dark]" />
        </label>
        <div className="flex items-end text-xs text-slate-400">{state.readings.length} backend readings {astronaut ? `for ${astronaut.name}` : ''}</div>
      </section>
      <ApiState loading={state.loading} error={state.error} empty={state.readings.length === 0 ? 'No readings match this astronaut and date range.' : null}>
        <div className="overflow-hidden rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85">
          <div className="overflow-x-auto">
            <table className="w-full min-w-[700px] text-left">
              <thead className="bg-white/[0.025] text-[10px] uppercase tracking-[0.12em] text-slate-500">
                <tr><th className="px-4 py-3 font-medium">Recorded</th><th className="px-4 py-3 font-medium">Type</th><th className="px-4 py-3 font-medium">Value</th><th className="px-4 py-3 font-medium">Source</th><th className="px-4 py-3 font-medium">Device</th></tr>
              </thead>
              <tbody>
                {state.readings.map((reading) => (
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
      <section className="rounded-2xl border border-white/[0.08] bg-[#0b1a29]/70 p-4 sm:p-5">
        <h2 className="text-sm font-semibold text-slate-100">Alert history</h2>
        <ul className="mt-3 divide-y divide-white/[0.06]">
          {historyAlerts.map((alert) => <li key={alert.id} className="py-3 text-xs leading-5 text-slate-400">{new Date(alert.timestamp).toLocaleString()} · {alert.title} · {alert.status}</li>)}
          {historyAlerts.length === 0 ? <li className="py-3 text-xs text-slate-500">No alerts are available for this astronaut.</li> : null}
        </ul>
      </section>
    </div>
  )
}

export default MedicalHistory
