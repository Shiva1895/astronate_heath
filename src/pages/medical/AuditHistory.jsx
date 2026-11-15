import { useEffect, useState } from 'react'
import { api } from '../../api/client'
import ApiState from '../../components/dashboard/ApiState'
import PageHeader from '../../components/dashboard/PageHeader'

function AuditHistory() {
  const [state, setState] = useState({ loading: true, error: '', entries: [] })

  useEffect(() => {
    let active = true
    api.auditEvents()
      .then((page) => {
        if (active) setState({ loading: false, error: '', entries: page.content || [] })
      })
      .catch((error) => {
        if (active) setState({ loading: false, error: error.message || 'Audit history could not be loaded.', entries: [] })
      })
    return () => { active = false }
  }, [])

  return (
    <div className="space-y-5">
      <PageHeader
        eyebrow="Administrator-only"
        title="Audit history"
        description="Review recorded platform actions. Audit metadata is provided by the backend and excludes credentials and private report contents."
      />
      <ApiState loading={state.loading} error={state.error} empty={state.entries.length === 0 ? 'No audit events are available.' : null}>
        <div className="overflow-hidden rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85">
          <div className="overflow-x-auto">
            <table className="w-full min-w-[720px] text-left text-sm">
              <thead className="bg-white/[0.025] text-[10px] uppercase tracking-[0.12em] text-slate-500">
                <tr>
                  <th className="px-4 py-3 font-medium">Time</th>
                  <th className="px-4 py-3 font-medium">Actor</th>
                  <th className="px-4 py-3 font-medium">Action</th>
                  <th className="px-4 py-3 font-medium">Resource</th>
                  <th className="px-4 py-3 font-medium">Metadata</th>
                </tr>
              </thead>
              <tbody>
                {state.entries.map((entry) => (
                  <tr key={entry.id} className="border-t border-white/[0.06] align-top text-xs text-slate-300">
                    <td className="whitespace-nowrap px-4 py-3">{new Date(entry.occurredAt).toLocaleString()}</td>
                    <td className="px-4 py-3">{entry.actorId ?? 'System'}</td>
                    <td className="px-4 py-3">{entry.action}</td>
                    <td className="px-4 py-3">{entry.resourceType} · {entry.resourceId ?? '—'}</td>
                    <td className="max-w-sm break-words px-4 py-3 text-slate-500">{entry.metadata || '—'}</td>
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

export default AuditHistory
