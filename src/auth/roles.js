export const APP_ROLES = Object.freeze({
  ASTRONAUT: 'ASTRONAUT',
  MEDICAL_OFFICER: 'MEDICAL_OFFICER',
  MISSION_CONTROL: 'MISSION_CONTROL',
  ADMINISTRATOR: 'ADMINISTRATOR',
})

export const ROLE_LABELS = Object.freeze({
  [APP_ROLES.ASTRONAUT]: 'Astronaut',
  [APP_ROLES.MEDICAL_OFFICER]: 'Medical Officer',
  [APP_ROLES.MISSION_CONTROL]: 'Mission Control',
  [APP_ROLES.ADMINISTRATOR]: 'Administrator',
})

export const PUBLIC_REGISTRATION_ROLE = APP_ROLES.ASTRONAUT

const publicNavigation = [
  { label: 'Home', to: '/' },
  { label: 'Sign In', to: '/login' },
  { label: 'Create Account', to: '/register' },
]

export const ROLE_NAVIGATION = Object.freeze({
  [APP_ROLES.ASTRONAUT]: [
    { label: 'Home', to: '/#home' },
    { label: 'Health Platform', to: '/#platform-preview' },
    { label: 'Features', to: '/#features' },
  ],
  [APP_ROLES.MEDICAL_OFFICER]: [
    { label: 'Home', to: '/#home' },
    { label: 'Health Overview', to: '/#platform-preview' },
    { label: 'Analysis Workflow', to: '/#how-it-works' },
  ],
  [APP_ROLES.MISSION_CONTROL]: [
    { label: 'Home', to: '/#home' },
    { label: 'Mission Overview', to: '/#about' },
    { label: 'Health Status', to: '/#platform-preview' },
  ],
  [APP_ROLES.ADMINISTRATOR]: [
    { label: 'Home', to: '/#home' },
    { label: 'Platform Overview', to: '/#platform-preview' },
    { label: 'System Features', to: '/#features' },
  ],
})

export function getRoleNavigation(verifiedRole) {
  return ROLE_NAVIGATION[verifiedRole] ?? publicNavigation
}
