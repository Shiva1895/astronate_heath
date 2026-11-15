import { CircleUserRound, ShieldCheck } from 'lucide-react'
import { useOutletContext } from 'react-router-dom'
import PageHeader from '../../components/dashboard/PageHeader'

function Profile() {
  const { user, astronautId } = useOutletContext()
  const displayName = user.displayName || user.username || user.email
  const details = [
    ['Name', displayName],
    ['Email', user.email],
    ['Account ID', user.id],
    ['Astronaut profile ID', astronautId],
    ['Roles', user.roles.join(', ')],
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Authenticated account"
        title="My profile"
        description="Account information returned by the authenticated backend session."
      />
      <section className="max-w-3xl rounded-2xl border border-white/[0.08] bg-[#0b1a29]/85 p-5 sm:p-7">
        <div className="flex flex-col gap-5 border-b border-white/[0.07] pb-6 sm:flex-row sm:items-center">
          <div className="flex h-16 w-16 items-center justify-center rounded-2xl border border-cyan-200/15 bg-cyan-200/[0.07] text-cyan-100">
            <CircleUserRound size={32} />
          </div>
          <div>
            <h3 className="text-xl font-semibold text-slate-100">{displayName}</h3>
            <p className="mt-1 text-sm text-slate-400">{user.email}</p>
            <span className="mt-3 inline-flex items-center gap-1.5 rounded-full border border-cyan-200/10 bg-cyan-200/[0.05] px-2.5 py-1 text-[11px] text-cyan-100">
              <ShieldCheck size={13} /> Verified by the authenticated session
            </span>
          </div>
        </div>
        <dl className="grid grid-cols-1 gap-x-6 sm:grid-cols-2">
          {details.map(([label, value]) => (
            <div key={label} className="border-b border-white/[0.06] py-4">
              <dt className="text-xs text-slate-500">{label}</dt>
              <dd className="mt-1 text-sm font-medium text-slate-200">{value}</dd>
            </div>
          ))}
        </dl>
        <p className="mt-5 text-xs leading-5 text-slate-500">
          Mission assignments and profile details are only shown when provided by authorized backend APIs.
        </p>
      </section>
    </div>
  )
}

export default Profile
