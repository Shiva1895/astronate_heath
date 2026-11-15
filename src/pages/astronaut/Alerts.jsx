import { AlertTriangle } from 'lucide-react'
import { useOutletContext } from 'react-router-dom'
import ApiState from '../../components/dashboard/ApiState'
import PageHeader from '../../components/dashboard/PageHeader'

function Alerts() {
  const { alerts } = useOutletContext()

  return (
    <div className="space-y-5">
      <PageHeader
        eyebrow="Personal alert center"
        title="Review alerts"
        description="Alerts are backend workflow records associated with your profile. They are operational flags, not medical diagnoses."
      />
      <ApiState empty={alerts.length === 0 ? 'No alerts are available for your profile.' : null}>
        <div className="space-y-3">
          {alerts.map((alert) => (
            <article key={alert.id} className="rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4 sm:p-5">
              <div className="flex items-start gap-3">
                <AlertTriangle size={18} className={alert.severity === 'Critical' ? 'mt-0.5 shrink-0 text-rose-200' : 'mt-0.5 shrink-0 text-amber-100'} />
                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <h2 className="text-sm font-semibold text-slate-100">{alert.title}</h2>
                    <span className="rounded-full border border-white/10 px-2.5 py-1 text-[10px] text-slate-300">{alert.status}</span>
                  </div>
                  <p className="mt-1 text-xs text-slate-500">{alert.severity} · {new Date(alert.timestamp).toLocaleString()}</p>
                  <p className="mt-3 whitespace-pre-wrap text-sm leading-6 text-slate-300">{alert.explanation}</p>
                </div>
              </div>
            </article>
          ))}
        </div>
      </ApiState>
    </div>
  )
}

export default Alerts
