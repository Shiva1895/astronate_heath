function ApiState({ loading, error, empty, children }) {
  if (loading) {
    return <p className="rounded-xl border border-white/[0.08] bg-white/[0.02] p-5 text-sm text-slate-400" role="status">Loading authorized data…</p>
  }
  if (error) {
    return <p className="rounded-xl border border-rose-300/15 bg-rose-300/[0.05] p-5 text-sm text-rose-100" role="alert">{error}</p>
  }
  if (empty) {
    return <p className="rounded-xl border border-white/[0.08] bg-white/[0.02] p-5 text-sm text-slate-400">{empty}</p>
  }
  return children
}

export default ApiState
