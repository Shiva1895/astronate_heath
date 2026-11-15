import { useState } from 'react'
import DemoBanner from '../../components/dashboard/DemoBanner'
import PageHeader from '../../components/dashboard/PageHeader'

function Settings() {
  const [preferences, setPreferences] = useState({ notificationPreview: true, compactCards: false })

  function togglePreference(key) {
    setPreferences((current) => ({ ...current, [key]: !current[key] }))
  }

  return (
    <div className="space-y-6">
      <DemoBanner />
      <PageHeader
        eyebrow="Preferences · Local demo only"
        title="Settings"
        description="These controls demonstrate a settings layout. Changes are held in page memory only and are not saved or sent anywhere."
      />
      <section className="max-w-3xl divide-y divide-white/[0.07] rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 px-5">
        {[
          { id: 'notificationPreview', title: 'Notification preview', detail: 'Show illustrative notification indicators in the top navigation.' },
          { id: 'compactCards', title: 'Compact card preference', detail: 'Demo preference for a denser dashboard presentation.' },
        ].map((item) => (
          <div key={item.id} className="flex items-center justify-between gap-5 py-5">
            <div className="min-w-0">
              <h3 className="text-sm font-medium text-slate-100">{item.title}</h3>
              <p className="mt-1 text-xs leading-5 text-slate-500">{item.detail}</p>
            </div>
            <button
              type="button"
              onClick={() => togglePreference(item.id)}
              role="switch"
              aria-checked={preferences[item.id]}
              aria-label={item.title}
              className={`relative h-7 w-12 shrink-0 rounded-full border transition ${
                preferences[item.id] ? 'border-cyan-200/30 bg-cyan-300/20' : 'border-white/10 bg-white/[0.04]'
              }`}
            >
              <span className={`absolute top-1 h-[18px] w-[18px] rounded-full transition ${
                preferences[item.id] ? 'left-[25px] bg-cyan-100' : 'left-1 bg-slate-500'
              }`} />
            </button>
          </div>
        ))}
      </section>
    </div>
  )
}

export default Settings
