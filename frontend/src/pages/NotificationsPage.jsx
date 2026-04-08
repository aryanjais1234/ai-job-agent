import { useState, useEffect, useCallback } from 'react';
import {
  Bell, BellOff, Mail, Target, ClipboardList, BarChart3, Info,
  ChevronLeft, ChevronRight, CheckCheck,
} from 'lucide-react';
import * as notificationsApi from '../api/notifications';
import { Badge } from '../components/ui/Badge';
import { SkeletonCard } from '../components/ui/Skeleton';
import { EmptyState } from '../components/ui/EmptyState';
import { useToast } from '../components/ui/Toast';

const TYPE_CONFIG = {
  DAILY_DIGEST:        { icon: Mail,          color: 'bg-blue-100  text-blue-600' },
  MATCH_ALERT:         { icon: Target,        color: 'bg-brand-100 text-brand-600' },
  APPLICATION_UPDATE:  { icon: ClipboardList,  color: 'bg-green-100 text-green-600' },
  WEEKLY_REPORT:       { icon: BarChart3,      color: 'bg-amber-100 text-amber-600' },
  SYSTEM:              { icon: Info,           color: 'bg-gray-100  text-gray-600' },
};

const TABS = [
  { key: 'ALL',                label: 'All',          icon: null },
  { key: 'DAILY_DIGEST',      label: 'Daily Digest',  icon: Mail },
  { key: 'MATCH_ALERT',       label: 'Match Alerts',  icon: Target },
  { key: 'APPLICATION_UPDATE', label: 'App Updates',   icon: ClipboardList },
  { key: 'SYSTEM',            label: 'System',         icon: Info },
];

function relativeTime(dateStr) {
  const now = Date.now();
  const diff = now - new Date(dateStr).getTime();
  const minutes = Math.floor(diff / 60000);
  if (minutes < 1) return 'Just now';
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours} hour${hours > 1 ? 's' : ''} ago`;
  const days = Math.floor(hours / 24);
  if (days === 1) return 'Yesterday';
  if (days < 7) return `${days} days ago`;
  return new Date(dateStr).toLocaleDateString();
}

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('ALL');
  const [expandedId, setExpandedId] = useState(null);
  const toast = useToast();

  const loadNotifications = useCallback(async () => {
    setLoading(true);
    try {
      const data = await notificationsApi.getNotifications(page, 10);
      setNotifications(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch {
      toast.error('Failed to load notifications.');
    } finally {
      setLoading(false);
    }
  }, [page, toast]);

  useEffect(() => { loadNotifications(); }, [loadNotifications]);

  const handleMarkRead = async (id) => {
    try {
      await notificationsApi.markAsRead(id);
      setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, isRead: true } : n)));
    } catch {
      toast.error('Failed to mark as read.');
    }
  };

  const handleMarkAllRead = async () => {
    const unread = notifications.filter((n) => !n.isRead);
    try {
      await Promise.all(unread.map((n) => notificationsApi.markAsRead(n.id)));
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
      toast.success('All notifications marked as read.');
    } catch {
      toast.error('Failed to mark all as read.');
    }
  };

  const handleClick = (notif) => {
    if (!notif.isRead) handleMarkRead(notif.id);
    setExpandedId((prev) => (prev === notif.id ? null : notif.id));
  };

  const filtered =
    activeTab === 'ALL' ? notifications : notifications.filter((n) => n.type === activeTab);
  const unreadCount = notifications.filter((n) => !n.isRead).length;

  return (
    <div className="max-w-3xl mx-auto animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <Bell className="h-6 w-6 text-brand-600" />
          <h1 className="text-2xl font-bold text-gray-900">Notifications</h1>
          {unreadCount > 0 && (
            <Badge variant="brand" dot>{unreadCount} unread</Badge>
          )}
        </div>
        {unreadCount > 0 && (
          <button onClick={handleMarkAllRead} className="btn-ghost text-sm">
            <CheckCheck className="h-4 w-4" /> Mark all as read
          </button>
        )}
      </div>

      {/* Tabs */}
      <div className="flex gap-1 mb-6 overflow-x-auto pb-1">
        {TABS.map(({ key, label, icon: TabIcon }) => (
          <button
            key={key}
            onClick={() => { setActiveTab(key); setExpandedId(null); }}
            className={`inline-flex items-center gap-1.5 rounded-lg px-3 py-2 text-sm font-medium whitespace-nowrap transition-colors ${
              activeTab === key
                ? 'bg-brand-600 text-white shadow-sm'
                : 'text-gray-600 hover:bg-gray-100'
            }`}
          >
            {TabIcon && <TabIcon className="h-4 w-4" />}
            {label}
          </button>
        ))}
      </div>

      {/* Content */}
      {loading ? (
        <SkeletonCard count={4} />
      ) : filtered.length === 0 ? (
        <div className="card">
          <EmptyState
            icon={BellOff}
            title="No notifications"
            description="You're all caught up!"
          />
        </div>
      ) : (
        <>
          <div className="space-y-3">
            {filtered.map((notif) => {
              const cfg = TYPE_CONFIG[notif.type] || TYPE_CONFIG.SYSTEM;
              const Icon = cfg.icon;
              const isExpanded = expandedId === notif.id;

              return (
                <div
                  key={notif.id}
                  onClick={() => handleClick(notif)}
                  className={`card p-4 cursor-pointer transition-all hover:shadow-md ${
                    !notif.isRead ? 'border-l-4 border-l-brand-500' : ''
                  }`}
                >
                  <div className="flex items-start gap-3">
                    <div className={`shrink-0 rounded-full p-2 ${cfg.color}`}>
                      <Icon className="h-4 w-4" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between gap-2">
                        <h3
                          className={`text-sm truncate ${
                            notif.isRead ? 'font-medium text-gray-700' : 'font-semibold text-gray-900'
                          }`}
                        >
                          {notif.subject}
                        </h3>
                        <span className="text-xs text-gray-400 whitespace-nowrap shrink-0">
                          {relativeTime(notif.sentAt)}
                        </span>
                      </div>
                      <p className={`text-sm text-gray-500 mt-0.5 ${isExpanded ? '' : 'line-clamp-2'}`}>
                        {notif.content}
                      </p>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-3 mt-8">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="btn-secondary py-2"
              >
                <ChevronLeft className="h-4 w-4" /> Previous
              </button>
              <span className="text-sm text-gray-600">
                Page {page + 1} of {totalPages}
              </span>
              <button
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
                className="btn-secondary py-2"
              >
                Next <ChevronRight className="h-4 w-4" />
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
