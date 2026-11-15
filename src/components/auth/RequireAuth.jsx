import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../../auth/AuthProvider'

function RequireAuth({ roles, children }) {
  const { isAuthenticated, user } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) return <Navigate to="/login" replace state={{ from: location }} />
  if (roles && !user.roles.some((role) => roles.includes(role))) {
    return <Navigate to={user.roles.includes('ASTRONAUT') ? '/astronaut/dashboard' : '/'} replace />
  }
  return children
}

export default RequireAuth
