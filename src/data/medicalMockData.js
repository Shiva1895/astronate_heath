import {
  Activity,
  Bell,
  ChartNoAxesCombined,
  CircleCheck,
  CircleUserRound,
  ClipboardList,
  FileText,
  HeartPulse,
  Radio,
  Settings2,
  ShieldAlert,
  Thermometer,
  UsersRound,
  Wind,
} from 'lucide-react'

export const MEDICAL_ASTRONAUTS = [
  {
    id: 'AST-001',
    name: 'Alex Morgan',
    mission: 'ORBIT-01',
    monitoring: 'Active',
    latestUpdate: '09:42 UTC',
    alertSummary: '1 new alert',
    alertTone: 'warning',
    avatar: 'AM',
    age: '34 · sample',
    role: 'Flight specialist',
    missionDay: 24,
    deviceStatus: '3 connected · simulated',
  },
  {
    id: 'AST-002',
    name: 'Sarah Chen',
    mission: 'ORBIT-01',
    monitoring: 'Active',
    latestUpdate: '09:38 UTC',
    alertSummary: '1 acknowledged alert',
    alertTone: 'info',
    avatar: 'SC',
    age: '31 · sample',
    role: 'Mission specialist',
    missionDay: 24,
    deviceStatus: '4 connected · simulated',
  },
  {
    id: 'AST-003',
    name: 'Daniel Reed',
    mission: 'LUNAR-02',
    monitoring: 'Standby',
    latestUpdate: '09:12 UTC',
    alertSummary: 'No new alerts',
    alertTone: 'neutral',
    avatar: 'DR',
    age: '38 · sample',
    role: 'Pilot',
    missionDay: 11,
    deviceStatus: '2 connected · simulated',
  },
  {
    id: 'AST-004',
    name: 'Maya Patel',
    mission: 'ORBIT-02',
    monitoring: 'Active',
    latestUpdate: '08:55 UTC',
    alertSummary: '1 review item',
    alertTone: 'critical',
    avatar: 'MP',
    age: '29 · sample',
    role: 'Flight engineer',
    missionDay: 7,
    deviceStatus: '1 disconnected · simulated',
  },
]

export const MEDICAL_MISSIONS = [
  { value: 'all', label: 'All sample missions' },
  { value: 'ORBIT-01', label: 'ORBIT-01' },
  { value: 'LUNAR-02', label: 'LUNAR-02' },
  { value: 'ORBIT-02', label: 'ORBIT-02' },
]

export const MEDICAL_NAVIGATION = [
  { label: 'Overview', to: '/medical/dashboard', icon: Activity, end: true },
  { label: 'My Astronauts', to: '/medical/dashboard/astronauts', icon: UsersRound },
  { label: 'Health Monitoring', to: '/medical/dashboard/health', icon: HeartPulse },
  { label: 'Alert Management', to: '/medical/dashboard/alerts', icon: ShieldAlert },
  { label: 'Health History', to: '/medical/dashboard/history', icon: ChartNoAxesCombined },
  { label: 'Medical Observations', to: '/medical/dashboard/observations', icon: ClipboardList },
  { label: 'Reports', to: '/medical/dashboard/reports', icon: FileText },
  { label: 'Notifications', to: '/medical/dashboard/notifications', icon: Bell },
  { label: 'My Profile', to: '/medical/dashboard/profile', icon: CircleUserRound },
  { label: 'Settings', to: '/medical/dashboard/settings', icon: Settings2 },
  { label: 'Audit History', to: '/medical/dashboard/audit', icon: ClipboardList, roles: ['ADMINISTRATOR'] },
]

export const MEDICAL_PAGE_TITLES = {
  '/medical/dashboard': 'Overview',
  '/medical/dashboard/astronauts': 'My Astronauts',
  '/medical/dashboard/health': 'Health Monitoring',
  '/medical/dashboard/alerts': 'Alert Management',
  '/medical/dashboard/history': 'Health History',
  '/medical/dashboard/observations': 'Medical Observations',
  '/medical/dashboard/reports': 'Reports',
  '/medical/dashboard/notifications': 'Notifications',
  '/medical/dashboard/profile': 'My Profile',
  '/medical/dashboard/settings': 'Settings',
  '/medical/dashboard/audit': 'Audit History',
}

