import { ArrowUpRight, Rocket } from 'lucide-react'

function FinalCTA() {
  return (
    <section className="section-shell py-16 sm:py-20">
      <div className="relative overflow-hidden rounded-[30px] border border-cyan-400/20 bg-[radial-gradient(circle_at_top,_rgba(22,200,232,0.22),_transparent_35%),linear-gradient(135deg,_rgba(9,20,35,0.95),_rgba(14,25,43,0.9))] p-8 shadow-[0_30px_100px_rgba(13,22,34,0.9)] sm:p-10 lg:p-14">
        <div className="absolute -right-10 top-0 h-52 w-52 rounded-full bg-violet-500/20 blur-3xl" />
        <div className="absolute -left-8 bottom-0 h-48 w-48 rounded-full bg-cyan-500/20 blur-3xl" />

        <div className="relative mx-auto max-w-3xl text-center">
          <div className="eyebrow mx-auto justify-center">
            <Rocket size={12} />
            Future-ready health intelligence
          </div>
          <h2 className="mt-5 text-3xl font-bold tracking-[-0.05em] text-slate-50 sm:text-4xl lg:text-6xl">
            The Future of Space Exploration Starts with Human Health.
          </h2>
          <p className="mt-5 text-base leading-8 text-slate-300 sm:text-lg">
            Explore a smarter approach to astronaut health monitoring, mission awareness and intelligent medical support.
          </p>
          <div className="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
            <a
              href="#platform-preview"
              className="inline-flex items-center justify-center gap-2 rounded-full bg-gradient-to-r from-cyan-400 to-violet-500 px-5 py-3.5 text-sm font-semibold text-slate-950 shadow-[0_0_30px_rgba(34,211,238,0.2)]"
            >
              Explore the Platform
              <ArrowUpRight size={16} />
            </a>
            <a
              href="#home"
              className="inline-flex items-center justify-center gap-2 rounded-full border border-white/15 bg-white/5 px-5 py-3.5 text-sm font-semibold text-slate-100"
            >
              Back to Top
            </a>
          </div>
        </div>
      </div>
    </section>
  )
}

export default FinalCTA
