import { useState, useEffect, useCallback } from 'react';
import * as notificationsApi from '../api/notifications';

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);

  const loadNotifications = useCallback(async () => {
    setLoading(true);
    try {
      const data = await notificationsApi.getNotifications(page, 10);
      setNotifications(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch {
      // ignore
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => { loadNotifications(); }, [loadNotifications]);

  const handleMarkRead = async (id) => {
    try {
      await notificationsApi.markAsRead(id);
      setNotifications(prev => prev.map(n => n.id === id ? { ...n, isRead: true } : n));
    } catch { /* ignore */ }
  };

  const typeIcons = { DAILY_DIGEST: '📬', MATCH_ALERT: '🎯', APPLICATION_UPDATE: '📋' };

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Notifications</h1>

      {loading ? (
        <div className="flex items-center justify-center py-16">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600" />
        </div>
      ) : notifications.length === 0 ? (
        <div className="bg-white rounded-xl shadow-sm border p-12 text-center">
          <p className="text-3xl mb-4">🔔</p>
          <h2 className="text-xl font-semibold text-gray-800 mb-2">No notifications</h2>
          <p className="text-gray-500">You&apos;ll receive notifications when new job matches are found.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {notifications.map((notif) => (
            <div key={notif.id}
              className={`bg-white rounded-xl shadow-sm border p-4 cursor-pointer transition-colors ${
                notif.isRead ? 'border-gray-200' : 'border-indigo-300 bg-indigo-50'
              }`}
              onClick={() => !notif.isRead && handleMarkRead(notif.id)}>
              <div className="flex items-start space-x-3">
                <span className="text-2xl">{typeIcons[notif.type] || '🔔'}</span>
                <div className="flex-1">
                  <div className="flex items-center justify-between">
                    <h3 className={`font-medium ${notif.isRead ? 'text-gray-700' : 'text-gray-900'}`}>{notif.subject}</h3>
                    <span className="text-xs text-gray-500">{new Date(notif.sentAt).toLocaleDateString()}</span>
                  </div>
                  <p className="text-sm text-gray-600 mt-1">{notif.content}</p>
                </div>
                {!notif.isRead && <span className="w-2 h-2 bg-indigo-600 rounded-full mt-2" />}
              </div>
            </div>
          ))}
          {totalPages > 1 && (
            <div className="flex justify-center space-x-2 mt-6">
              <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}
                className="px-4 py-2 border rounded-lg disabled:opacity-50 hover:bg-gray-50">Previous</button>
              <span className="px-4 py-2 text-gray-600">Page {page + 1} of {totalPages}</span>
              <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1}
                className="px-4 py-2 border rounded-lg disabled:opacity-50 hover:bg-gray-50">Next</button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
