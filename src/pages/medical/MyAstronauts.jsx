import { useEffect, useMemo, useState } from 'react'
import { Search, UserPlus } from 'lucide-react'
import { useOutletContext } from 'react-router-dom'
import { api } from '../../api/client'
import { useAuth } from '../../auth/AuthProvider'
import ApiState from '../../components/dashboard/ApiState'
import PageHeader from '../../components/dashboard/PageHeader'
import AstronautCard from '../../components/medical/AstronautCard'

const EMPTY_FORM = { astronautCode: '', fullName: '', email: '', status: 'ACTIVE' }

function MyAstronauts() {
  const { astronauts: assignedAstronauts } = useOutletContext()
  const { user } = useAuth()
  const isAdministrator = user.roles.includes('ADMINISTRATOR')
  const [query, setQuery] = useState('')
  const [astronauts, setAstronauts] = useState([])
  const [form, setForm] = useState(EMPTY_FORM)
  const [editingId, setEditingId] = useState('')
  const [loading, setLoading] = useState(isAdministrator)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  useEffect(() => {
    if (!isAdministrator) return undefined
    let active = true
    api.astronauts('?page=0&size=100&sortBy=astronautCode&direction=ASC')
      .then((page) => {
        if (active) setAstronauts((page.content || []).map((item) => ({
          ...item,
          id: String(item.id),
          name: item.fullName,
          avatar: item.fullName.split(/\s+/).map((part) => part[0]).join('').slice(0, 2).toUpperCase(),
          mission: 'No mission details',
          monitoring: item.status,
          latestUpdate: 'Not available',
          alertSummary: 'Manage profile',
          alertTone: 'neutral',
        })))
      })
      .catch((requestError) => {
        if (active) setError(requestError.message || 'Astronaut records could not be loaded.')
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => { active = false }
  }, [isAdministrator])

  const roster = isAdministrator ? astronauts : assignedAstronauts
  const filteredAstronauts = useMemo(() => roster.filter((astronaut) => (
    `${astronaut.name || astronaut.fullName} ${astronaut.id} ${astronaut.astronautCode || ''}`.toLowerCase().includes(query.trim().toLowerCase())
  )), [roster, query])

  function startEdit(astronaut) {
    setEditingId(String(astronaut.id))
    setForm({
      astronautCode: astronaut.astronautCode,
      fullName: astronaut.fullName || astronaut.name,
      email: astronaut.email || '',
      status: astronaut.status || 'ACTIVE',
    })
    setError('')
    setMessage('')
  }

  function resetForm() {
    setEditingId('')
    setForm(EMPTY_FORM)
  }

  async function saveAstronaut(event) {
    event.preventDefault()
    setSaving(true)
    setError('')
    setMessage('')
    try {
      if (editingId) {
        await api.updateAstronaut(editingId, form)
        setMessage('Astronaut profile updated.')
      } else {
        await api.createAstronaut(form)
        setMessage('Astronaut profile created.')
      }
      const page = await api.astronauts('?page=0&size=100&sortBy=astronautCode&direction=ASC')
      setAstronauts((page.content || []).map((item) => ({
        ...item,
        id: String(item.id),
        name: item.fullName,
        avatar: item.fullName.split(/\s+/).map((part) => part[0]).join('').slice(0, 2).toUpperCase(),
        mission: 'No mission details',
        monitoring: item.status,
        latestUpdate: 'Not available',
        alertSummary: 'Manage profile',
        alertTone: 'neutral',
      })))
      resetForm()
    } catch (requestError) {
      setError(requestError.message || 'Astronaut profile could not be saved.')
    } finally {
      setSaving(false)
    }
  }

  async function deleteAstronaut(astronaut) {
    if (!window.confirm(`Delete astronaut ${astronaut.fullName || astronaut.name} (profile #${astronaut.id})? The backend may reject deletion if retained records reference it.`)) return
    setError('')
    setMessage('')
    try {
      await api.deleteAstronaut(astronaut.id)
      setAstronauts((current) => current.filter((item) => item.id !== String(astronaut.id)))
      setMessage('Astronaut profile deleted.')
    } catch (requestError) {
      setError(requestError.message || 'Astronaut profile could not be deleted.')
    }
  }

  return (
    <div className="space-y-5">
      <PageHeader
        eyebrow={isAdministrator ? 'Administrator astronaut management' : 'Assigned astronaut roster'}
        title={isAdministrator ? 'Astronaut Management' : 'My Astronauts'}
        description={isAdministrator ? 'Create, update, and remove astronaut profiles through the administrator-only API.' : 'Review astronauts assigned to your account. Access to each profile is enforced by backend assignment checks.'}
      />
      {!isAdministrator && error ? <p role="alert" className="rounded-xl border border-rose-300/15 bg-rose-300/[0.05] p-4 text-sm text-rose-100">{error}</p> : null}
      {message ? <p role="status" className="rounded-xl border border-emerald-200/10 bg-emerald-200/[0.04] p-3 text-sm text-emerald-100">{message}</p> : null}
      {isAdministrator ? (
        <form onSubmit={saveAstronaut} className="grid gap-3 rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-4 sm:grid-cols-2 xl:grid-cols-5">
          <label><span className="mb-1 block text-xs text-slate-400">Astronaut code</span><input required maxLength={40} value={form.astronautCode} onChange={(event) => setForm((current) => ({ ...current, astronautCode: event.target.value }))} className="min-h-11 w-full rounded-lg border border-white/10 bg-[#071522] px-3 text-sm text-slate-100" /></label>
          <label><span className="mb-1 block text-xs text-slate-400">Full name</span><input required maxLength={120} value={form.fullName} onChange={(event) => setForm((current) => ({ ...current, fullName: event.target.value }))} className="min-h-11 w-full rounded-lg border border-white/10 bg-[#071522] px-3 text-sm text-slate-100" /></label>
          <label><span className="mb-1 block text-xs text-slate-400">Email</span><input required type="email" maxLength={254} value={form.email} onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))} className="min-h-11 w-full rounded-lg border border-white/10 bg-[#071522] px-3 text-sm text-slate-100" /></label>
          <label><span className="mb-1 block text-xs text-slate-400">Status</span><select value={form.status} onChange={(event) => setForm((current) => ({ ...current, status: event.target.value }))} className="min-h-11 w-full rounded-lg border border-white/10 bg-[#071522] px-3 text-sm text-slate-100"><option value="ACTIVE">ACTIVE</option><option value="INACTIVE">INACTIVE</option></select></label>
          <div className="flex items-end gap-2"><button type="submit" disabled={saving} className="inline-flex min-h-11 flex-1 items-center justify-center gap-2 rounded-lg border border-cyan-200/15 bg-cyan-200/[0.07] px-3 text-xs font-medium text-cyan-100 disabled:opacity-50"><UserPlus size={14} />{saving ? 'Saving…' : editingId ? 'Save changes' : 'Create astronaut'}</button>{editingId ? <button type="button" onClick={resetForm} className="min-h-11 rounded-lg px-3 text-xs text-slate-400">Cancel</button> : null}</div>
        </form>
      ) : null}
      <label className="flex min-h-11 max-w-md items-center gap-2 rounded-xl border border-white/10 bg-[#0b1a29]/85 px-3">
        <Search size={16} className="shrink-0 text-slate-500" />
        <span className="sr-only">Search astronauts</span>
        <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search name or astronaut ID" className="min-w-0 flex-1 bg-transparent text-sm text-slate-100 outline-none placeholder:text-slate-600" />
      </label>
      <ApiState loading={loading} error={isAdministrator ? error : ''} empty={filteredAstronauts.length === 0 ? (isAdministrator ? 'No astronaut profiles match this search.' : 'No astronauts are assigned to your account.') : null}>
        {isAdministrator ? (
          <div className="overflow-x-auto rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85">
            <table className="w-full min-w-[760px] text-left text-xs">
              <thead className="bg-white/[0.025] text-[10px] uppercase tracking-[0.12em] text-slate-500"><tr><th className="px-4 py-3 font-medium">Code</th><th className="px-4 py-3 font-medium">Full name</th><th className="px-4 py-3 font-medium">Email</th><th className="px-4 py-3 font-medium">Status</th><th className="px-4 py-3 font-medium">Actions</th></tr></thead>
              <tbody>{filteredAstronauts.map((astronaut) => <tr key={astronaut.id} className="border-t border-white/[0.06] text-slate-300"><td className="px-4 py-3">{astronaut.astronautCode}</td><td className="px-4 py-3">{astronaut.fullName}</td><td className="px-4 py-3">{astronaut.email}</td><td className="px-4 py-3">{astronaut.status}</td><td className="px-4 py-3"><div className="flex gap-2"><button type="button" onClick={() => startEdit(astronaut)} className="text-cyan-100">Edit</button><button type="button" onClick={() => deleteAstronaut(astronaut)} className="text-rose-200">Delete</button></div></td></tr>)}</tbody>
            </table>
          </div>
        ) : (
          <div className="grid min-w-0 grid-cols-1 gap-3 md:grid-cols-2 2xl:grid-cols-4">
            {filteredAstronauts.map((astronaut) => <AstronautCard key={astronaut.id} astronaut={astronaut} />)}
          </div>
        )}
      </ApiState>
    </div>
  )
}

export default MyAstronauts
