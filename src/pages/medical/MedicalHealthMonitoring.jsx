import { useEffect, useState } from 'react'
import { Activity } from 'lucide-react'
import { useOutletContext } from 'react-router-dom'
import { api } from '../../api/client'
import ApiState from '../../components/dashboard/ApiState'
import PageHeader from '../../components/dashboard/PageHeader'

const RANGES = [{ days: 7, label: '7 days' }, { days: 30, label: '30 days' }]

function MedicalHealthMonitoring() {
  const { astronauts } = useOutletContext()
  const [astronautId, setAstronautId] = useState('')
  const [range, setRange] = useState(7)
  const [state, setState] = useState({ loading: false, error: '', buckets: [] })

  useEffect(() => {
    setAstronautId((current) => current || astronauts[0]?.id || '')
  }, [astronauts])

  useEffect(() => {
    if (!astronautId) return
    let active = true
    const to = new Date()
    const from = new Date(to.getTime() - range * 24 * 60 * 60 * 1000)
    setState({ loading: true, error: '', buckets: [] })
    api.healthSummary(astronautId, from.toISOString(), to.toISOString())
      .then((buckets) => {
        if (active) setState({ loading: false, error: '', buckets })
      })
      .catch((error) => {
        if (active) setState({ loading: false, error: error.message || 'Health summaries could not be loaded.', buckets: [] })
      })
    return () => { active = false }
  }, [astronautId, range])

  const astronaut = astronauts.find((item) => item.id === astronautId)

  return (
    <div className="space-y-5">
      <PageHeader
        eyebrow="Assigned astronaut health summaries"
        title="Health Monitoring"
        description="Daily summary values are aggregated by the backend from readings accessible to your medical-officer account."
      />
      <section className="grid gap-3 rounded-2xl border border-white/[0.08] bg-[#0b1a29]/65 p-4 sm:grid-cols-2 xl:grid-cols-[minmax(0,1fr)_auto] xl:items-end">
        <label>
          <span className="mb-1.5 block text-xs text-slate-400">Assigned astronaut</span>
          <select value={astronautId} onChange={(event) => setAstronautId(event.target.value)} className="min-h-11 w-full rounded-xl border border-white/10 bg-[#071522] px-3 text-sm text-slate-100">
            {astronauts.map((item) => <option key={item.id} value={item.id}>{item.name} · {item.id}</option>)}
          </select>
        </label>
        <div className="inline-flex min-h-11 items-center gap-2 rounded-xl border border-cyan-200/10 bg-cyan-200/[0.035] px-3 text-xs text-cyan-100"><Activity size={15} />{astronaut?.name || 'Assigned astronaut'}</div>
      </section>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-xs text-slate-500">Summary range: last {range} days · UTC</p>
        <div className="inline-flex rounded-xl border border-white/[0.08] bg-[#0b1a29] p-1" role="group" aria-label="Select health summary range">
          {RANGES.map((item) => <button key={item.days} type="button" onClick={() => setRange(item.days)} aria-pressed={range === item.days} className={`min-h-9 rounded-lg px-3 text-xs font-medium ${range === item.days ? 'bg-cyan-200/10 text-cyan-100' : 'text-slate-400'}`}>{item.label}</button>)}
        </div>
      </div>
      <ApiState loading={state.loading} error={state.error} empty={state.buckets.length === 0 ? 'No health summaries are available for this astronaut and period.' : null}>
        <div className="space-y-3">
          {state.buckets.map((bucket) => (
            <article key={`${bucket.readingType}-${bucket.bucketDate}`} className="grid gap-3 rounded-xl border border-white/[0.08] bg-[#0b1a29]/85 p-4 sm:grid-cols-2 xl:grid-cols-6">
              <div><p className="text-xs text-slate-500">Date</p><p className="mt-1 text-sm text-slate-200">{bucket.bucketDate}</p></div>
              <div><p className="text-xs text-slate-500">Reading</p><p className="mt-1 text-sm font-medium text-slate-100">{bucket.readingType}</p></div>
              <div><p className="text-xs text-slate-500">Minimum</p><p className="mt-1 text-sm text-slate-200">{bucket.minimumValue}</p></div>
              <div><p className="text-xs text-slate-500">Maximum</p><p className="mt-1 text-sm text-slate-200">{bucket.maximumValue}</p></div>
              <div><p className="text-xs text-slate-500">Average / latest</p><p className="mt-1 text-sm text-slate-200">{bucket.averageValue} / {bucket.latestValue}</p></div>
              <div><p className="text-xs text-slate-500">Count / timestamp</p><p className="mt-1 text-xs text-slate-200">{bucket.readingCount} · {new Date(bucket.latestRecordedAt).toLocaleString()}</p></div>
            </article>
          ))}
        </div>
      </ApiState>
      <p className="text-xs leading-5 text-slate-500">These backend summaries are operational data and do not provide diagnoses or medically validated thresholds.</p>
    </div>
  )
}

export default MedicalHealthMonitoring
