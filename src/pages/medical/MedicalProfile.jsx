import { ShieldCheck, Stethoscope } from 'lucide-react'
import { useAuth } from '../../auth/AuthProvider'
import PageHeader from '../../components/dashboard/PageHeader'

function MedicalProfile() {
  const { user } = useAuth()
  const displayName = user.displayName || user.username || user.email
  return (
    <div className="space-y-5">
      <PageHeader
        eyebrow="Authenticated account"
        title="My Profile"
        description="Account information returned by the authenticated backend session."
      />
      <section className="max-w-3xl rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-5 sm:p-7">
        <div className="flex flex-col gap-5 border-b border-white/[0.07] pb-6 sm:flex-row sm:items-center">
          <div className="flex h-16 w-16 items-center justify-center rounded-2xl border border-violet-200/15 bg-violet-200/[0.07] text-violet-100">
            <Stethoscope size={30} />
          </div>
          <div>
            <h2 className="text-xl font-semibold text-slate-100">{displayName}</h2>
            <p className="mt-1 text-sm text-slate-400">{user.email}</p>
            <span className="mt-3 inline-flex items-center gap-1.5 rounded-full border border-amber-200/10 bg-amber-200/[0.04] px-2.5 py-1 text-[11px] text-amber-100">
              <ShieldCheck size={13} /> Role checks enforced by the backend
            </span>
          </div>
        </div>
        <dl className="grid grid-cols-1 gap-x-6 sm:grid-cols-2">
          {[['Display name', displayName], ['Account ID', user.id], ['Email', user.email], ['Roles', user.roles.join(', ')], ['Linked astronaut profile', user.astronautProfileId || 'Not linked']].map(([label, value]) => (
            <div key={label} className="border-b border-white/[0.06] py-4">
              <dt className="text-xs text-slate-500">{label}</dt>
              <dd className="mt-1 text-sm font-medium text-slate-200">{value}</dd>
            </div>
          ))}
        </dl>
        <p className="mt-5 text-xs leading-5 text-slate-500">The backend enforces role permissions and per-astronaut assignment checks for medical records.</p>
      </section>
    </div>
  )
}

export default MedicalProfile
