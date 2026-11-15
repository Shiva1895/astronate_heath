import { useState } from 'react'
import HealthChart from '../dashboard/HealthChart'
import { MISSION_CONTROL_TRENDS, MISSION_CONTROL_TREND_RANGES } from '../../data/missionControlMockData'

function HealthTrendCharts() {
  const [range, setRange] = useState('24h')
  const data = MISSION_CONTROL_TRENDS[range]

  return (
    <section className="min-w-0" aria-labelledby="mission-trends-title">
      <div className="mb-3 flex flex-col justify-between gap-3 sm:flex-row sm:items-end">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.14em] text-cyan-200/75">Fleet health patterns · Sample data</p>
          <h2 id="mission-trends-title" className="mt-2 text-lg font-semibold text-slate-50">Health Monitoring Summary</h2>
          <p className="mt-1 text-xs leading-5 text-slate-500">Illustrative aggregate trends across the fictional astronaut roster.</p>
        </div>
        <div className="inline-flex max-w-full overflow-x-auto rounded-xl border border-white/[0.08] bg-[#0b1a29] p-1" role="group" aria-label="Select mission health trend time range">
          {MISSION_CONTROL_TREND_RANGES.map((item) => (
            <button key={item.id} type="button" onClick={() => setRange(item.id)} aria-pressed={range === item.id} className={`min-h-9 shrink-0 rounded-lg px-3 text-xs font-medium ${range === item.id ? 'bg-cyan-200/10 text-cyan-100' : 'text-slate-400 hover:text-slate-200'}`}>
              {item.label}
            </button>
          ))}
        </div>
      </div>
      <div className="grid min-w-0 grid-cols-1 gap-3 xl:grid-cols-2">
        <HealthChart title="Heart-rate trends" dataKey="heartRate" unit="BPM" color="#16c8e8" data={data} />
        <HealthChart title="SpO₂ trends" dataKey="spo2" unit="%" color="#8b83ff" data={data} />
        <HealthChart title="Body-temperature trends" dataKey="temperature" unit="°C" color="#4ea8ff" data={data} />
      </div>
      <p className="mt-3 text-[11px] leading-5 text-slate-500">Charts summarize simulated values only. A trend or flagged reading is not a diagnosis or medically validated threshold.</p>
    </section>
  )
}

export default HealthTrendCharts