export const MEDICAL_HEALTH_METRICS = {
  'AST-001': [
    { id: 'heart-rate', title: 'Heart Rate', value: '72', unit: 'BPM', trend: 'Within sample range', direction: 'stable', updated: '09:42 UTC', icon: HeartPulse, tone: 'cyan' },
    { id: 'oxygen', title: 'Blood Oxygen (SpO₂)', value: '98', unit: '%', trend: 'Recent sample available', direction: 'stable', updated: '09:41 UTC', icon: Activity, tone: 'violet' },
    { id: 'temperature', title: 'Body Temperature', value: '36.8', unit: '°C', trend: 'Recent sample available', direction: 'stable', updated: '09:40 UTC', icon: Thermometer, tone: 'blue' },
    { id: 'respiratory-rate', title: 'Respiratory Rate', value: '16', unit: 'breaths/min', trend: 'Recent sample available', direction: 'stable', updated: '09:39 UTC', icon: Wind, tone: 'mint' },
  ],
  'AST-002': [
    { id: 'heart-rate', title: 'Heart Rate', value: '68', unit: 'BPM', trend: 'Recent sample available', direction: 'stable', updated: '09:38 UTC', icon: HeartPulse, tone: 'cyan' },
    { id: 'oxygen', title: 'Blood Oxygen (SpO₂)', value: '97', unit: '%', trend: 'Review sample context', direction: 'down', updated: '09:37 UTC', icon: Activity, tone: 'violet' },
    { id: 'temperature', title: 'Body Temperature', value: '36.7', unit: '°C', trend: 'Recent sample available', direction: 'stable', updated: '09:36 UTC', icon: Thermometer, tone: 'blue' },
    { id: 'respiratory-rate', title: 'Respiratory Rate', value: '15', unit: 'breaths/min', trend: 'Recent sample available', direction: 'stable', updated: '09:35 UTC', icon: Wind, tone: 'mint' },
  ],
  'AST-003': [
    { id: 'heart-rate', title: 'Heart Rate', value: '74', unit: 'BPM', trend: 'Recent sample available', direction: 'stable', updated: '09:12 UTC', icon: HeartPulse, tone: 'cyan' },
    { id: 'oxygen', title: 'Blood Oxygen (SpO₂)', value: '99', unit: '%', trend: 'Recent sample available', direction: 'stable', updated: '09:11 UTC', icon: Activity, tone: 'violet' },
    { id: 'temperature', title: 'Body Temperature', value: '36.6', unit: '°C', trend: 'Recent sample available', direction: 'stable', updated: '09:10 UTC', icon: Thermometer, tone: 'blue' },
    { id: 'respiratory-rate', title: 'Respiratory Rate', value: '14', unit: 'breaths/min', trend: 'Recent sample available', direction: 'stable', updated: '09:09 UTC', icon: Wind, tone: 'mint' },
  ],
  'AST-004': [
    { id: 'heart-rate', title: 'Heart Rate', value: '76', unit: 'BPM', trend: 'Review sample context', direction: 'up', updated: '08:55 UTC', icon: HeartPulse, tone: 'cyan' },
    { id: 'oxygen', title: 'Blood Oxygen (SpO₂)', value: '98', unit: '%', trend: 'Recent sample available', direction: 'stable', updated: '08:54 UTC', icon: Activity, tone: 'violet' },
    { id: 'temperature', title: 'Body Temperature', value: '37.1', unit: '°C', trend: 'Review sample context', direction: 'up', updated: '08:53 UTC', icon: Thermometer, tone: 'blue' },
    { id: 'respiratory-rate', title: 'Respiratory Rate', value: '17', unit: 'breaths/min', trend: 'Recent sample available', direction: 'stable', updated: '08:52 UTC', icon: Wind, tone: 'mint' },
  ],
}

export const MEDICAL_TREND_RANGES = [
  { id: '24h', label: '24 hours' },
  { id: '7d', label: '7 days' },
  { id: '30d', label: '30 days' },
]

const sampleTrend24h = [
  { time: '00:00', heartRate: 66, spo2: 98, temperature: 36.6 },
  { time: '03:00', heartRate: 64, spo2: 97, temperature: 36.5 },
  { time: '06:00', heartRate: 68, spo2: 98, temperature: 36.6 },
  { time: '09:00', heartRate: 72, spo2: 98, temperature: 36.8 },
  { time: '12:00', heartRate: 75, spo2: 99, temperature: 36.8 },
  { time: '15:00', heartRate: 71, spo2: 98, temperature: 36.7 },
  { time: '18:00', heartRate: 69, spo2: 98, temperature: 36.7 },
  { time: '21:00', heartRate: 67, spo2: 97, temperature: 36.6 },
]

