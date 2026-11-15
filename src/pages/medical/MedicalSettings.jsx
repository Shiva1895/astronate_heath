import { useState } from 'react'
import PageHeader from '../../components/dashboard/PageHeader'

function MedicalSettings() {
  const [preferences, setPreferences] = useState({ notificationPreview: true, denseTables: false })

  function toggle(key) {
    setPreferences((current) => ({ ...current, [key]: !current[key] }))
  }

  const options = [
    { id: 'notificationPreview', title: 'Notification preview', detail: 'Show unread indicators for illustrative notifications.' },
    { id: 'denseTables', title: 'Compact table preference', detail: 'Demo preference for denser alert and history tables.' },
  ]

  return (
    <div className="space-y-5">
      <PageHeader eyebrow="Preferences · Local demo only" title="Settings" description="These controls demonstrate a settings layout. Nothing is persisted or sent to a service." />
      <section className="max-w-3xl divide-y divide-white/[0.07] rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 px-5">
        {options.map((option) => (
          <div key={option.id} className="flex items-center justify-between gap-5 py-5">
            <div className="min-w-0">
              <h2 className="text-sm font-medium text-slate-100">{option.title}</h2>
              <p className="mt-1 text-xs leading-5 text-slate-500">{option.detail}</p>
            </div>
            <button type="button" onClick={() => toggle(option.id)} role="switch" aria-checked={preferences[option.id]} aria-label={option.title} className={`relative h-7 w-12 shrink-0 rounded-full border transition ${preferences[option.id] ? 'border-cyan-200/30 bg-cyan-300/20' : 'border-white/10 bg-white/[0.04]'}`}>
              <span className={`absolute top-1 h-[18px] w-[18px] rounded-full transition ${preferences[option.id] ? 'left-[25px] bg-cyan-100' : 'left-1 bg-slate-500'}`} />
            </button>
          </div>
        ))}
      </section>
    </div>
  )
}

export default MedicalSettings
