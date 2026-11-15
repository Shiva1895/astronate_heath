import { Activity, ArrowLeft, ShieldCheck, Sparkles } from 'lucide-react'
import { Link } from 'react-router-dom'
import { ROLE_LABELS } from '../../auth/roles'
import RoleAwareNavigation from './RoleAwareNavigation'

function AuthLayout({ eyebrow, title, description, children }) {
  return (
    <main className="min-h-screen px-4 py-5 sm:px-6 sm:py-8 lg:px-8">
      <div className="mx-auto max-w-7xl">
        <header className="flex items-center justify-between gap-4">
          <Link to="/" className="flex min-w-0 items-center gap-3" aria-label="ASTRA HEALTH home">
            <div className="orbit-mark small shrink-0">
              <span className="orbit-ring" />
            </div>
            <span className="truncate text-xs font-semibold tracking-[0.24em] text-slate-100 sm:text-sm">
              ASTRA HEALTH
            </span>
          </Link>
          <RoleAwareNavigation />
        </header>

        <div className="grid items-center gap-10 py-10 sm:py-14 lg:min-h-[calc(100svh-10rem)] lg:grid-cols-[minmax(0,1fr)_minmax(390px,0.82fr)] lg:gap-16 lg:py-12">
          <section className="mx-auto w-full max-w-xl">
            <div className="eyebrow mb-5">
              <Sparkles size={12} />
              Secure mission access
            </div>
            <h1 className="max-w-xl text-4xl font-black leading-[1.05] tracking-[-0.06em] text-slate-50 sm:text-5xl lg:text-6xl">
              Health systems built for{' '}
              <span className="bg-gradient-to-r from-cyan-300 via-sky-400 to-violet-400 bg-clip-text text-transparent">
                life beyond Earth.
              </span>
            </h1>
            <p className="mt-5 max-w-lg text-base leading-7 text-slate-300 sm:text-lg">
              {description}
            </p>

            <div className="mt-8 grid gap-3 sm:grid-cols-2">
              <div className="rounded-2xl border border-white/10 bg-white/[0.04] p-4">
                <Activity className="mb-3 text-cyan-200" size={19} />
                <p className="text-sm font-semibold text-slate-100">Mission-aware care</p>
                <p className="mt-1 text-xs leading-5 text-slate-400">
                  Designed to support health awareness across demanding missions.
                </p>
              </div>
              <div className="rounded-2xl border border-white/10 bg-white/[0.04] p-4">
                <ShieldCheck className="mb-3 text-violet-200" size={19} />
                <p className="text-sm font-semibold text-slate-100">Role-conscious access</p>
                <p className="mt-1 text-xs leading-5 text-slate-400">
                  Roles must be assigned and verified by the connected backend.
                </p>
              </div>
            </div>

            <div className="mt-7 flex flex-wrap gap-2" aria-label="Supported application roles">
              {Object.values(ROLE_LABELS).map((role) => (
                <span
                  key={role}
                  className="rounded-full border border-white/10 bg-slate-900/60 px-3 py-1.5 text-xs text-slate-300"
                >
                  {role}
                </span>
              ))}
            </div>
          </section>

          <section className="mx-auto w-full max-w-lg">
            <div className="glass-panel relative p-5 sm:p-8">
              <div className="mb-7">
                <Link
                  to="/"
                  className="mb-5 inline-flex min-h-11 items-center gap-2 text-xs font-medium text-slate-400 transition hover:text-cyan-200"
                >
                  <ArrowLeft size={14} />
                  Back to ASTRA HEALTH
                </Link>
                <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-200">{eyebrow}</p>
                <h2 className="mt-2 text-2xl font-bold tracking-[-0.04em] text-slate-50 sm:text-3xl">{title}</h2>
                <p className="mt-2 text-sm leading-6 text-slate-400">
                  Frontend prototype only. No account or authenticated session is created.
                </p>
              </div>
              {children}
            </div>
            <p className="mt-5 px-2 text-center text-xs leading-5 text-slate-500">
              ASTRA HEALTH is an educational prototype, not a certified medical device or an authentication service.
            </p>
          </section>
        </div>
      </div>
    </main>
  )
}

export default AuthLayout
