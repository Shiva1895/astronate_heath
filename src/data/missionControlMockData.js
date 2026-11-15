import {
  Activity,
  Bell,
  CircleAlert,
  Clock3,
  FileText,
  HeartPulse,
  Radio,
  Settings2,
  ShieldAlert,
  UsersRound,
  UserRound,
  Wifi,
  Wrench,
} from 'lucide-react'
import {
  MEDICAL_ASTRONAUTS,
  MEDICAL_HEALTH_METRICS,
  MEDICAL_HEALTH_TRENDS,
} from './medicalMockData'

export const MISSION_CONTROL_BASE_PATH = '/mission-control/dashboard'

export const MISSION_CONTROL_NAVIGATION = [
  { label: 'Overview', path: MISSION_CONTROL_BASE_PATH, icon: Activity, end: true },
  { label: 'Device Monitoring', path: `${MISSION_CONTROL_BASE_PATH}/device-monitoring`, icon: Wrench },
  { label: 'Reports', path: `${MISSION_CONTROL_BASE_PATH}/reports`, icon: FileText },
  { label: 'Notifications', path: `${MISSION_CONTROL_BASE_PATH}/notifications`, icon: Bell },
]

export const MISSION_CONTROL_PAGE_TITLES = Object.fromEntries(
  MISSION_CONTROL_NAVIGATION.map(({ label, path }) => [path, label === 'Overview' ? 'Mission Control' : label]),
)

export const MISSION_CONTROL_SUMMARY = {
  name: 'ORBIT-01',
  missionDay: 24,
  status: 'Active',
  crewMembers: 4,
  medicalOfficers: 2,
  monitoringStatus: 'Operational',
  lastDataUpdate: '14 Nov 2026 · 09:42 UTC',
}

export const MISSION_CONTROL_ALERTS = [
  {
    id: 'MC-4102',
    astronautId: 'AST-001',
    title: 'Elevated heart-rate sample flagged for review',
    category: 'Health measurement',
    severity: 'Warning',
    timestamp: '2026-11-14T09:36:00Z',
    explanation: 'An illustrative sample crossed a configured demonstration threshold. Medical review is required; this is not a diagnosis.',
    status: 'New',
  },
  {
    id: 'MC-4101',
    astronautId: 'AST-002',
    title: 'Blood oxygen sample needs verification',
    category: 'Health measurement',
    severity: 'Critical',
    timestamp: '2026-11-14T09:20:00Z',
    explanation: 'A fictional sample is marked for prompt verification by authorized medical personnel.',
    status: 'Acknowledged',
  },
  {
    id: 'MC-4100',
    astronautId: 'AST-004',
    title: 'Device communication interruption',
    category: 'Device status',
    severity: 'Information',
    timestamp: '2026-11-14T08:42:00Z',
    explanation: 'A simulated device stopped reporting. No real device or spacecraft link is connected.',
    status: 'Under Review',
  },
  {
    id: 'MC-4099',
    astronautId: 'AST-003',
    title: 'Daily sample health review completed',
    category: 'Mission event',
    severity: 'Information',
    timestamp: '2026-11-14T08:10:00Z',
    explanation: 'Illustrative review event for the dashboard demonstration.',
    status: 'Resolved',
  },
]

export const MISSION_CONTROL_ASTRONAUTS = MEDICAL_ASTRONAUTS.map((astronaut, index) => {
  const metrics = MEDICAL_HEALTH_METRICS[astronaut.id]
  const alertCount = MISSION_CONTROL_ALERTS.filter(
    (alert) => alert.astronautId === astronaut.id && alert.status !== 'Resolved',
  ).length

  return {
    ...astronaut,
    mission: MISSION_CONTROL_SUMMARY.name,
    monitoringStatus: astronaut.monitoring,
    healthStatus: ['Normal', 'Normal', 'Standby', 'Review'][index],
    heartRate: Number(metrics.find((metric) => metric.id === 'heart-rate').value),
    spo2: Number(metrics.find((metric) => metric.id === 'oxygen').value),
    unresolvedAlerts: alertCount,
  }
})

export const MISSION_CONTROL_SUMMARIES = [
  {
    id: 'astronauts',
    label: 'Total Astronauts',
    value: String(MISSION_CONTROL_ASTRONAUTS.length),
    detail: 'Fictional mission roster',
    icon: UsersRound,
    tone: 'cyan',
  },
  {
    id: 'normal',
    label: 'Normal Health Status',
    value: String(MISSION_CONTROL_ASTRONAUTS.filter((astronaut) => astronaut.healthStatus === 'Normal').length),
    detail: 'Illustrative monitoring summaries',
    icon: HeartPulse,
    tone: 'mint',
  },
  {
    id: 'alerts',
    label: 'Alerts Requiring Review',
    value: String(MISSION_CONTROL_ALERTS.filter((alert) => alert.status === 'New').length),
    detail: 'Sample alerts awaiting acknowledgement',
    icon: CircleAlert,
    tone: 'amber',
  },
  {
    id: 'devices',
    label: 'Device Connection Issues',
    value: '1',
    detail: 'Simulated communication issue',
    icon: Wrench,
    tone: 'violet',
  },
]

