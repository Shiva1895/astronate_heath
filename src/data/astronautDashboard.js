import {
  Activity,
  Bell,
  BellRing,
  ChartNoAxesCombined,
  CircleUserRound,
  FileText,
  HeartPulse,
  LayoutDashboard,
  MoonStar,
  Settings2,
  ShieldAlert,
  Thermometer,
  Watch,
  Wind,
} from 'lucide-react'

export const SAMPLE_PROFILE = {
  name: 'Alex Morgan',
  id: 'AST-001',
  role: 'Astronaut',
  mission: 'ORBIT-01',
  missionDay: 24,
  missionStatus: 'In progress',
  nextCheckIn: '18:00 UTC',
  crew: '4 crew members',
  avatar: 'AM',
}

export const DASHBOARD_NAVIGATION = [
  { label: 'Overview', to: '/astronaut/dashboard', icon: LayoutDashboard, end: true },
  { label: 'Health Monitoring', to: '/astronaut/dashboard/health', icon: HeartPulse },
  { label: 'Health Analytics', to: '/astronaut/dashboard/analytics', icon: ChartNoAxesCombined },
  { label: 'Alerts', to: '/astronaut/dashboard/alerts', icon: ShieldAlert },
  { label: 'Notifications', to: '/astronaut/dashboard/notifications', icon: Bell },
  { label: 'Devices', to: '/astronaut/dashboard/devices', icon: Watch },
  { label: 'Sleep & Activity', to: '/astronaut/dashboard/sleep-activity', icon: MoonStar },
  { label: 'Well-being Reports', to: '/astronaut/dashboard/wellbeing', icon: FileText },
  { label: 'My Profile', to: '/astronaut/dashboard/profile', icon: CircleUserRound },
  { label: 'Settings', to: '/astronaut/dashboard/settings', icon: Settings2 },
]

export const PAGE_TITLES = {
  '/astronaut/dashboard': 'Overview',
  '/astronaut/dashboard/health': 'Health Monitoring',
  '/astronaut/dashboard/analytics': 'Health Analytics',
  '/astronaut/dashboard/alerts': 'Alerts',
  '/astronaut/dashboard/notifications': 'Notifications',
  '/astronaut/dashboard/devices': 'Devices',
  '/astronaut/dashboard/sleep-activity': 'Sleep & Activity',
  '/astronaut/dashboard/wellbeing': 'Well-being Reports',
  '/astronaut/dashboard/profile': 'My Profile',
  '/astronaut/dashboard/settings': 'Settings',
}

export const HEALTH_METRICS = [
  {
    id: 'heart-rate',
    title: 'Heart Rate',
    value: '72',
    unit: 'BPM',
    trend: '+2 BPM from prior sample',
    direction: 'up',
    updated: '09:42 UTC',
    icon: HeartPulse,
    tone: 'cyan',
  },
  {
    id: 'oxygen',
    title: 'Blood Oxygen (SpO₂)',
    value: '98',
    unit: '%',
    trend: 'Stable across recent samples',
    direction: 'stable',
    updated: '09:41 UTC',
    icon: Activity,
    tone: 'violet',
  },
  {
    id: 'temperature',
    title: 'Body Temperature',
    value: '36.8',
    unit: '°C',
    trend: '−0.1 °C from prior sample',
    direction: 'down',
    updated: '09:40 UTC',
    icon: Thermometer,
    tone: 'blue',
  },
  {
    id: 'respiratory-rate',
    title: 'Respiratory Rate',
    value: '16',
    unit: 'breaths/min',
    trend: 'Stable across recent samples',
    direction: 'stable',
    updated: '09:39 UTC',
    icon: Wind,
    tone: 'mint',
  },
]

export const TREND_RANGES = [
  { id: '24h', label: '24 hours' },
  { id: '7d', label: '7 days' },
  { id: '30d', label: '30 days' },
]

