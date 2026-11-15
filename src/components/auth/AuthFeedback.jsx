function AuthFeedback({ tone = 'info', children }) {
  const toneClass = tone === 'error'
    ? 'border-rose-300/25 bg-rose-400/10 text-rose-100'
    : tone === 'success'
      ? 'border-emerald-300/25 bg-emerald-400/10 text-emerald-100'
      : 'border-cyan-300/25 bg-cyan-400/10 text-cyan-100'

  return (
    <p
      className={`rounded-xl border px-4 py-3 text-sm leading-6 ${toneClass}`}
      role={tone === 'error' ? 'alert' : 'status'}
      aria-live={tone === 'error' ? 'assertive' : 'polite'}
    >
      {children}
    </p>
  )
}

export default AuthFeedback
