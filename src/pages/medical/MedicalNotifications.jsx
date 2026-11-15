import { useOutletContext } from 'react-router-dom'
import MedicalNotificationPanel from '../../components/medical/MedicalNotificationPanel'
import PageHeader from '../../components/dashboard/PageHeader'

function MedicalNotifications() {
  const { notifications, toggleNotificationRead, markAllNotificationsRead } = useOutletContext()

  return (
    <div className="space-y-5">
      <PageHeader
        eyebrow="Notification center"
        title="Notifications"
        description="Review account-scoped notifications and persist read status to the backend."
      />
      <MedicalNotificationPanel notifications={notifications} onToggleRead={toggleNotificationRead} onMarkAllRead={markAllNotificationsRead} />
    </div>
  )
}

export default MedicalNotifications
