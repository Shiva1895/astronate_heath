function Footer() {
  return (
    <footer className="border-t border-white/10 bg-slate-950/80">
      <div className="section-shell grid gap-8 py-12 min-[600px]:grid-cols-2 xl:grid-cols-[1.2fr_0.8fr_0.8fr_0.8fr_1fr] lg:gap-10">
        <div>
          <div className="flex items-center gap-3">
            <div className="orbit-mark small">
              <span className="orbit-ring" />
            </div>
            <span className="text-sm font-semibold tracking-[0.26em] text-slate-50">ASTRA HEALTH</span>
          </div>
          <p className="mt-5 max-w-sm text-sm leading-7 text-slate-300">
            A future-facing health intelligence platform for space missions, designed to monitor vital signals and support confident decisions beyond Earth.
          </p>
        </div>

        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">Navigation</p>
          <ul className="mt-4 space-y-3 text-sm text-slate-300">
            <li><a href="#home" className="transition hover:text-cyan-300">Home</a></li>
            <li><a href="#platform-preview" className="transition hover:text-cyan-300">Platform</a></li>
            <li><a href="#features" className="transition hover:text-cyan-300">Features</a></li>
            <li><a href="#how-it-works" className="transition hover:text-cyan-300">How It Works</a></li>
          </ul>
        </div>

        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">Features</p>
          <ul className="mt-4 space-y-3 text-sm text-slate-300">
            <li><a href="#features" className="transition hover:text-cyan-300">Monitoring</a></li>
            <li><a href="#features" className="transition hover:text-cyan-300">Alerts</a></li>
            <li><a href="#features" className="transition hover:text-cyan-300">Analytics</a></li>
            <li><a href="#platform-preview" className="transition hover:text-cyan-300">Dashboard</a></li>
          </ul>
        </div>

        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">About</p>
          <ul className="mt-4 space-y-3 text-sm text-slate-300">
            <li><a href="#about" className="transition hover:text-cyan-300">Mission</a></li>
            <li><a href="#about" className="transition hover:text-cyan-300">Principles</a></li>
            <li><a href="#features" className="transition hover:text-cyan-300">Intelligence</a></li>
            <li><a href="#home" className="transition hover:text-cyan-300">Overview</a></li>
          </ul>
        </div>

        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">Technology</p>
          <ul className="mt-4 space-y-3 text-sm text-slate-300">
            <li>React + Vite</li>
            <li>Tailwind CSS</li>
            <li>Framer Motion</li>
            <li>Lucide Icons</li>
          </ul>
        </div>
      </div>

      <div className="border-t border-white/10">
        <div className="section-shell flex flex-col gap-3 py-5 text-sm text-slate-400 sm:flex-row sm:items-center sm:justify-between">
          <p>© 2026 ASTRA HEALTH. Educational prototype for design exploration.</p>
          <p className="max-w-xl text-xs leading-6 text-slate-400">
            ASTRA HEALTH is an educational software prototype. It is not a certified medical device and does not replace qualified medical professionals.
          </p>
        </div>
      </div>
    </footer>
  )
}

export default Footer
