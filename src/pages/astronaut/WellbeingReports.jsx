import { useEffect, useState } from 'react'
import { Headphones, Send, Trash2, Upload } from 'lucide-react'
import { useOutletContext } from 'react-router-dom'
import { api } from '../../api/client'
import PageHeader from '../../components/dashboard/PageHeader'

const CATEGORIES = ['TIRED', 'STRESSED', 'DIZZY', 'HEADACHE', 'PAIN', 'BREATHING_DIFFICULTY', 'OTHER']

function WellbeingReports() {
  const { reports } = useOutletContext()
  const [myReports, setMyReports] = useState(reports)
  const [category, setCategory] = useState('OTHER')
  const [reportText, setReportText] = useState('')
  const [audioDraft, setAudioDraft] = useState(null)
  const [audio, setAudio] = useState(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  useEffect(() => setMyReports(reports), [reports])
  useEffect(() => () => {
    if (audio?.url) URL.revokeObjectURL(audio.url)
  }, [audio])

  async function uploadAudio(event) {
    const file = event.target.files?.[0]
    if (!file) return
    setBusy(true)
    setError('')
    setMessage('')
    try {
      const draft = await api.uploadAudioDraft(file)
      setAudioDraft(draft)
      setMessage('Audio draft uploaded. It will be attached only after report submission.')
    } catch (requestError) {
      setError(requestError.message || 'Audio upload failed.')
    } finally {
      setBusy(false)
      event.target.value = ''
    }
  }

  async function previewDraft() {
    if (!audioDraft?.id) return
    setBusy(true)
    setError('')
    try {
      const blob = await api.previewAudioDraft(audioDraft.id)
      setAudio((current) => {
        if (current?.url) URL.revokeObjectURL(current.url)
        return { reportId: `draft-${audioDraft.id}`, url: URL.createObjectURL(blob) }
      })
    } catch (requestError) {
      setError(requestError.message || 'Audio preview failed.')
    } finally {
      setBusy(false)
    }
  }

  async function deleteDraft() {
    if (!audioDraft?.id) return
    setBusy(true)
    setError('')
    try {
      await api.deleteAudioDraft(audioDraft.id)
      setAudioDraft(null)
      if (audio?.url) URL.revokeObjectURL(audio.url)
      setAudio(null)
      setMessage('Unsubmitted audio draft deleted.')
    } catch (requestError) {
      setError(requestError.message || 'The audio draft could not be deleted.')
    } finally {
      setBusy(false)
    }
  }

  async function submitReport(event) {
    event.preventDefault()
    if (!reportText.trim() && !audioDraft?.id) {
      setError('Enter a report or attach an audio recording before submitting.')
      return
    }
    setBusy(true)
    setError('')
    setMessage('')
    try {
      const created = await api.submitWellbeingReport({
        category,
        reportText: reportText.trim() || null,
        audioRecordingId: audioDraft?.id || null,
      })
      setMyReports((current) => [created, ...current])
      setReportText('')
      setAudioDraft(null)
      setMessage('Your report was submitted to the backend.')
    } catch (requestError) {
      setError(requestError.message || 'The report could not be submitted.')
    } finally {
      setBusy(false)
    }
  }

  async function loadReportAudio(report) {
    setBusy(true)
    setError('')
    try {
      const blob = await api.wellbeingReportAudio(report.id)
      setAudio((current) => {
        if (current?.url) URL.revokeObjectURL(current.url)
        return { reportId: String(report.id), url: URL.createObjectURL(blob) }
      })
    } catch (requestError) {
      setError(requestError.message || 'Authorized audio could not be loaded.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="space-y-5">
      <PageHeader
        eyebrow="Private well-being reporting"
        title="Well-being reports"
        description="Submit a private written report or a backend-validated audio attachment. Your medical team receives the report according to backend assignment and role rules."
      />
      {error ? <p role="alert" className="rounded-xl border border-rose-300/15 bg-rose-300/[0.05] p-4 text-sm text-rose-100">{error}</p> : null}
      {message ? <p role="status" className="rounded-xl border border-emerald-200/10 bg-emerald-200/[0.04] p-4 text-sm text-emerald-100">{message}</p> : null}
      <form onSubmit={submitReport} className="space-y-4 rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4 sm:p-5">
        <label className="block">
          <span className="mb-1.5 block text-xs text-slate-400">Category</span>
          <select value={category} onChange={(event) => setCategory(event.target.value)} className="min-h-11 w-full rounded-xl border border-white/10 bg-[#071522] px-3 text-sm text-slate-100">
            {CATEGORIES.map((item) => <option key={item} value={item}>{item.replaceAll('_', ' ')}</option>)}
          </select>
        </label>
        <label className="block">
          <span className="mb-1.5 block text-xs text-slate-400">Report text (optional if audio is attached)</span>
          <textarea value={reportText} onChange={(event) => setReportText(event.target.value)} maxLength={10000} rows={5} className="w-full rounded-xl border border-white/10 bg-[#071522] px-3 py-3 text-sm leading-6 text-slate-100" />
          <span className="mt-1 block text-right text-[11px] text-slate-500">{reportText.length}/10000</span>
        </label>
        <label className="block">
          <span className="mb-1.5 block text-xs text-slate-400">Audio attachment · backend file validation applies</span>
          <span className="flex min-h-11 cursor-pointer items-center justify-center gap-2 rounded-xl border border-white/10 bg-[#071522] px-3 text-sm text-slate-300 hover:bg-white/[0.03]">
            <Upload size={16} /> Choose audio file
            <input type="file" accept="audio/*" onChange={uploadAudio} disabled={busy} className="sr-only" />
          </span>
        </label>
        {audioDraft ? (
          <div className="flex flex-wrap items-center gap-2 rounded-xl border border-violet-200/10 bg-violet-200/[0.035] p-3">
            <span className="mr-auto text-xs text-violet-100">Audio draft #{audioDraft.id} ready</span>
            <button type="button" onClick={previewDraft} disabled={busy} className="inline-flex min-h-9 items-center gap-1 rounded-lg px-2 text-xs text-violet-100"><Headphones size={14} /> Preview</button>
            <button type="button" onClick={deleteDraft} disabled={busy} className="inline-flex min-h-9 items-center gap-1 rounded-lg px-2 text-xs text-rose-200"><Trash2 size={14} /> Delete draft</button>
            {audio?.reportId === `draft-${audioDraft.id}` ? <audio controls src={audio.url} className="w-full" /> : null}
          </div>
        ) : null}
        <button type="submit" disabled={busy} className="inline-flex min-h-11 items-center justify-center gap-2 rounded-xl border border-cyan-200/15 bg-cyan-200/[0.07] px-4 text-sm font-medium text-cyan-100 disabled:opacity-50">
          <Send size={15} /> {busy ? 'Please wait…' : 'Submit report'}
        </button>
      </form>

      <section className="space-y-3">
        <h2 className="text-sm font-semibold text-slate-100">Your submitted reports</h2>
        {myReports.map((report) => (
          <article key={report.id} className="rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <p className="text-sm font-medium text-slate-100">{report.category.replaceAll('_', ' ')}</p>
              <span className="rounded-full border border-white/10 px-2.5 py-1 text-[10px] text-slate-300">{report.status.replaceAll('_', ' ')}</span>
            </div>
            <p className="mt-1 text-xs text-slate-500">{new Date(report.submittedAt).toLocaleString()}</p>
            <p className="mt-3 whitespace-pre-wrap text-sm leading-6 text-slate-300">{report.reportText || 'Audio report submitted.'}</p>
            {report.hasAudio ? (
              <div className="mt-3 flex flex-wrap items-center gap-3">
                <button type="button" onClick={() => loadReportAudio(report)} disabled={busy} className="inline-flex min-h-9 items-center gap-2 rounded-lg border border-violet-200/15 px-3 text-xs text-violet-100 disabled:opacity-50">
                  <Headphones size={14} /> Load your audio
                </button>
                {audio?.reportId === String(report.id) ? <audio controls src={audio.url} className="max-w-full" /> : null}
              </div>
            ) : null}
          </article>
        ))}
        {!myReports.length ? <p className="rounded-xl border border-white/[0.08] p-5 text-sm text-slate-400">You have not submitted a report.</p> : null}
      </section>
      <p className="text-xs leading-5 text-slate-500">The backend stores audio in private storage and applies access checks when previewing or playing recordings. Do not upload audio you are not authorized to share.</p>
    </div>
  )
}

export default WellbeingReports
