const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1').replace(/\/+$/, '')

let tokenPair = null
let refreshRequest = null

export function setTokenPair(pair) {
  tokenPair = pair
}

export function getTokenPair() {
  return tokenPair
}

function sessionExpired() {
  tokenPair = null
  window.dispatchEvent(new Event('astra:session-expired'))
}

async function readError(response) {
  let payload
  try {
    payload = await response.json()
  } catch {
    payload = null
  }
  const message = payload?.message || `Request failed with status ${response.status}.`
  const validation = payload?.validationErrors
  const details = validation && typeof validation === 'object'
    ? Object.values(validation).filter(Boolean).join(' ')
    : ''
  return new Error(details ? `${message} ${details}` : message)
}

async function refreshAccessToken() {
  if (!tokenPair?.refreshToken) throw new Error('Your session has expired. Please sign in again.')
  if (!refreshRequest) {
    refreshRequest = fetch(`${API_BASE_URL}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      body: JSON.stringify({ refreshToken: tokenPair.refreshToken }),
    })
      .then(async (response) => {
        if (!response.ok) throw await readError(response)
        const nextPair = await response.json()
        setTokenPair(nextPair)
        return nextPair
      })
      .catch((error) => {
        sessionExpired()
        throw error
      })
      .finally(() => {
        refreshRequest = null
      })
  }
  return refreshRequest
}

async function send(path, options, retry) {
  const headers = new Headers(options.headers || {})
  headers.set('Accept', 'application/json')
  if (options.body && !(options.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  if (tokenPair?.accessToken) headers.set('Authorization', `Bearer ${tokenPair.accessToken}`)
  const response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers })

  if (response.status === 401 && retry && tokenPair?.refreshToken && !path.startsWith('/auth/')) {
    await refreshAccessToken()
    return send(path, options, false)
  }
  if (!response.ok) throw await readError(response)
  if (response.status === 204) return null

  const contentType = response.headers.get('content-type') || ''
  return contentType.includes('application/json') ? response.json() : response.blob()
}

export function apiRequest(path, options = {}) {
  return send(path, options, true)
}

export const api = {
  login: (body) => apiRequest('/auth/login', { method: 'POST', body: JSON.stringify(body) }),
  register: (body) => apiRequest('/auth/register', { method: 'POST', body: JSON.stringify(body) }),
  logout: (refreshToken) => apiRequest('/auth/logout', { method: 'POST', body: JSON.stringify({ refreshToken }) }),
  me: () => apiRequest('/users/me'),
  astronauts: (query = '') => apiRequest(`/astronauts${query}`),
  astronaut: (id) => apiRequest(`/astronauts/${encodeURIComponent(id)}`),
  createAstronaut: (body) => apiRequest('/astronauts', { method: 'POST', body: JSON.stringify(body) }),
  updateAstronaut: (id, body) => apiRequest(`/astronauts/${encodeURIComponent(id)}`, { method: 'PUT', body: JSON.stringify(body) }),
  deleteAstronaut: (id) => apiRequest(`/astronauts/${encodeURIComponent(id)}`, { method: 'DELETE' }),
  medicalAstronauts: (query = '?page=0&size=100') => apiRequest(`/dashboard/medical-officer/astronauts${query}`),
  missions: (query = '?page=0&size=100') => apiRequest(`/dashboard/missions${query}`),
  mission: (id) => apiRequest(`/dashboard/missions/${encodeURIComponent(id)}`),
  devicesList: (query = '?page=0&size=100') => apiRequest(`/devices${query}`),
  readings: (astronautId, query = '?page=0&size=20') => apiRequest(`/health/astronauts/${encodeURIComponent(astronautId)}/readings${query}`),
  latestReadings: (astronautId) => apiRequest(`/dashboard/astronauts/${encodeURIComponent(astronautId)}/latest-readings`),
  healthSummary: (astronautId, from, to, bucket = 'DAY', type) => {
    const params = new URLSearchParams({ from, to, bucket })
    if (type) params.set('type', type)
    return apiRequest(`/dashboard/astronauts/${encodeURIComponent(astronautId)}/health-summary?${params}`)
  },
  devices: (astronautId) => apiRequest(`/devices/astronauts/${encodeURIComponent(astronautId)}`),
  alerts: (query = '') => apiRequest(`/alerts${query}`),
  astronautAlerts: (astronautId, query = '?page=0&size=100') => apiRequest(`/alerts/astronauts/${encodeURIComponent(astronautId)}${query}`),
  activeAlerts: (astronautId) => apiRequest(`/dashboard/astronauts/${encodeURIComponent(astronautId)}/active-alerts`),
  createAlert: (body) => apiRequest('/alerts', { method: 'POST', body: JSON.stringify(body) }),
  alertAction: (id, action) => apiRequest(`/alerts/${encodeURIComponent(id)}/${action}`, { method: 'PUT' }),
  alertHistory: (id, query = '') => apiRequest(`/alerts/${encodeURIComponent(id)}/history${query}`),
  observations: (astronautId, query = '?page=0&size=100') => apiRequest(`/medical-observations/astronauts/${encodeURIComponent(astronautId)}${query}`),
  createObservation: (body) => apiRequest('/medical-observations', { method: 'POST', body: JSON.stringify(body) }),
  updateObservation: (id, body) => apiRequest(`/medical-observations/${encodeURIComponent(id)}`, { method: 'PUT', body: JSON.stringify(body) }),
  observationHistory: (id, query = '') => apiRequest(`/medical-observations/${encodeURIComponent(id)}/history${query}`),
  observationFollowUps: (query = '?page=0&size=100') => apiRequest(`/medical-observations/follow-ups/inbox${query}`),
  notifications: (query = '?page=0&size=100') => apiRequest(`/notifications${query}`),
  markNotificationRead: (id) => apiRequest(`/notifications/${encodeURIComponent(id)}/read`, { method: 'PUT' }),
  markAllNotificationsRead: () => apiRequest('/notifications/read-all', { method: 'PUT' }),
  myWellbeingReports: (query = '?page=0&size=100') => apiRequest(`/wellbeing/reports/mine${query}`),
  wellbeingInbox: (query = '?page=0&size=100') => apiRequest(`/wellbeing/reports/inbox${query}`),
  submitWellbeingReport: (body) => apiRequest('/wellbeing/reports', { method: 'POST', body: JSON.stringify(body) }),
  reviewWellbeingReport: (id, body) => apiRequest(`/wellbeing/reports/${encodeURIComponent(id)}/review`, { method: 'PUT', body: JSON.stringify(body) }),
  previewAudioDraft: (audioId) => apiRequest(`/wellbeing/reports/audio-drafts/${encodeURIComponent(audioId)}`),
  deleteAudioDraft: (audioId) => apiRequest(`/wellbeing/reports/audio-drafts/${encodeURIComponent(audioId)}`, { method: 'DELETE' }),
  wellbeingReportAudio: (reportId) => apiRequest(`/wellbeing/reports/${encodeURIComponent(reportId)}/audio`),
  wellbeingReport: (reportId) => apiRequest(`/wellbeing/reports/${encodeURIComponent(reportId)}`),
  editWellbeingReport: (reportId, body) => apiRequest(`/wellbeing/reports/${encodeURIComponent(reportId)}`, { method: 'PUT', body: JSON.stringify(body) }),
  unreadNotificationCount: () => apiRequest('/notifications/unread-count'),
  exportHealthReport: (astronautId, from, to, format = 'CSV') => apiRequest(`/reports/astronauts/${encodeURIComponent(astronautId)}/health?${new URLSearchParams({ from, to, format })}`),
  exportMissionReport: (missionId, format = 'CSV') => apiRequest(`/reports/missions/${encodeURIComponent(missionId)}?${new URLSearchParams({ format })}`),
  exportAlertsReport: (astronautId, format = 'CSV') => {
    const params = new URLSearchParams({ format })
    if (astronautId) params.set('astronautId', astronautId)
    return apiRequest(`/reports/alerts?${params}`)
  },
  exportWellbeingReport: (astronautId, format = 'CSV') => apiRequest(`/reports/astronauts/${encodeURIComponent(astronautId)}/wellbeing?${new URLSearchParams({ format })}`),
  exportFollowUpsReport: (astronautId, format = 'CSV') => apiRequest(`/reports/astronauts/${encodeURIComponent(astronautId)}/follow-ups?${new URLSearchParams({ format })}`),
  uploadAudioDraft: (file) => {
    const body = new FormData()
    body.append('file', file)
    return apiRequest('/wellbeing/reports/audio-drafts', { method: 'POST', body })
  },
  auditEvents: (query = '?page=0&size=100') => apiRequest(`/audit-events${query}`),
}
