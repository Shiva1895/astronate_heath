import { useEffect, useState } from 'react'
import { Download, FileText, Headphones, ShieldAlert } from 'lucide-react'
import { useOutletContext } from 'react-router-dom'
import PageHeader from '../../components/dashboard/PageHeader'
import { api } from '../../api/client'

const REVIEW_STATUSES = ['UNDER_REVIEW', 'REVIEWED', 'FOLLOW_UP_REQUIRED', 'CLOSED']

function MedicalReports() {
  const { astronauts, reports, reviewReport } = useOutletContext()
  const [astronautId, setAstronautId] = useState('')
  const [audio, setAudio] = useState(null)
  const [busyId, setBusyId] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    setAstronautId((current) => current || astronauts[0]?.id || '')
  }, [astronauts])

  useEffect(() => () => {
    if (audio?.url) URL.revokeObjectURL(audio.url)
  }, [audio])

  const selectedAstronaut = astronauts.find((item) => item.id === astronautId)
  const visibleReports = reports.filter((report) => !astronautId || String(report.astronautId) === astronautId)

  async function playAudio(report) {
    setBusyId(String(report.id))
    setError('')
    try {
      const audio = await api.wellbeingReportAudio(report.id)
      setAudio((current) => {
        if (current?.url) URL.revokeObjectURL(current.url)
        return { reportId: String(report.id), url: URL.createObjectURL(audio) }
      })
    } catch (requestError) {
      setError(requestError.message || 'Authorized audio could not be loaded.')
    } finally {
      setBusyId('')
    }
  }

  async function downloadReport() {
    if (!selectedAstronaut) return
    setBusyId('export')
    setError('')
    try {
      const file = await api.exportWellbeingReport(selectedAstronaut.id, 'PDF')
      const url = URL.createObjectURL(file)
      const link = document.createElement('a')
      link.href = url
      link.download = `wellbeing-${selectedAstronaut.id}.pdf`
      link.click()
      URL.revokeObjectURL(url)
    } catch (requestError) {
      setError(requestError.message || 'The report export could not be downloaded.')
    } finally {
      setBusyId('')
    }
  }

  async function submitReview(event, report) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    setBusyId(String(report.id))
    setError('')
    try {
      await reviewReport(report.id, {
        status: form.get('status'),
        reviewNotes: form.get('reviewNotes') || null,
        observationText: form.get('observationText') || null,
        followUpAssigneeId: null,
        followUpDueDate: null,
      })
    } catch (requestError) {
      setError(requestError.message || 'The report review could not be saved.')
    } finally {
      setBusyId('')
    }
  }

  return (
    <div className="space-y-5">
      <PageHeader
        eyebrow="Private well-being reports"
        title="Well-being reports"
        description="Review reports and play attached audio only when permitted by the backend. Access is limited to assigned medical officers and administrators."
        action={
          <button type="button" onClick={downloadReport} disabled={!selectedAstronaut || busyId === 'export'} className="inline-flex min-h-11 items-center gap-2 rounded-xl border border-cyan-200/15 bg-cyan-200/[0.07] px-4 text-sm font-medium text-cyan-100 disabled:opacity-50">
            <Download size={16} /> {busyId === 'export' ? 'Preparing…' : 'Export PDF'}
          </button>
        }
      />
      {error ? <p role="alert" className="rounded-xl border border-rose-300/15 bg-rose-300/[0.05] p-4 text-sm text-rose-100">{error}</p> : null}
      <label className="block max-w-lg">
        <span className="mb-1.5 block text-xs text-slate-400">Assigned astronaut</span>
        <select value={astronautId} onChange={(event) => setAstronautId(event.target.value)} className="min-h-11 w-full rounded-xl border border-white/10 bg-[#071522] px-3 text-sm text-slate-100">
          {astronauts.map((item) => <option key={item.id} value={item.id}>{item.name} · {item.id}</option>)}
        </select>
      </label>
      <div className="space-y-3">
        {visibleReports.map((report) => (
          <article key={report.id} className="rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4 sm:p-5">
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div>
                <p className="text-[10px] uppercase tracking-[0.14em] text-cyan-200/80">Report #{report.id} · {report.category}</p>
                <p className="mt-1 text-xs text-slate-500">{selectedAstronaut?.name || `Astronaut ${report.astronautId}`} · {new Date(report.submittedAt).toLocaleString()}</p>
              </div>
              <span className="rounded-full border border-white/10 px-2.5 py-1 text-xs text-slate-300">{report.status.replaceAll('_', ' ')}</span>
            </div>
            <p className="mt-4 whitespace-pre-wrap text-sm leading-6 text-slate-300">{report.reportText || 'No written report was submitted.'}</p>
            {report.hasAudio ? (
              <div className="mt-4 flex flex-wrap items-center gap-3">
                <button type="button" onClick={() => playAudio(report)} disabled={Boolean(busyId)} className="inline-flex min-h-10 items-center gap-2 rounded-lg border border-violet-200/15 px-3 text-xs font-medium text-violet-100 hover:bg-violet-200/[0.06] disabled:opacity-50">
                  <Headphones size={15} /> {busyId === String(report.id) ? 'Loading audio…' : 'Load authorized audio'}
                </button>
                {audio?.reportId === String(report.id) ? <audio controls src={audio.url} className="max-w-full" /> : null}
              </div>
            ) : null}
            <details className="mt-4 border-t border-white/[0.07] pt-4">
              <summary className="cursor-pointer text-sm font-medium text-cyan-100">Review report</summary>
              <form onSubmit={(event) => submitReview(event, report)} className="mt-4 grid gap-3">
                <label className="block">
                  <span className="mb-1 block text-xs text-slate-400">Status</span>
                  <select name="status" defaultValue={report.status} className="min-h-10 w-full rounded-lg border border-white/10 bg-[#071522] px-3 text-sm text-slate-100">
                    {[...new Set([report.status, ...REVIEW_STATUSES])].map((status) => <option key={status} value={status}>{status.replaceAll('_', ' ')}</option>)}
                  </select>
                </label>
                <label className="block">
                  <span className="mb-1 block text-xs text-slate-400">Review notes</span>
                  <textarea name="reviewNotes" maxLength={2000} rows={2} className="w-full rounded-lg border border-white/10 bg-[#071522] px-3 py-2 text-sm text-slate-100" />
                </label>
                <label className="block">
                  <span className="mb-1 block text-xs text-slate-400">Private medical observation (optional)</span>
                  <textarea name="observationText" maxLength={10000} rows={3} className="w-full rounded-lg border border-white/10 bg-[#071522] px-3 py-2 text-sm text-slate-100" />
                </label>
                <button type="submit" disabled={Boolean(busyId)} className="inline-flex min-h-10 items-center justify-center gap-2 rounded-lg border border-cyan-200/15 px-3 text-xs font-medium text-cyan-100 disabled:opacity-50">
                  <FileText size={14} /> Save review
                </button>
              </form>
            </details>
          </article>
        ))}
        {!visibleReports.length ? <p className="rounded-xl border border-white/[0.08] p-6 text-sm text-slate-400">No well-being reports are available for this astronaut.</p> : null}
      </div>
      <div className="flex items-start gap-2 rounded-xl border border-amber-200/10 bg-amber-200/[0.035] p-4 text-xs leading-5 text-amber-100/80">
        <ShieldAlert size={16} className="mt-0.5 shrink-0" />
        Report text and audio are private medical data. Audio is fetched only on request and is not retained after leaving this page.
      </div>
    </div>
  )
}

export default MedicalReports
