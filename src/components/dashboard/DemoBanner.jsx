import { FlaskConical } from 'lucide-react'

function DemoBanner() {
  return (
    <div className="flex items-start gap-3 rounded-xl border border-cyan-200/15 bg-cyan-200/[0.045] px-4 py-3 text-xs leading-5 text-cyan-50/90" role="note">
      <FlaskConical size={16} className="mt-0.5 shrink-0 text-cyan-200" />
      <p>
        <span className="font-semibold text-cyan-100">DEMO DATA</span>
        <span className="text-cyan-100/70"> · </span>
        All profiles, readings, alerts, trends, and device states are simulated. No live sensors or backend are connected.
      </p>
    </div>
  )
}

export default DemoBanner
