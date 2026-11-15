import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { api, getTokenPair, setTokenPair } from '../api/client'

const AuthContext = createContext(null)

function normalizeUser(user) {
  const roles = (user.roles || []).map((role) => role === 'ADMIN' ? 'ADMINISTRATOR' : role)
  return { ...user, roles }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  useEffect(() => {
    function clearSession() {
      setTokenPair(null)
      setUser(null)
    }
    window.addEventListener('astra:session-expired', clearSession)
    return () => window.removeEventListener('astra:session-expired', clearSession)
  }, [])

  const login = useCallback(async (credentials) => {
    const tokens = await api.login(credentials)
    setTokenPair(tokens)
    try {
      const currentUser = normalizeUser(await api.me())
      setUser(currentUser)
      return currentUser
    } catch (error) {
      setTokenPair(null)
      setUser(null)
      throw error
    }
  }, [])

  const register = useCallback((account) => api.register(account), [])

  const logout = useCallback(async () => {
    const refreshToken = getTokenPair()?.refreshToken
    let logoutError
    try {
      if (refreshToken) await api.logout(refreshToken)
    } catch (error) {
      logoutError = error
    } finally {
      setTokenPair(null)
      setUser(null)
    }
    if (logoutError) throw logoutError
  }, [])

  const value = useMemo(() => ({
    user,
    isAuthenticated: Boolean(user && getTokenPair()?.accessToken),
    login,
    register,
    logout,
  }), [user, login, register, logout])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within AuthProvider.')
  return context
}
