import { useEffect, useState } from 'react'
import { useOutletContext } from 'react-router-dom'
import { api } from '../../api/client'
import ApiState from '../../components/dashboard/ApiState'
import PageHeader from '../../components/dashboard/PageHeader'

const RANGES = [
  { days: 7, label: '7 days' },
  { days: 30, label: '30 days' },
]

function HealthAnalytics() {
  const { astronautId } = useOutletContext()
  const [range, setRange] = useState(7)
  const [state, setState] = useState({ loading: true, error: '', buckets: [] })

  useEffect(() => {
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

  const types = [...new Set(state.buckets.map((bucket) => bucket.readingType))]

  return (
    <div className="space-y-5">
      <PageHeader
        eyebrow="Health analytics"
        title="Health trends"
        description="Daily summary buckets are calculated by the backend from readings you are authorized to access."
        action={
          <div className="inline-flex rounded-xl border border-white/[0.08] bg-[#0b1a29] p-1" role="group" aria-label="Select trend range">
            {RANGES.map((item) => (
              <button key={item.days} type="button" onClick={() => setRange(item.days)} aria-pressed={range === item.days} className={`min-h-9 rounded-lg px-3 text-xs font-medium ${range === item.days ? 'bg-cyan-200/10 text-cyan-100' : 'text-slate-400'}`}>
                {item.label}
              </button>
            ))}
          </div>
        }
      />
      <ApiState loading={state.loading} error={state.error} empty={state.buckets.length === 0 ? `No health summary data is available for the selected ${range}-day period.` : null}>
        <div className="space-y-4">
          {types.map((type) => (
            <section key={type} className="overflow-hidden rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85">
              <h2 className="border-b border-white/[0.06] px-4 py-3 text-sm font-semibold text-slate-100">{type}</h2>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[680px] text-left text-xs">
                  <thead className="text-[10px] uppercase tracking-[0.12em] text-slate-500">
                    <tr>
                      <th className="px-4 py-3 font-medium">Date</th>
                      <th className="px-4 py-3 font-medium">Minimum</th>
                      <th className="px-4 py-3 font-medium">Maximum</th>
                      <th className="px-4 py-3 font-medium">Average</th>
                      <th className="px-4 py-3 font-medium">Latest</th>
                      <th className="px-4 py-3 font-medium">Readings</th>
                    </tr>
                  </thead>
                  <tbody>
                    {state.buckets.filter((bucket) => bucket.readingType === type).map((bucket) => (
                      <tr key={`${type}-${bucket.bucketDate}`} className="border-t border-white/[0.06] text-slate-300">
                        <td className="px-4 py-3">{bucket.bucketDate}</td>
                        <td className="px-4 py-3">{bucket.minimumValue}</td>
                        <td className="px-4 py-3">{bucket.maximumValue}</td>
                        <td className="px-4 py-3">{bucket.averageValue}</td>
                        <td className="px-4 py-3">{bucket.latestValue}</td>
                        <td className="px-4 py-3">{bucket.readingCount}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </section>
          ))}
        </div>
      </ApiState>
    </div>
  )
}

export default HealthAnalytics