export const MISSION_CONTROL_TREND_RANGES = [
  { id: '24h', label: 'Last 24 Hours' },
  { id: '7d', label: 'Last 7 Days' },
  { id: '30d', label: 'Last 30 Days' },
]

export const MISSION_CONTROL_TRENDS = Object.fromEntries(
  MISSION_CONTROL_TREND_RANGES.map(({ id }) => {
    const source = MEDICAL_HEALTH_TRENDS['AST-001'][id]
    const astronautTrends = Object.values(MEDICAL_HEALTH_TRENDS).map((range) => range[id])
    const aggregate = source.map((point, index) => {
      const average = (key) => astronautTrends.reduce((sum, trend) => sum + trend[index][key], 0) / astronautTrends.length
      return {
        time: point.time,
        heartRate: Math.round(average('heartRate')),
        spo2: Math.round(average('spo2') * 10) / 10,
        temperature: Math.round(average('temperature') * 10) / 10,
      }
    })
    return [id, aggregate]
  }),
)

export const MISSION_CONTROL_COMMUNICATIONS = [
  { id: 'crew', name: 'Astronaut communication channel', status: 'Connected', detail: 'Fictional status · Updated 09:40 UTC', icon: UsersRound },
  { id: 'medical', name: 'Medical team communication', status: 'Connected', detail: 'Fictional status · Updated 09:38 UTC', icon: HeartPulse },
  { id: 'control', name: 'Mission Control coordination', status: 'Connected', detail: 'Fictional status · Updated 09:42 UTC', icon: Radio },
  { id: 'sync', name: 'Sample data synchronization', status: 'Delayed', detail: 'Fictional status · Last successful update 09:12 UTC', icon: Activity },
]

export const MISSION_CONTROL_DEVICES = [
  { id: 'DEV-HR-001', name: 'Heart-rate sensor', astronautId: 'AST-001', status: 'Connected', lastMeasurement: '72 BPM · sample', updated: '09:42 UTC', battery: 86 },
  { id: 'DEV-OX-002', name: 'Pulse oximeter', astronautId: 'AST-002', status: 'Connected', lastMeasurement: '97% · sample', updated: '09:38 UTC', battery: 74 },
  { id: 'DEV-TM-003', name: 'Temperature sensor', astronautId: 'AST-004', status: 'Disconnected', lastMeasurement: '37.1 °C · sample', updated: '08:42 UTC', battery: 21 },
  { id: 'DEV-ACT-004', name: 'Activity tracker', astronautId: 'AST-003', status: 'Synchronizing', lastMeasurement: 'Activity sample available', updated: '09:12 UTC', battery: 63 },
]

export const MISSION_CONTROL_TIMELINE = [
  { id: 'EV-01', title: 'Mission monitoring started', time: '14 Nov 2026 · 06:00 UTC', category: 'Mission', description: 'Illustrative monitoring session began for the ORBIT-01 demo.' },
  { id: 'EV-02', title: 'Daily astronaut health review completed', time: '14 Nov 2026 · 08:10 UTC', category: 'Medical review', description: 'Fictional sample review event recorded for interface demonstration.' },
  { id: 'EV-03', title: 'Device communication interruption detected', time: '14 Nov 2026 · 08:42 UTC', category: 'Device', description: 'A simulated activity-device update is delayed.' },
  { id: 'EV-04', title: 'Medical officer acknowledged a sample alert', time: '14 Nov 2026 · 09:20 UTC', category: 'Alert', description: 'A fictional alert was acknowledged in the demo workflow.' },
  { id: 'EV-05', title: 'Sample data synchronization completed', time: '14 Nov 2026 · 09:42 UTC', category: 'Synchronization', description: 'The latest illustrative records were made available in this prototype.' },
]

export const MISSION_CONTROL_NOTIFICATIONS = [
  { id: 'MN-001', type: 'Health alert', title: 'New health alert requires review', detail: 'A sample heart-rate reading for AST-001 was flagged for review.', time: '09:36 UTC · Today', read: false },
  { id: 'MN-002', type: 'Medical team', title: 'Medical officer update', detail: 'A fictional review status changed for a sample astronaut alert.', time: '09:20 UTC · Today', read: false },
  { id: 'MN-003', type: 'Device', title: 'Device communication interruption', detail: 'The simulated temperature sensor for AST-004 is disconnected.', time: '08:42 UTC · Today', read: true },
  { id: 'MN-004', type: 'Measurement', title: 'Missing measurement notification', detail: 'A sample check-in has no associated measurement in this scenario.', time: '08:10 UTC · Today', read: true },
  { id: 'MN-005', type: 'Mission', title: 'Mission status update', detail: 'The ORBIT-01 demonstration mission is marked active.', time: '06:00 UTC · Today', read: true },
]