const sampleTrend7d = [
  { time: 'Mon', heartRate: 68, spo2: 98, temperature: 36.6 },
  { time: 'Tue', heartRate: 70, spo2: 97, temperature: 36.7 },
  { time: 'Wed', heartRate: 67, spo2: 98, temperature: 36.6 },
  { time: 'Thu', heartRate: 73, spo2: 99, temperature: 36.8 },
  { time: 'Fri', heartRate: 71, spo2: 98, temperature: 36.7 },
  { time: 'Sat', heartRate: 69, spo2: 98, temperature: 36.7 },
  { time: 'Sun', heartRate: 72, spo2: 98, temperature: 36.8 },
]

const sampleTrend30d = [
  { time: 'Oct 16', heartRate: 69, spo2: 98, temperature: 36.6 },
  { time: 'Oct 18', heartRate: 71, spo2: 98, temperature: 36.7 },
  { time: 'Oct 20', heartRate: 68, spo2: 97, temperature: 36.6 },
  { time: 'Oct 22', heartRate: 70, spo2: 98, temperature: 36.7 },
  { time: 'Oct 24', heartRate: 67, spo2: 99, temperature: 36.5 },
  { time: 'Oct 26', heartRate: 72, spo2: 98, temperature: 36.8 },
  { time: 'Oct 28', heartRate: 70, spo2: 97, temperature: 36.7 },
  { time: 'Oct 30', heartRate: 68, spo2: 98, temperature: 36.6 },
  { time: 'Nov 01', heartRate: 73, spo2: 98, temperature: 36.8 },
  { time: 'Nov 03', heartRate: 71, spo2: 99, temperature: 36.7 },
  { time: 'Nov 05', heartRate: 69, spo2: 98, temperature: 36.6 },
  { time: 'Nov 07', heartRate: 74, spo2: 97, temperature: 36.8 },
  { time: 'Nov 09', heartRate: 70, spo2: 98, temperature: 36.7 },
  { time: 'Nov 11', heartRate: 68, spo2: 98, temperature: 36.6 },
  { time: 'Nov 14', heartRate: 72, spo2: 98, temperature: 36.8 },
]

export const MEDICAL_HEALTH_TRENDS = {
  'AST-001': { '24h': sampleTrend24h, '7d': sampleTrend7d, '30d': sampleTrend30d },
  'AST-002': { '24h': sampleTrend24h.map((item) => ({ ...item, heartRate: item.heartRate - 2, spo2: item.spo2 - 1 })), '7d': sampleTrend7d.map((item) => ({ ...item, heartRate: item.heartRate - 2, spo2: item.spo2 - 1 })), '30d': sampleTrend30d.map((item) => ({ ...item, heartRate: item.heartRate - 2, spo2: item.spo2 - 1 })) },
  'AST-003': { '24h': sampleTrend24h.map((item) => ({ ...item, heartRate: item.heartRate + 2, temperature: item.temperature - 0.1 })), '7d': sampleTrend7d.map((item) => ({ ...item, heartRate: item.heartRate + 2, temperature: item.temperature - 0.1 })), '30d': sampleTrend30d.map((item) => ({ ...item, heartRate: item.heartRate + 2, temperature: item.temperature - 0.1 })) },
  'AST-004': { '24h': sampleTrend24h.map((item) => ({ ...item, heartRate: item.heartRate + 4, temperature: item.temperature + 0.3 })), '7d': sampleTrend7d.map((item) => ({ ...item, heartRate: item.heartRate + 4, temperature: item.temperature + 0.3 })), '30d': sampleTrend30d.map((item) => ({ ...item, heartRate: item.heartRate + 4, temperature: item.temperature + 0.3 })) },
}

