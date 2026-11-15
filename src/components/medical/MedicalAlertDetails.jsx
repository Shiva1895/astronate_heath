import { Check, Clock3, X } from 'lucide-react'
import { getAstronautName } from './medicalHelpers'

function MedicalAlertDetails({ alert, history, historyLoading, historyError, updating, onClose, onAcknowledge, onResolve }) {
  if (!alert) return null

  const timestamp = new Date(alert.timestamp).toLocaleString('en-GB', {
    dateStyle: 'medium',
    timeStyle: 'short',
    timeZone: 'UTC',
  })
  return (
    <div className="fixed inset-0 z-[70] flex items-end justify-center bg-black/65 p-0 backdrop-blur-sm sm:items-center sm:p-5" role="presentation" onMouseDown={(event) => {
      if (event.target === event.currentTarget) onClose()
    }}>
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby="medical-alert-title"
        className="max-h-[92vh] w-full max-w-2xl overflow-y-auto rounded-t-2xl border border-white/10 bg-[#0b1a29] p-5 shadow-2xl shadow-black/50 sm:rounded-2xl sm:p-6"
      >
        <div className="flex items-start justify-between gap-4">
          <div className="min-w-0">
            <p className="text-xs font-medium uppercase tracking-[0.14em] text-cyan-200/80">Alert #{alert.id}</p>
            <h2 id="medical-alert-title" className="mt-2 text-xl font-semibold text-slate-50">{alert.title}</h2>
          </div>
          <button type="button" onClick={onClose} className="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-xl text-slate-400 hover:bg-white/[0.06] hover:text-white" aria-label="Close alert details">
            <X size={18} />
          </button>
        </div>
        <div className="mt-5 grid grid-cols-1 gap-4 rounded-xl border border-white/[0.07] bg-white/[0.02] p-4 sm:grid-cols-2">
          <div>
            <p className="text-xs text-slate-500">Astronaut</p>
            <p className="mt-1 text-sm font-medium text-slate-200">{getAstronautName(alert.astronautId, alert.astronautName)} · {alert.astronautId}</p>
          </div>
          <div>
            <p className="text-xs text-slate-500">Observed at</p>
            <p className="mt-1 text-sm font-medium text-slate-200">{timestamp} UTC</p>
          </div>
          <div>
            <p className="text-xs text-slate-500">Severity</p>
            <p className="mt-1 text-sm font-medium text-slate-200">{alert.severity} priority</p>
          </div>
          <div>
            <p className="text-xs text-slate-500">Current status</p>
            <p className="mt-1 text-sm font-medium text-slate-200">{alert.status}</p>
          </div>
        </div>
        <div className="mt-4 rounded-xl border border-amber-200/10 bg-amber-200/[0.035] p-4">
          <p className="text-xs font-semibold text-amber-100">Review context</p>
          <p className="mt-2 text-sm leading-6 text-slate-300">{alert.explanation}</p>
          <p className="mt-2 text-xs leading-5 text-slate-500">An alert is an operational workflow signal, not a diagnosis or treatment recommendation.</p>
        </div>
        <div className="mt-5 flex flex-wrap gap-2">
          {alert.status === 'New' ? (
            <button type="button" onClick={() => onAcknowledge(alert)} disabled={updating} className="inline-flex min-h-11 items-center gap-2 rounded-xl border border-cyan-200/15 bg-cyan-200/[0.06] px-4 text-sm font-medium text-cyan-100 hover:bg-cyan-200/[0.1] disabled:opacity-50">
              <Check size={16} /> {updating ? 'Updating…' : 'Acknowledge alert'}
            </button>
          ) : null}
          {['Acknowledged', 'Escalated'].includes(alert.status) ? (
            <button type="button" onClick={() => onResolve(alert)} disabled={updating} className="inline-flex min-h-11 items-center gap-2 rounded-xl border border-emerald-200/15 bg-emerald-200/[0.06] px-4 text-sm font-medium text-emerald-100 hover:bg-emerald-200/[0.1] disabled:opacity-50">
              <Check size={16} /> {updating ? 'Updating…' : 'Mark resolved'}
            </button>
          ) : null}
          {alert.status === 'Resolved' ? <p className="inline-flex min-h-11 items-center gap-2 text-sm text-emerald-200"><Check size={16} /> Resolved</p> : null}
          {alert.status === 'Closed' ? <p className="inline-flex min-h-11 items-center gap-2 text-sm text-slate-400">Closed</p> : null}
        </div>
        <div className="mt-6 border-t border-white/[0.07] pt-4">
          <h3 className="flex items-center gap-2 text-sm font-semibold text-slate-200"><Clock3 size={15} /> Alert history</h3>
          {historyLoading ? <p className="mt-2 text-xs text-slate-500">Loading history…</p> : null}
          {historyError ? <p role="alert" className="mt-2 text-xs text-rose-200">{historyError}</p> : null}
          {!historyLoading && !historyError && history.length ? (
            <ul className="mt-3 space-y-2">
              {history.map((item) => <li key={item.id} className="text-xs leading-5 text-slate-400">{new Date(item.occurredAt).toLocaleString()} · {item.action} · actor #{item.actorUserId ?? 'system'}{item.details ? ` · ${item.details}` : ''}</li>)}
            </ul>
          ) : null}
          {!historyLoading && !historyError && history.length === 0 ? <p className="mt-2 text-xs text-slate-500">No history events are available.</p> : null}
        </div>
      </section>
    </div>
  )
}

export default MedicalAlertDetails
