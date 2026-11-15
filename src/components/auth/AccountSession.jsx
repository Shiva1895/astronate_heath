import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../auth/AuthProvider'

function AccountSession() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  async function handleLogout() {
    setIsLoading(true)
    setError('')
    try {
      await logout()
      navigate('/login', { replace: true })
    } catch (logoutError) {
      setError(logoutError.message || 'The server could not confirm sign-out. Local credentials were cleared.')
      navigate('/login', { replace: true })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="flex min-h-11 items-center justify-between gap-3 border-b border-white/[0.06] bg-[#0b1a29]/60 px-4 py-2 text-xs sm:px-6 xl:px-8">
      <div className="min-w-0">
        <p className="truncate font-medium text-slate-200">{user?.displayName || user?.username || user?.email}</p>
        <p className="truncate text-[10px] text-slate-500">{user?.roles?.join(', ')}</p>
        {error ? <p role="status" className="text-[10px] text-amber-200">{error}</p> : null}
      </div>
      <button
        type="button"
        onClick={handleLogout}
        disabled={isLoading}
        className="min-h-9 shrink-0 rounded-lg border border-white/10 px-3 text-xs font-medium text-slate-300 hover:bg-white/[0.06] disabled:opacity-60"
      >
        {isLoading ? 'Signing out…' : 'Sign out'}
      </button>
    </div>
  )
}

export default AccountSession
