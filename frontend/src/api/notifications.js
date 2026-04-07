import client from './client';

export const getNotifications = (page = 0, size = 10) =>
  client.get('/notifications', { params: { page, size } }).then(r => r.data);

export const markAsRead = (id) =>
  client.put(`/notifications/${id}/read`);

export const getNotificationPreferences = () =>
  client.get('/notifications/preferences').then(r => r.data);

export const updateNotificationPreferences = (data) =>
  client.put('/notifications/preferences', data).then(r => r.data);
