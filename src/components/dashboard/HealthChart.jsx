import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'

function HealthChart({ title, dataKey, unit, color, data, compact = false }) {
  return (
    <section className="min-w-0 rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4 sm:p-5">
      <div className="mb-4 flex items-start justify-between gap-3">
        <div className="min-w-0">
          <h3 className="truncate text-sm font-semibold text-slate-100">{title}</h3>
          <p className="mt-1 text-xs text-slate-500">Illustrative history · {unit}</p>
        </div>
        <span className="shrink-0 rounded-full border border-white/[0.08] px-2 py-1 text-[10px] text-slate-500">Sample</span>
      </div>
      <div className={compact ? 'h-40 w-full' : 'h-56 w-full'}>
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={data} margin={{ top: 8, right: 4, left: -24, bottom: 0 }}>
            <defs>
              <linearGradient id={`fill-${dataKey}`} x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor={color} stopOpacity={0.28} />
                <stop offset="95%" stopColor={color} stopOpacity={0.015} />
              </linearGradient>
            </defs>
            <CartesianGrid stroke="rgba(148, 163, 184, 0.1)" vertical={false} />
            <XAxis
              dataKey="time"
              axisLine={false}
              tickLine={false}
              tick={{ fill: '#71859a', fontSize: 10 }}
              minTickGap={compact ? 24 : 12}
            />
            <YAxis
              axisLine={false}
              tickLine={false}
              tick={{ fill: '#71859a', fontSize: 10 }}
              width={34}
              domain={['dataMin - 1', 'dataMax + 1']}
            />
            <Tooltip
              contentStyle={{
                background: '#0b1b2b',
                border: '1px solid rgba(255,255,255,0.12)',
                borderRadius: '12px',
                color: '#e8f4ff',
                fontSize: '12px',
              }}
              labelStyle={{ color: '#91a6bc', marginBottom: 4 }}
              formatter={(value) => [`${value} ${unit}`, title]}
            />
            <Area
              type="monotone"
              dataKey={dataKey}
              stroke={color}
              strokeWidth={2}
              fill={`url(#fill-${dataKey})`}
              activeDot={{ r: 4, strokeWidth: 0, fill: color }}
              dot={false}
              isAnimationActive={false}
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </section>
  )
}

export default HealthChart
