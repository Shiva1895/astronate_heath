import Navbar from '../components/Navbar'
import Hero from '../components/Hero'
import MissionSection from '../components/MissionSection'
import FeatureSection from '../components/FeatureSection'
import HowItWorks from '../components/HowItWorks'
import DashboardPreview from '../components/DashboardPreview'
import WhyAstra from '../components/WhyAstra'
import FinalCTA from '../components/FinalCTA'
import Footer from '../components/Footer'

function LandingPage() {
  return (
    <>
      <Navbar />
      <main>
        <Hero />
        <MissionSection />
        <FeatureSection />
        <HowItWorks />
        <DashboardPreview />
        <WhyAstra />
        <FinalCTA />
      </main>
      <Footer />
    </>
  )
}

export default LandingPage
