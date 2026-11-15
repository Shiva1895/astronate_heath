import { ClipboardList } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useOutletContext } from 'react-router-dom'
import { api } from '../../api/client'
import ObservationForm from '../../components/medical/ObservationForm'
import PageHeader from '../../components/dashboard/PageHeader'

function MedicalObservations() {
  const { astronauts, addObservation } = useOutletContext()
  const [astronautId, setAstronautId] = useState(astronauts[0]?.id || '')
  const [observations, setObservations] = useState([])
  const [loading, setLoading] = useState(Boolean(astronautId))
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    if (!astronautId) {
      setObservations([])
      setLoading(false)
      return () => { active = false }
    }

    setLoading(true)
    setError('')
    api.observations(astronautId)
      .then((page) => {
        if (active) setObservations(Array.isArray(page) ? page : page.content || [])
      })
      .catch((requestError) => {
        if (active) setError(requestError.message || 'Medical observations could not be loaded.')
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => { active = false }
  }, [astronautId])

  async function saveObservation(request) {
    const created = await addObservation(request)
    setObservations((current) => [created, ...current])
  }

  return (
    <div className="space-y-5">
      <PageHeader
        eyebrow="Authorized medical record"
        title="Medical Observations"
        description="Create and review persistent observations for astronauts assigned to your account. Access is checked by the backend."
      />
      <div className="grid min-w-0 grid-cols-1 gap-4 2xl:grid-cols-[minmax(0,0.9fr)_minmax(0,1.1fr)]">
        <section>
          <div className="mb-3">
            <h2 className="text-sm font-semibold text-slate-100">New observation draft</h2>
            <p className="mt-1 text-xs text-slate-500">Never enter real patient or health information.</p>
          </div>
          <ObservationForm astronauts={astronauts} onSave={saveObservation} />
        </section>
        <section>
          <div className="mb-3 flex items-center justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold text-slate-100">Medical observations</h2>
              <p className="mt-1 text-xs text-slate-500">Private records for the selected assigned astronaut</p>
            </div>
            <ClipboardList size={17} className="text-violet-100" />
          </div>
          <label className="mb-3 block">
            <span className="mb-1.5 block text-xs text-slate-400">Assigned astronaut</span>
            <select value={astronautId} onChange={(event) => setAstronautId(event.target.value)} className="min-h-11 w-full rounded-xl border border-white/10 bg-[#071522] px-3 text-sm text-slate-100">
              {astronauts.map((astronaut) => <option key={astronaut.id} value={astronaut.id}>{astronaut.name} · {astronaut.id}</option>)}
            </select>
          </label>
          {loading ? <p role="status" className="py-5 text-sm text-slate-400">Loading private observations…</p> : null}
          {error ? <p role="alert" className="rounded-xl border border-rose-300/15 p-4 text-sm text-rose-100">{error}</p> : null}
          <div className="space-y-3">
            {!loading && !error ? observations.map((observation) => (
              <article key={observation.id} className="rounded-xl border border-white/[0.07] bg-[#0b1a29]/80 p-4">
                <div className="flex flex-wrap items-start justify-between gap-2">
                  <div className="min-w-0">
                    <h3 className="text-sm font-medium text-slate-100">Observation #{observation.id}</h3>
                    <p className="mt-1 text-xs text-slate-500">{astronauts.find((item) => item.id === String(observation.astronautId))?.name || 'Assigned astronaut'} · {observation.astronautId}</p>
                  </div>
                  {observation.followUpDate ? <span className="rounded-full border border-white/[0.08] px-2.5 py-1 text-[10px] text-slate-400">Follow-up {observation.followUpDate}</span> : null}
                </div>
                <p className="mt-3 whitespace-pre-wrap text-xs leading-5 text-slate-300">{observation.observationText}</p>
                <p className="mt-3 border-t border-white/[0.06] pt-3 text-[11px] text-slate-500">{new Date(observation.createdAt).toLocaleString()}</p>
              </article>
            )) : null}
            {!loading && !error && observations.length === 0 ? <p className="rounded-xl border border-white/[0.07] p-5 text-sm text-slate-500">No observations are recorded for this astronaut.</p> : null}
          </div>
        </section>
      </div>
    </div>
  )
}

export default MedicalObservations
