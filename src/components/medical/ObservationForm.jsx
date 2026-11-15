import { useState } from 'react'
import { Save } from 'lucide-react'

function ObservationForm({ astronauts, onSave }) {
  const [values, setValues] = useState({
    astronautId: astronauts[0]?.id || '',
    observationText: '',
    followUpDate: '',
  })
  const [errors, setErrors] = useState({})
  const [savedMessage, setSavedMessage] = useState('')
  const [isSaving, setIsSaving] = useState(false)

  function update(field, value) {
    setValues((current) => ({ ...current, [field]: value }))
    setErrors((current) => ({ ...current, [field]: '' }))
    setSavedMessage('')
  }

  async function submit(event) {
    event.preventDefault()
    const nextErrors = {}
    if (!values.astronautId) nextErrors.astronautId = 'Select an astronaut.'
    if (!values.observationText.trim()) nextErrors.observationText = 'Enter the medical observation.'
    setErrors(nextErrors)
    if (Object.keys(nextErrors).length) return

    setIsSaving(true)
    setSavedMessage('')
    try {
      await onSave({
        astronautId: Number(values.astronautId),
        observationText: values.observationText.trim(),
        followUpDate: values.followUpDate || null,
        relatedAlertId: null,
        relatedReadingId: null,
      })
      setValues((current) => ({ ...current, observationText: '', followUpDate: '' }))
      setSavedMessage('Observation saved to the authorized medical record.')
    } catch (error) {
      setSavedMessage(error.message || 'The observation could not be saved.')
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <form onSubmit={submit} noValidate className="space-y-4 rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4 sm:p-5">
      <div>
        <label htmlFor="observation-astronaut" className="mb-1.5 block text-xs font-medium text-slate-300">Assigned astronaut</label>
        <select
          id="observation-astronaut"
          value={values.astronautId}
          onChange={(event) => update('astronautId', event.target.value)}
          className="min-h-11 w-full rounded-xl border border-white/10 bg-[#071522] px-3 text-sm text-slate-100 outline-none focus:border-cyan-200/40"
        >
          {astronauts.map((astronaut) => <option key={astronaut.id} value={astronaut.id}>{astronaut.name} · {astronaut.id} · sample</option>)}
        </select>
        {errors.astronautId ? <p className="mt-1 text-xs text-rose-200">{errors.astronautId}</p> : null}
      </div>
      <div>
        <label htmlFor="observation-date" className="mb-1.5 block text-xs font-medium text-slate-300">Follow-up date (optional)</label>
        <input
          id="observation-date"
          type="date"
          value={values.followUpDate}
          onChange={(event) => update('followUpDate', event.target.value)}
          className="min-h-11 w-full rounded-xl border border-white/10 bg-[#071522] px-3 text-sm text-slate-100 outline-none [color-scheme:dark] focus:border-cyan-200/40"
        />
      </div>
      <div>
        <label htmlFor="observation-notes" className="mb-1.5 block text-xs font-medium text-slate-300">Medical observation</label>
        <textarea
          id="observation-notes"
          value={values.observationText}
          onChange={(event) => update('observationText', event.target.value)}
          maxLength={10000}
          rows={5}
          className="w-full resize-y rounded-xl border border-white/10 bg-[#071522] px-3 py-3 text-sm leading-6 text-slate-100 outline-none placeholder:text-slate-600 focus:border-cyan-200/40"
          placeholder="Enter an authorized medical observation"
          aria-invalid={Boolean(errors.observationText)}
        />
        <div className="mt-1 flex items-center justify-between gap-2">
          {errors.observationText ? <p className="text-xs text-rose-200">{errors.observationText}</p> : <p className="text-[11px] text-slate-500">Only authorized medical officers can access this record.</p>}
          <span className="text-[11px] text-slate-600">{values.observationText.length}/10000</span>
        </div>
      </div>
      {savedMessage ? <p role="status" className={`rounded-lg border px-3 py-2 text-xs leading-5 ${savedMessage.includes('could not') ? 'border-rose-200/10 bg-rose-200/[0.04] text-rose-100' : 'border-emerald-200/10 bg-emerald-200/[0.04] text-emerald-100'}`}>{savedMessage}</p> : null}
      <button type="submit" disabled={isSaving} className="inline-flex min-h-11 w-full items-center justify-center gap-2 rounded-xl border border-cyan-200/15 bg-cyan-200/[0.07] px-4 text-sm font-medium text-cyan-100 hover:bg-cyan-200/[0.11] disabled:opacity-60 sm:w-auto">
        <Save size={16} /> {isSaving ? 'Saving…' : 'Save observation'}
      </button>
    </form>
  )
}

export default ObservationForm