export const HEALTH_TRENDS = {
  '24h': [
    { time: '00:00', heartRate: 66, spo2: 98, temperature: 36.6 },
    { time: '03:00', heartRate: 64, spo2: 97, temperature: 36.5 },
    { time: '06:00', heartRate: 68, spo2: 98, temperature: 36.6 },
    { time: '09:00', heartRate: 72, spo2: 98, temperature: 36.8 },
    { time: '12:00', heartRate: 75, spo2: 99, temperature: 36.8 },
    { time: '15:00', heartRate: 71, spo2: 98, temperature: 36.7 },
    { time: '18:00', heartRate: 69, spo2: 98, temperature: 36.7 },
    { time: '21:00', heartRate: 67, spo2: 97, temperature: 36.6 },
  ],
  '7d': [
    { time: 'Mon', heartRate: 68, spo2: 98, temperature: 36.6 },
    { time: 'Tue', heartRate: 70, spo2: 97, temperature: 36.7 },
    { time: 'Wed', heartRate: 67, spo2: 98, temperature: 36.6 },
    { time: 'Thu', heartRate: 73, spo2: 99, temperature: 36.8 },
    { time: 'Fri', heartRate: 71, spo2: 98, temperature: 36.7 },
    { time: 'Sat', heartRate: 69, spo2: 98, temperature: 36.7 },
    { time: 'Sun', heartRate: 72, spo2: 98, temperature: 36.8 },
  ],
  '30d': [
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
  ],
}

export const SAMPLE_ALERTS = [
  {
    id: 'alert-heart-rate',
    title: 'Elevated heart rate detected',
    time: '09:36 UTC · Today',
    severity: 'Warning',
    description: 'A sample reading crossed a configured review threshold. Check context with your medical officer.',
    status: 'New',
  },
  {
    id: 'alert-connection',
    title: 'Sensor connection interrupted',
    time: '08:52 UTC · Today',
    severity: 'Critical',
    description: 'The activity tracker has not synchronized recently in this illustrative scenario.',
    status: 'Acknowledged',
  },
  {
    id: 'alert-review',
    title: 'Health measurement requires review',
    time: 'Yesterday · 17:20 UTC',
    severity: 'Information',
    description: 'A sample measurement is available for routine medical team review.',
    status: 'Resolved',
  },
]

export const SAMPLE_DEVICES = [
  {
    id: 'DEV-HR-204',
    name: 'Heart-rate sensor',
    category: 'Wearable vital sensor',
    status: 'Connected',
    lastSync: '09:42 UTC',
    battery: 86,
    serial: 'HR-SAMPLE-204',
    detail: 'Illustrative heart-rate samples are listed for interface review only.',
    icon: HeartPulse,
  },
  {
    id: 'DEV-OX-118',
    name: 'Pulse oximeter',
    category: 'Oxygen measurement',
    status: 'Connected',
    lastSync: '09:41 UTC',
    battery: 63,
    serial: 'OX-SAMPLE-118',
    detail: 'This simulated device does not transmit or measure real oxygen saturation.',
    icon: Activity,
  },
  {
    id: 'DEV-TM-031',
    name: 'Temperature sensor',
    category: 'Body temperature',
    status: 'Synchronizing',
    lastSync: '09:40 UTC',
    battery: 100,
    serial: 'TM-SAMPLE-031',
    detail: 'Synchronizing is a sample UI state; no connection is active.',
    icon: Thermometer,
  },
  {
    id: 'DEV-AT-085',
    name: 'Activity tracker',
    category: 'Movement and activity',
    status: 'Disconnected',
    lastSync: '08:52 UTC',
    battery: 23,
    serial: 'AT-SAMPLE-085',
    detail: 'The disconnected state is illustrative and does not describe a real device.',
    icon: Watch,
  },
]

export const SLEEP_ACTIVITY = [
  { id: 'sleep-duration', title: 'Sleep duration', value: '7h 42m', detail: 'Sample goal: 8 hours', progress: 96, icon: MoonStar, tone: 'violet' },
  { id: 'sleep-quality', title: 'Sleep quality', value: 'Good', detail: 'Illustrative sleep summary', progress: 82, icon: Activity, tone: 'cyan' },
  { id: 'activity-duration', title: 'Daily activity', value: '2h 18m', detail: 'Sample movement time', progress: 74, icon: Activity, tone: 'mint' },
  { id: 'exercise-duration', title: 'Exercise', value: '46 min', detail: 'Sample planned activity', progress: 68, icon: BellRing, tone: 'blue' },
]

export const DEMO_NOTIFICATIONS = [
  { id: 'notice-alert', title: 'One sample alert needs review', time: '09:36 UTC' },
  { id: 'notice-device', title: 'Activity tracker shows disconnected', time: '08:52 UTC' },
  { id: 'notice-mission', title: 'Next sample check-in at 18:00 UTC', time: '08:00 UTC' },
]
