import { LoaderCircle } from 'lucide-react'

function AuthButton({ children, isLoading = false }) {
  return (
    <button
      type="submit"
      disabled={isLoading}
      className="inline-flex min-h-12 w-full items-center justify-center gap-2 rounded-full bg-gradient-to-r from-cyan-400 to-violet-500 px-5 py-3 text-sm font-semibold text-slate-950 shadow-[0_0_26px_rgba(22,200,232,0.18)] transition hover:brightness-110 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-cyan-200 disabled:cursor-wait disabled:opacity-70"
    >
      {isLoading ? <LoaderCircle size={17} className="animate-spin" aria-hidden="true" /> : null}
      {isLoading ? 'Please wait…' : children}
    </button>
  )
}

export default AuthButton
