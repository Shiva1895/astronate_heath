import { Link } from 'react-router-dom'
import { getRoleNavigation } from '../../auth/roles'

function RoleAwareNavigation({ verifiedRole }) {
  const items = getRoleNavigation(verifiedRole)

  return (
    <nav className="flex flex-wrap items-center justify-end gap-x-4 gap-y-2" aria-label="Account navigation">
      {items.map((item, index) => (
        <Link
          key={item.label}
          to={item.to}
          className={`inline-flex min-h-11 items-center rounded-full px-2 text-sm text-slate-300 transition hover:text-cyan-200 focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-cyan-300 ${
            index > 0 ? 'hidden sm:inline-flex' : ''
          }`}
        >
          {item.label}
        </Link>
      ))}
    </nav>
  )
}

export default RoleAwareNavigation
