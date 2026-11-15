import { useEffect, useState } from 'react'
import { ArrowRight, Menu, X } from 'lucide-react'
import { Link } from 'react-router-dom'

const navItems = [
  { label: 'Home', target: 'home' },
  { label: 'Platform', target: 'platform-preview' },
  { label: 'Features', target: 'features' },
  { label: 'How It Works', target: 'how-it-works' },
  { label: 'About', target: 'about' },
]

function Navbar() {
  const [isOpen, setIsOpen] = useState(false)

  useEffect(() => {
    if (!isOpen) return undefined

    function handleKeyDown(event) {
      if (event.key === 'Escape') setIsOpen(false)
    }

    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [isOpen])

  return (
    <header className="sticky top-0 z-50 border-b border-white/10 bg-slate-950/70 backdrop-blur-xl">
      <nav className="section-shell flex min-w-0 items-center justify-between gap-4 py-4" aria-label="Main navigation">
        <a href="#home" className="flex items-center gap-3" aria-label="ASTRA HEALTH home">
          <div className="orbit-mark">
            <span className="orbit-ring" />
          </div>
          <span className="text-sm font-semibold tracking-[0.28em] text-slate-100 sm:text-base">
            ASTRA HEALTH
          </span>
        </a>

        <div className="hidden items-center gap-6 xl:flex 2xl:gap-8">
          {navItems.map((item) => (
            <a
              key={item.label}
              href={`#${item.target}`}
              className="text-sm text-slate-300 transition hover:text-cyan-300"
            >
              {item.label}
            </a>
          ))}
        </div>

        <div className="hidden items-center gap-3 xl:flex">
          <Link
            to="/login"
            className="rounded-full px-3 py-2 text-sm font-medium text-slate-200 transition hover:text-cyan-200"
          >
            Sign In
          </Link>
          <a
            href="#platform-preview"
            className="rounded-full border border-white/10 bg-white/5 px-4 py-2 text-sm font-medium text-slate-100 transition hover:border-cyan-400/40 hover:bg-cyan-500/10"
          >
            View Demo
          </a>
          <a
            href="#platform-preview"
            className="inline-flex items-center gap-2 rounded-full bg-gradient-to-r from-cyan-400 to-violet-500 px-4 py-2 text-sm font-semibold text-slate-950 transition hover:scale-[1.02]"
          >
            Explore Platform
            <ArrowRight size={16} />
          </a>
        </div>

        <button
          type="button"
          aria-label={isOpen ? 'Close menu' : 'Open menu'}
          aria-expanded={isOpen}
          aria-controls="mobile-navigation"
          className="inline-flex h-11 w-11 items-center justify-center rounded-full border border-white/10 bg-white/5 text-slate-100 xl:hidden"
          onClick={() => setIsOpen((prev) => !prev)}
        >
          {isOpen ? <X size={20} /> : <Menu size={20} />}
        </button>
      </nav>

      <div
        id="mobile-navigation"
        className={`${isOpen ? 'block' : 'hidden'} border-t border-white/10 bg-slate-950/95 xl:hidden`}
      >
        <div className="section-shell flex flex-col gap-2 py-4">
          {navItems.map((item) => (
            <a
              key={item.label}
              href={`#${item.target}`}
              className="min-h-11 rounded-xl px-3 py-3 text-sm text-slate-300 transition hover:bg-white/5 hover:text-cyan-300"
              onClick={() => setIsOpen(false)}
            >
              {item.label}
            </a>
          ))}
          <Link
            to="/login"
            className="min-h-11 rounded-xl px-3 py-3 text-sm font-medium text-cyan-200 transition hover:bg-white/5"
            onClick={() => setIsOpen(false)}
          >
            Sign In
          </Link>
          <div className="mt-2 flex gap-3 pt-2">
            <a
              href="#platform-preview"
              className="flex-1 rounded-full border border-white/10 bg-white/5 px-4 py-3 text-sm font-medium"
              onClick={() => setIsOpen(false)}
            >
              View Demo
            </a>
            <a
              href="#platform-preview"
              className="flex flex-1 items-center justify-center gap-2 rounded-full bg-gradient-to-r from-cyan-400 to-violet-500 px-4 py-3 text-sm font-semibold text-slate-950"
              onClick={() => setIsOpen(false)}
            >
              Explore
            </a>
          </div>
        </div>
      </div>
    </header>
  )
}

export default Navbar