export const MEDICAL_ALERTS = [
  {
    id: 'AL-2041',
    astronautId: 'AST-001',
    title: 'Elevated heart-rate reading',
    timestamp: '2026-11-14T09:36:00Z',
    severity: 'Warning',
    explanation: 'An illustrative sample crossed a configured review threshold. This is a measurement flag, not a medical diagnosis.',
    status: 'New',
  },
  {
    id: 'AL-2040',
    astronautId: 'AST-002',
    title: 'Oxygen saturation sample requires review',
    timestamp: '2026-11-14T09:20:00Z',
    severity: 'Critical',
    explanation: 'A sample value is marked for prompt clinician review in this scenario. Confirm context using an authorized, validated system.',
    status: 'Acknowledged',
  },
  {
    id: 'AL-2039',
    astronautId: 'AST-004',
    title: 'Temperature measurement outside configured limits',
    timestamp: '2026-11-14T08:57:00Z',
    severity: 'Warning',
    explanation: 'The sample measurement is outside a demonstration threshold. It does not confirm illness or another condition.',
    status: 'New',
  },
  {
    id: 'AL-2038',
    astronautId: 'AST-004',
    title: 'Monitoring device disconnected',
    timestamp: '2026-11-14T08:42:00Z',
    severity: 'Information',
    explanation: 'A simulated device status changed to disconnected; no real device is paired.',
    status: 'New',
  },
  {
    id: 'AL-2037',
    astronautId: 'AST-003',
    title: 'Missing health measurements',
    timestamp: '2026-11-14T08:10:00Z',
    severity: 'Information',
    explanation: 'A sample check-in has no associated measurement record in this scenario.',
    status: 'Resolved',
  },
]

export const SAMPLE_OBSERVATIONS = [
  {
    id: 'OBS-SAMPLE-01',
    astronautId: 'AST-001',
    title: 'Routine sample review',
    notes: 'Illustrative note for demonstrating the observation history interface. Not a real medical record.',
    observedAt: '2026-11-13T16:30',
    status: 'Sample entry',
  },
  {
    id: 'OBS-SAMPLE-02',
    astronautId: 'AST-002',
    title: 'Check-in follow-up',
    notes: 'Example follow-up placeholder. No clinical assessment or recommendation is recorded.',
    observedAt: '2026-11-12T10:15',
    status: 'Sample entry',
  },
]

export const MEDICAL_NOTIFICATIONS = [
  { id: 'MN-001', type: 'Alert', title: 'New warning alert: AST-001', detail: 'Elevated heart-rate sample flagged for review.', time: '09:36 UTC · Today', astronautId: 'AST-001', read: false },
  { id: 'MN-002', type: 'Critical', title: 'Critical sample alert: AST-002', detail: 'Oxygen saturation sample requires clinician review.', time: '09:20 UTC · Today', astronautId: 'AST-002', read: false },
  { id: 'MN-003', type: 'Device', title: 'Device communication interruption', detail: 'A sample device state for AST-004 is disconnected.', time: '08:42 UTC · Today', astronautId: 'AST-004', read: true },
  { id: 'MN-004', type: 'Check-in', title: 'Scheduled health check-in', detail: 'Illustrative check-in reminder for ORBIT-01.', time: '08:00 UTC · Today', astronautId: 'AST-001', read: true },
]

export const MEASUREMENT_CATEGORIES = [
  { id: 'all', label: 'All measurements' },
  { id: 'heart-rate', label: 'Heart rate' },
  { id: 'oxygen', label: 'Blood oxygen' },
  { id: 'temperature', label: 'Temperature' },
  { id: 'respiratory-rate', label: 'Respiratory rate' },
]

export const MEDICAL_DEVICES = [
  { name: 'Heart-rate sensor', status: 'Connected · sample', id: 'DEV-HR-SAMPLE' },
  { name: 'Pulse oximeter', status: 'Connected · sample', id: 'DEV-OX-SAMPLE' },
  { name: 'Temperature sensor', status: 'Synchronizing · sample', id: 'DEV-TM-SAMPLE' },
  { name: 'Activity tracker', status: 'Disconnected · sample', id: 'DEV-AT-SAMPLE' },
]

export const MEDICAL_OVERVIEW_SUMMARIES = [
  { id: 'assigned', label: 'Assigned Astronauts', value: '04', detail: 'Fictional demo roster', icon: UsersRound, tone: 'cyan' },
  { id: 'monitoring', label: 'Monitoring Active', value: '03', detail: 'Illustrative status only', icon: Radio, tone: 'mint' },
  { id: 'review', label: 'Alerts Requiring Review', value: '03', detail: 'Sample new / acknowledged', icon: ShieldAlert, tone: 'amber' },
  { id: 'acknowledged', label: 'Acknowledged Alerts', value: '01', detail: 'Demo status count', icon: CircleCheck, tone: 'violet' },
]
