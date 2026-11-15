import AppRoutes from './routes/AppRoutes'

function App() {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-50">
      <div className="pointer-events-none fixed inset-0 -z-10 overflow-hidden">
        <div className="starfield" />
        <div className="planet-halo" />
      </div>

      <div className="relative z-10">
        <AppRoutes />
      </div>
    </div>
  )
}

export default App
