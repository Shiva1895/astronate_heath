import { useEffect, useMemo, useState } from 'react'
import { useSearchParams, useOutletContext } from 'react-router-dom'
import { api } from '../../api/client'
import MedicalAlertDetails from '../../components/medical/MedicalAlertDetails'
import MedicalAlertTable from '../../components/medical/MedicalAlertTable'
import MedicalSummaryCard from '../../components/medical/MedicalSummaryCard'
import PageHeader from '../../components/dashboard/PageHeader'
import { CircleAlert, Check, Clock3 } from 'lucide-react'
import { getAstronautName } from '../../components/medical/medicalHelpers'

function MedicalAlertManagement() {
  const { alerts, updateAlertStatus } = useOutletContext()
  const [searchParams, setSearchParams] = useSearchParams()
  const [severity, setSeverity] = useState('all')
  const [status, setStatus] = useState('all')
  const [astronautQuery, setAstronautQuery] = useState('')
  const [updatingAlertId, setUpdatingAlertId] = useState('')
  const focusedId = searchParams.get('focus')
  const selectedAlert = alerts.find((alert) => alert.id === focusedId) || null
  const selectedAlertId = selectedAlert?.id || ''
  const selectedAlertStatus = selectedAlert?.status || ''
  const historyRequestKey = selectedAlertId ? `${selectedAlertId}:${selectedAlertStatus}` : ''
  const [historyState, setHistoryState] = useState({ key: '', error: '', entries: [] })

  useEffect(() => {
    let active = true
    if (!selectedAlertId) return () => { active = false }
    api.alertHistory(selectedAlertId)
      .then((page) => {
        if (active) setHistoryState({ key: historyRequestKey, error: '', entries: Array.isArray(page) ? page : page.content || [] })
      })
      .catch((error) => {
        if (active) setHistoryState({ key: historyRequestKey, error: error.message || 'Alert history could not be loaded.', entries: [] })
      })
    return () => { active = false }
  }, [selectedAlertId, selectedAlertStatus, historyRequestKey])
  const currentHistory = historyState.key === historyRequestKey ? historyState.entries : []
  const historyLoading = Boolean(historyRequestKey) && historyState.key !== historyRequestKey
  const historyError = historyState.key === historyRequestKey ? historyState.error : ''

  async function changeAlertStatus(alertId, nextStatus) {
    setUpdatingAlertId(alertId)
    try {
      await updateAlertStatus(alertId, nextStatus)
    } finally {
      setUpdatingAlertId('')
    }
  }

  const filteredAlerts = useMemo(() => alerts.filter((alert) => {
    const matchesSeverity = severity === 'all' || alert.severity === severity
    const matchesStatus = status === 'all' || alert.status === status
    const matchesAstronaut = `${alert.astronautId} ${getAstronautName(alert.astronautId)}`.toLowerCase().includes(astronautQuery.trim().toLowerCase())
    return matchesSeverity && matchesStatus && matchesAstronaut
  }), [alerts, severity, status, astronautQuery])

  const summaries = [
    { id: 'new', label: 'New', value: alerts.filter((alert) => alert.status === 'New').length, detail: 'Open backend alerts', icon: CircleAlert, tone: 'amber' },
    { id: 'acknowledged', label: 'Acknowledged', value: alerts.filter((alert) => alert.status === 'Acknowledged').length, detail: 'Backend review in progress', icon: Clock3, tone: 'cyan' },
    { id: 'resolved', label: 'Resolved', value: alerts.filter((alert) => alert.status === 'Resolved').length, detail: 'Backend resolved alerts', icon: Check, tone: 'mint' },
  ]

  function closeDetails() {
    setSearchParams((current) => {
      const next = new URLSearchParams(current)
      next.delete('focus')
      return next
    })
  }

  return (
    <div className="space-y-5">
      <PageHeader
        eyebrow="Alert triage"
        title="Alert Management"
        description="Review authorized alert records and persist supported workflow transitions to the backend."
      />
      <div className="grid min-w-0 grid-cols-1 gap-3 sm:grid-cols-3">
        {summaries.map((summary) => <MedicalSummaryCard key={summary.id} summary={summary} />)}
      </div>
      <section className="grid min-w-0 grid-cols-1 gap-3 rounded-2xl border border-white/[0.08] bg-[#0b1a29]/65 p-4 sm:grid-cols-2 xl:grid-cols-3" aria-label="Alert filters">
        <label className="min-w-0">
          <span className="mb-1.5 block text-xs font-medium text-slate-400">Search astronaut</span>
          <input value={astronautQuery} onChange={(event) => setAstronautQuery(event.target.value)} placeholder="Name or astronaut ID" className="min-h-11 w-full rounded-xl border border-white/10 bg-[#071522] px-3 text-sm text-slate-100 outline-none placeholder:text-slate-600 focus:border-cyan-200/35" />
        </label>
        <label className="min-w-0">
          <span className="mb-1.5 block text-xs font-medium text-slate-400">Severity</span>
          <select value={severity} onChange={(event) => setSeverity(event.target.value)} className="min-h-11 w-full rounded-xl border border-white/10 bg-[#071522] px-3 text-sm text-slate-100 outline-none focus:border-cyan-200/35">
            <option value="all">All severities</option>
            <option value="Critical">Critical</option>
            <option value="Warning">Warning</option>
            <option value="Information">Information</option>
          </select>
        </label>
        <label className="min-w-0 sm:col-span-2 xl:col-span-1">
          <span className="mb-1.5 block text-xs font-medium text-slate-400">Status</span>
          <select value={status} onChange={(event) => setStatus(event.target.value)} className="min-h-11 w-full rounded-xl border border-white/10 bg-[#071522] px-3 text-sm text-slate-100 outline-none focus:border-cyan-200/35">
            <option value="all">All statuses</option>
            <option value="New">New</option>
            <option value="Acknowledged">Acknowledged</option>
            <option value="Escalated">Escalated</option>
            <option value="Resolved">Resolved</option>
            <option value="Closed">Closed</option>
          </select>
        </label>
      </section>
      <p className="text-xs text-slate-500">Showing {filteredAlerts.length} authorized alert{filteredAlerts.length === 1 ? '' : 's'}.</p>
      <MedicalAlertTable alerts={filteredAlerts} onSelect={(alert) => setSearchParams({ focus: alert.id })} />

      <p className="text-xs leading-5 text-slate-500">Alert priority indicates a backend workflow signal and is not a diagnosis.</p>

      {selectedAlert ? (
        <MedicalAlertDetails
          alert={selectedAlert}
          history={currentHistory}
          historyLoading={historyLoading}
          historyError={historyError}
          updating={updatingAlertId === selectedAlert.id}
          onClose={closeDetails}
          onAcknowledge={(alert) => changeAlertStatus(alert.id, 'Acknowledged')}
          onResolve={(alert) => changeAlertStatus(alert.id, 'Resolved')}
        />
      ) : null}
    </div>
  )
}

export default MedicalAlertManagement
